package org.kostiskag.unitynetwork.bluenode.bluethreads;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.*;

import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.routing.packet.IPv4Packet;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.redthreads.RedReceive;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.PortHandle;


/**
 * In matters of communication this class it tricky. Do not expect BlueReceive to be a client or server
 * as it may be both. Depending on which bluenode starts the communication first the server and client roles
 * may be defined. However, as the class suggests despite being run either as server or as client this 
 * class will always receive.
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueReceive extends Thread {

    private final String pre;
    private final BlueNode blueNode;
    private final boolean isServer;
    //connection
    private int serverPort;
    private int portToReceive;   
    private DatagramSocket socket;
    private InetAddress blueNodePhAddress;
	//triggers
	private Boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);
    
    /**
     * This is the server constructor. 
     */
    public BlueReceive(BlueNode blueNode) {
    	this.isServer = true;
        this.serverPort = PortHandle.getInstance().requestPort();
        this.blueNode = blueNode;
        this.pre = "^BlueDownServiceServer "+blueNode.getName()+" ";
        //simply since the server listens the port to receive is the server port
        this.portToReceive = serverPort;
    }
    
    /**
     * This is the client constructor
     */
    public BlueReceive(BlueNode blueNode, int portToReceive) {
    	this.isServer = false;
        this.blueNode = blueNode;
        this.pre = "^BlueReceive "+blueNode.getName()+" ";
        this.blueNodePhAddress = blueNode.getPhaddress();    	
        //collects the port to receive from auth
    	this.portToReceive = portToReceive;        
    }
    
    public int getServerPort() {
    	return serverPort;
    }   
    
    public int getPortToReceive() {
		return portToReceive;
	}

    public BlueNode getBlueNode() {
		return blueNode;
	}
    
    public boolean isKilled() {
		return kill.get();
	}
    
    @Override
    public void run() {
    	if (isServer) {
    		buildServer();
    	} else {
    		buildClient();
    	}

        AppLogger.getInstance().consolePrint(pre + "STARTED AT " + Thread.currentThread().getName());
    	byte[] receiveData = new byte[2048];
    	while (!kill.get()) {
        	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(receivePacket);
                int len = receivePacket.getLength();
                if (len > 0 && len <= 1500) {
                    byte[] packet = new byte[len];
                    System.arraycopy(receivePacket.getData(), 0, packet, 0, len);
                    if (UnityPacket.isUnity(packet)) {
    					if (UnityPacket.isKeepAlive(packet)) {
    						// keep alive
                            AppLogger.getInstance().trafficPrint(pre +"KEEP ALIVE RECEIVED", AppLogger.MessageType.KEEP_ALIVE, NodeType.BLUENODE);
    					} else if (UnityPacket.isUping(packet)) {
                            //blue node uping!
    						blueNode.setUping(true);
                            AppLogger.getInstance().trafficPrint(pre + "UPING RECEIVED", AppLogger.MessageType.PINGS, NodeType.BLUENODE);
                        } else if (UnityPacket.isDping(packet)) {
                            //blue node dping!
    						blueNode.setDping(true);
                            AppLogger.getInstance().trafficPrint(pre + "DPING RECEIVED", AppLogger.MessageType.PINGS, NodeType.BLUENODE);
                        } else if (UnityPacket.isShortRoutedAck(packet)) {
                        	//collect the ack
                        	try {
								blueNode.getUploadMan().gotACK(UnityPacket.getShortRoutedAckTrackNum(packet));
                                AppLogger.getInstance().trafficPrint(pre + "SHORT ACK RECEIVED", AppLogger.MessageType.PINGS, NodeType.BLUENODE);
							} catch (Exception e) {
								e.printStackTrace();
							}
                        	
                        } else if (UnityPacket.isLongRoutedAck(packet)) {
    						//route the L-ACK
                        	try {
    							blueNode.getReceiveQueue().offer(packet);
                                AppLogger.getInstance().trafficPrint(pre + "ACK-> "+UnityPacket.getDestAddress(packet)+" RECEIVED", AppLogger.MessageType.ACKS, NodeType.BLUENODE);
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
    					} else if (UnityPacket.isMessage(packet)) {
    						blueNode.getReceiveQueue().offer(packet);
                            AppLogger.getInstance().trafficPrint(pre + "MESSAGE RECEIVED", AppLogger.MessageType.ROUTING, NodeType.BLUENODE);
    						
    						//build and offer a short routed ack towards the sender bluenode
    						byte[] ACKS = UnityPacket.buildShortRoutedAckPacket(blueNode.getReceiveQueue().getlen());
    						blueNode.getSendQueue().offer(ACKS);
    					}        				
                    } else if (IPv4Packet.isIPv4(packet)) {
                    	blueNode.getReceiveQueue().offer(packet);
                        AppLogger.getInstance().trafficPrint(pre + "IPV4 RECEIVED", AppLogger.MessageType.ROUTING, NodeType.BLUENODE);
                    	
                    	//build and offer a short routed ack towards the sender bluenode
						byte[] ACKS = UnityPacket.buildShortRoutedAckPacket(blueNode.getReceiveQueue().getlen());
						blueNode.getSendQueue().offer(ACKS);
                    }
                    
                    if (!didTrigger) {
                    	if (Bluenode.getInstance().isGui()) {
                    		MainWindow.getInstance().setReceivedBnData();
                        }
                    	didTrigger = true;
                    }
                } else {
                    System.out.println(pre + "MAXIMUM LENGTH EXCEDED");
                }
            } catch (java.net.SocketException ex1) {
                AppLogger.getInstance().consolePrint(pre + "SOCKET ERROR");
            } catch (IOException ex) {
                AppLogger.getInstance().consolePrint(pre + "IO ERROR");
            }
        }
    	socket.close();
    	if (isServer) {
    		PortHandle.getInstance().releasePort(serverPort);
    	}
        AppLogger.getInstance().consolePrint(pre + "ENDED");
    }

	public void kill() {
    	kill.set(true);
    	socket.close();             
    }      
	
	private void buildServer() {
    	socket = null;
        try {
            socket = new DatagramSocket(serverPort);
        } catch (java.net.BindException ex) {
            AppLogger.getInstance().consolePrint(pre + "PORT ALLREADY IN USE");
            ex.printStackTrace();
            return;
        } catch (SocketException ex) {
            ex.printStackTrace();
            return;
        }
	}
	
	private void buildClient() {
		socket = null;
        try {
            socket = new DatagramSocket();
        } catch (java.net.BindException ex) {
            AppLogger.getInstance().consolePrint(pre + "PORT ALLREADY IN USE, EXITING");
            return;
        } catch (SocketException ex) {
            Logger.getLogger(RedReceive.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        byte[] sendData = "FISH PACKET".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, blueNodePhAddress, portToReceive);
        try {
            for (int i = 0; i < 3; i++) {
                socket.send(sendPacket);
            }
        } catch (java.net.SocketException ex1) {
            AppLogger.getInstance().trafficPrint("FISH PACKET SEND ERROR",AppLogger.MessageType.ROUTING, NodeType.BLUENODE);
            return;
        } catch (IOException ex) {
            AppLogger.getInstance().trafficPrint("FISH PACKET SEND ERROR", AppLogger.MessageType.ROUTING, NodeType.BLUENODE);
            ex.printStackTrace();
            return;
        }
        AppLogger.getInstance().trafficPrint("FISH PACKET",AppLogger.MessageType.ROUTING, NodeType.BLUENODE);
	}
}
