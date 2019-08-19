package org.kostiskag.unitynetwork.bluenode.redthreads;

import java.util.concurrent.atomic.AtomicBoolean;
import java.net.*;
import java.io.IOException;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.service.PortHandle;
import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.routing.packet.IPv4Packet;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;

import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.LocalRedNode;
import org.kostiskag.unitynetwork.bluenode.AppLogger.MessageType;


/**
 * down service listens for virtual packets then sends them to the target
 * specified by viewing the table if it fails to find the target the packet is
 * discarded
 *
 * down service runs differently for every rednode and every associated blue
 * node
 * 
 * @author Konstantinos Kagiampakis
 */
public class RedReceive extends Thread {

    private final String pre;
    private final LocalRedNode rn;
    //socket
    private DatagramSocket serverSocket;
    private int clientPort;
    private int serverPort;
    //triggers
    private Boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);

    public RedReceive(LocalRedNode rn) {
        this.rn = rn;
    	pre =  "^RedReceive "+rn.getHostname()+" ";
    	serverPort = PortHandle.getInstance().requestPort();
    }

    public int getServerPort() {
        return serverPort;
    }        
    
    public int getClientPort() {
		return clientPort;
	}
    
    public LocalRedNode getRn() {
		return rn;
	}
    
    public boolean getIsKilled() {
    	return kill.get();
    }

    @Override
    public void run() {
        AppLogger.getInstance().consolePrint(pre + "STARTED AT " + Thread.currentThread().getName() + " ON PORT " + serverPort);
     
        try {
            serverSocket = new DatagramSocket(serverPort);
        } catch (java.net.BindException ex) {
            AppLogger.getInstance().consolePrint(pre + "PORT ALLREADY IN USE, EXITING");
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        byte[] receiveData = new byte[2048];
        byte[] packet = null;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        while (!kill.get()) {
            try {
                serverSocket.receive(receivePacket);
                int len = receivePacket.getLength();
                if (len > 0 && len <= 1500) {
                    packet = new byte[len];
                    System.arraycopy(receivePacket.getData(), 0, packet, 0, len);                    
                    if (UnityPacket.isUnity(packet)) {
                    	if (UnityPacket.isKeepAlive(packet)) {
                            //keep alive packet received
                            AppLogger.getInstance().trafficPrint(pre +"KEEP ALIVE RECEIVED", MessageType.KEEP_ALIVE, NodeType.REDNODE);
                        }  else if (UnityPacket.isUping(packet)){
                        	//rednode uping packet received by the aspect of the red node
                        	//the red node tests its upload
                            rn.setUPing(true);
                            AppLogger.getInstance().trafficPrint(pre + "UPING RECEIVED", MessageType.PINGS, NodeType.REDNODE);
                        } else if (UnityPacket.isShortRoutedAck(packet)) {
                        	//you should not do something
                            AppLogger.getInstance().trafficPrint(pre + "SHORT ACK RECEIVED", MessageType.PINGS, NodeType.REDNODE);
                        } else if (UnityPacket.isLongRoutedAck(packet)){
                        	try {
                                AppLogger.getInstance().trafficPrint(pre + "ACK -> "+UnityPacket.getDestAddress(packet).asString()+" RECEIVED", MessageType.ACKS, NodeType.REDNODE);
								//now you have control over the buffer
								// do stuff here
								rn.getReceiveQueue().offer(packet);
							} catch (Exception e) {
								e.printStackTrace();
							}
                        } else if (UnityPacket.isMessage(packet)) {
                        	try {
                                AppLogger.getInstance().trafficPrint(pre + "Message -> "+UnityPacket.getDestAddress(packet).asString()+" RECEIVED", MessageType.ACKS, NodeType.REDNODE);
								//now you have controll over the buffer
								// do stuff here
								rn.getReceiveQueue().offer(packet);
								
								//build and offer a short routed ack
								byte[] ACKS = UnityPacket.buildShortRoutedAckPacket((short)rn.getReceiveQueue().getlen());
								rn.getSendQueue().offer(ACKS);
								
                        	} catch (Exception e) {
								e.printStackTrace();
							}
                        }                                                    
                    } else if (IPv4Packet.isIPv4(packet)){
                        AppLogger.getInstance().trafficPrint(pre + "IPv4", MessageType.ROUTING, NodeType.REDNODE);
                        //now you have control over the buffer
						// do stuff here
						rn.getReceiveQueue().offer(packet);   
						
						//build and offer a short routed ack
						byte[] ACKS = UnityPacket.buildShortRoutedAckPacket((short) rn.getReceiveQueue().getlen());
						rn.getSendQueue().offer(ACKS);
                    }
                    if (AppLogger.getInstance().isGui() && !didTrigger) {
                        MainWindow.getInstance().setReceivedLocalRnData();
                        didTrigger = true;
                    }
                }
            } catch (java.net.SocketException ex1) {
                break;
            } catch (IOException ex) {
                AppLogger.getInstance().consolePrint(pre + "IO ERROR");
                ex.printStackTrace();
                break;
            }
        }               
        PortHandle.getInstance().releasePort(clientPort);
        rn.getReceiveQueue().clear();
        AppLogger.getInstance().consolePrint(pre + "ENDED");
    }

    public void kill() {
        kill.set(true);
        serverSocket.close();
    }
}