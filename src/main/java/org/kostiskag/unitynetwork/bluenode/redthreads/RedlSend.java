package org.kostiskag.unitynetwork.bluenode.redthreads;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.routing.packet.IPv4Packet;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.LocalRedNode;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.AppLogger.MessageType;
import org.kostiskag.unitynetwork.bluenode.App;


/**
 * This service runs for every user it opens a UDP socket and waits a row to
 * fill then it sends the packets
 * 
 * @author Konstantinos Kagiampakis
 */
public class RedlSend extends Thread {

    private final String pre;
    private final LocalRedNode rn;
    //socket    
    private int clientPort;
    private int serverPort;
    private DatagramSocket serverSocket;
    //triggers
    private boolean trigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);

    /**
     * First the class must find all the valuable information to open the socket
     * we do this on the constructor so that the running time will be charged on
     * the AuthService Thread
     * 
     * IF FISH NEVER FISHES THEN EVERYTHING IS STUCK AND WE
     * HAVE A DEAD ENTRY
     */
    public RedlSend(LocalRedNode rn) {
        this.rn = rn;
        pre = "^RedlSend "+rn.getHostname()+" ";
        serverPort = App.bn.UDPports.requestPort();
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
            AppLogger.getInstance().consolePrint(pre + "PORT ALLREADY BINDED, EXITING");
            return;
        } catch (SocketException ex) {
            Logger.getLogger(RedlSend.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            serverSocket.setSoTimeout(10000);
        } catch (SocketException ex) {
            Logger.getLogger(RedlSend.class.getName()).log(Level.SEVERE, null, ex);
        }

        byte[] buffer = new byte[2048];
        DatagramPacket receivedUDPPacket = new DatagramPacket(buffer, buffer.length);
        try {
        	serverSocket.receive(receivedUDPPacket);
        } catch (java.net.SocketTimeoutException ex) {
            AppLogger.getInstance().consolePrint(pre + "FISH SOCKET TIMEOUT");
            return;
        } catch (java.net.SocketException ex) {
            AppLogger.getInstance().consolePrint(pre + "FISH SOCKET CLOSED, EXITING");
            return;
        } catch (IOException ex) {
            AppLogger.getInstance().consolePrint(pre + "IO EXCEPTION");
            Logger.getLogger(RedlSend.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        InetAddress clientAddress = receivedUDPPacket.getAddress();
        clientPort = receivedUDPPacket.getPort();
        System.out.println(pre+"you are"+clientAddress.getHostAddress()+" from "+clientPort);

        /*
         * The task at hand is to use a synchronized packet queue and
         * a while to get the packets from the queue and send them to the socket.
         * In other words when there is a packet on queue write on socket.
         */
        while (!kill.get()) {
            byte[] packet = null;
            
        	try {
				packet = rn.getSendQueue().pollWithTimeout();
			} catch (Exception e1) {
				//this means that wait has exceeded the maximum wait time
				//in which case keep alive messages are going to be served
				packet = UnityPacket.buildKeepAlivePacket();
			}                   
            
            if (kill.get()) {
            	break;
            } else if (packet == null) {
            	continue;
            } else if (packet.length == 0) {
            	continue;
            } else if (packet.length > 1500) {
                AppLogger.getInstance().trafficPrint("Throwing oversized packet of size "+packet.length, MessageType.ROUTING, NodeType.REDNODE);
                continue;
            }

            DatagramPacket sendUDPPacket = new DatagramPacket(packet, packet.length, clientAddress, clientPort);
            try {
               
                if (UnityPacket.isUnity(packet)) {
					if (UnityPacket.isKeepAlive(packet)) {
						for (int i=0; i<3; i++) {
							serverSocket.send(sendUDPPacket);
                            AppLogger.getInstance().trafficPrint(pre +"KEEP ALIVE SENT", MessageType.KEEP_ALIVE, NodeType.REDNODE);
						}
					} else if (UnityPacket.isDping(packet)) {
						serverSocket.send(sendUDPPacket);
                        AppLogger.getInstance().trafficPrint(pre + "DPING SENT", MessageType.PINGS, NodeType.REDNODE);
					} else if (UnityPacket.isShortRoutedAck(packet)) {
						serverSocket.send(sendUDPPacket);
                        AppLogger.getInstance().trafficPrint(pre + "SHORT ACK SENT", MessageType.PINGS, NodeType.REDNODE);
                    } else if (UnityPacket.isLongRoutedAck(packet)) {
                    	serverSocket.send(sendUDPPacket);
                    	try {
                            AppLogger.getInstance().trafficPrint(pre + "ACK -> "+UnityPacket.getDestAddress(packet)+" SENT", MessageType.ACKS, NodeType.REDNODE);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (UnityPacket.isMessage(packet)) {
						serverSocket.send(sendUDPPacket);
						try {
                            AppLogger.getInstance().trafficPrint(pre + "MESSAGE -> "+UnityPacket.getDestAddress(packet)+" SENT", MessageType.ROUTING, NodeType.REDNODE);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else if (IPv4Packet.isIPv4(packet)) {
					serverSocket.send(sendUDPPacket);
                    AppLogger.getInstance().trafficPrint(pre + "IPV4 SENT", MessageType.ROUTING, NodeType.REDNODE);
				}
                if (App.bn.gui && !trigger) {
                    MainWindow.getInstance().setSentDataToRn();
                    trigger = true;
                }
            } catch (java.net.SocketException ex1) {
                break;
            } catch (IOException ex) {
                ex.printStackTrace();
                AppLogger.getInstance().consolePrint(pre + "IO ERROR");
                break;
            }
        }
        serverSocket.close();
        App.bn.UDPports.releasePort(serverPort);
        AppLogger.getInstance().consolePrint(pre + "ENDED");
    }

    public void kill() {
        kill.set(true);
        rn.getSendQueue().clear();
    }       
}
