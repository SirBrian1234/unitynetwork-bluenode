package org.kostiskag.unitynetwork.bluenode.bluethreads;

import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.routing.packet.IPv4Packet;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;

import org.kostiskag.unitynetwork.bluenode.service.PortHandle;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.redthreads.RedlSend;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.AppLogger.MessageType;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueSend extends Thread {

    private final String pre;
    private final BlueNode blueNode;
    private final boolean isServer;
    //connection
    private int serverPort;
    private int portToSend;    
    private PhysicalAddress blueNodePhAddress;
    private DatagramSocket socket;    
    //triggers
    private boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);  

    /**
     * This is the server constructor
     * It builds a socket based on a port collected from the available 
     * port pool
     */
    public BlueSend(BlueNode blueNode) {
    	this.isServer = true;
    	this.blueNode = blueNode;
    	this.pre = "^BlueSend "+blueNode.getHostname()+" ";
        this.blueNodePhAddress = blueNode.getAddress();
        this.serverPort = PortHandle.getInstance().requestPort();
    }
    
    /**
     * This is the client constructor
     * It collects a port to send from the auth
     */
    public BlueSend(BlueNode blueNode, int portToSend) throws UnknownHostException {
    	this.isServer = false;
		this.blueNode = blueNode;
		this.pre = "^BlueSend " + blueNode.getHostname() + " ";
		this.blueNodePhAddress = blueNode.getAddress();
		//when in client, port to send may be collected from auth
		this.portToSend = portToSend;
	}
    
    public int getServerPort() {
    	if (isServer) {
    		return serverPort;
    	} else {
    		return 0;
    	}
    }    
    
    public int getPortToSend() {
		return portToSend;
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
    	    try {
                buildServer();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

    	} else {
    		buildClient();
    	}

        AppLogger.getInstance().consolePrint(pre + "STARTED AT " + Thread.currentThread().getName());
        while (!kill.get()) {        	
        	byte packet[];
            try {
                packet = blueNode.getSendQueue().pollWithTimeout();
            } catch (java.lang.NullPointerException ex1) {
                continue;
            } catch (java.util.NoSuchElementException ex) {
                continue;
            } catch (Exception e) {
            	//this means that wait has exceeded the maximum wait time
				//in which case keep alive messages are going to be served
				packet = UnityPacket.buildKeepAlivePacket();
			} 

            DatagramPacket sendUDPPacket = new DatagramPacket(packet, packet.length, blueNodePhAddress.asInet(), portToSend);
            try {
            	if (UnityPacket.isUnity(packet)) {
					if (UnityPacket.isKeepAlive(packet)) {
						//send three keep alive packets
						for (int i=0; i<3; i++) {
							socket.send(sendUDPPacket);
                            AppLogger.getInstance().trafficPrint(pre +"KEEP ALIVE SENT", MessageType.KEEP_ALIVE, NodeType.BLUENODE);
						}						
					} else if (UnityPacket.isUping(packet)) {
						//blue node uping!
						socket.send(sendUDPPacket);
                        AppLogger.getInstance().trafficPrint(pre + "UPING SENT", MessageType.PINGS, NodeType.BLUENODE);
					} else if (UnityPacket.isDping(packet)) {
						//blue node dping!
						socket.send(sendUDPPacket);
                        AppLogger.getInstance().trafficPrint(pre + "DPING SENT", MessageType.PINGS, NodeType.BLUENODE);
					} else if (UnityPacket.isShortRoutedAck(packet)) {
						//short ack sent
						socket.send(sendUDPPacket);
                        AppLogger.getInstance().trafficPrint(pre + "SHORT ACK SENT", MessageType.PINGS, NodeType.BLUENODE);
					} else if (UnityPacket.isLongRoutedAck(packet)) {
						socket.send(sendUDPPacket);
						try {
                            AppLogger.getInstance().trafficPrint(pre + "LONG ACK-> "+UnityPacket.getDestAddress(packet)+" SENT", MessageType.ACKS, NodeType.BLUENODE);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (UnityPacket.isMessage(packet)) {
						blueNode.getUploadMan().waitToSend();
						socket.send(sendUDPPacket);
                        AppLogger.getInstance().trafficPrint(pre + "MESSAGE SENT", MessageType.ROUTING, NodeType.BLUENODE);
					}
				} else if (IPv4Packet.isIPv4(packet)) {
					blueNode.getUploadMan().waitToSend();
					socket.send(sendUDPPacket);
                    AppLogger.getInstance().trafficPrint(pre + "IPV4 SENT", MessageType.ROUTING, NodeType.BLUENODE);
				}
                
                if (!didTrigger) {
                	if (AppLogger.getInstance().isGui()) {
                		MainWindow.getInstance().setSentDataToRn();
                	}
                	didTrigger = true;
                }
            } catch (java.net.SocketException ex1) {
                AppLogger.getInstance().consolePrint(pre + " SOCKET ERROR");
                break;
            } catch (IOException ex) {
                AppLogger.getInstance().consolePrint(pre + "IO ERROR");
                break;
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

    private void buildClient() {
        AppLogger.getInstance().consolePrint(pre+"building client.");
		socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException ex) {
			blueNode.getSendQueue().clear();
            AppLogger.getInstance().consolePrint(pre + "socket could not be opened");
			ex.printStackTrace();
		}
	}

	private void buildServer() throws UnknownHostException {
        AppLogger.getInstance().consolePrint(pre+"building server.");
        try {
            socket = new DatagramSocket(serverPort);
        } catch (SocketException ex) {
            AppLogger.getInstance().consolePrint(pre + "socket could not be opened");
        	ex.printStackTrace();
            return;
        }
        try {
            socket.setSoTimeout(10000);
        } catch (SocketException ex) {
            ex.printStackTrace();
            return;
        }

        byte[] data = new byte[2048];
        DatagramPacket receivedUDPPacket = new DatagramPacket(data, data.length);
        try {
            socket.receive(receivedUDPPacket);
        } catch (java.net.SocketTimeoutException ex) {
            AppLogger.getInstance().consolePrint(pre +"FISH SOCKET TIMEOUT");
            return;
        } catch (java.net.SocketException ex) {
            AppLogger.getInstance().consolePrint(pre + "FISH SOCKET CLOSED, EXITING");
            return;
        } catch (IOException ex) {
            Logger.getLogger(RedlSend.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        //when in server, port to send may be found when a client sends a packet
        portToSend = receivedUDPPacket.getPort();
        blueNodePhAddress = PhysicalAddress.valueOf(receivedUDPPacket.getAddress().getHostAddress());
	}

}
