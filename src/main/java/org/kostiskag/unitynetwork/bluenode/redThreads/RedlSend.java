package org.kostiskag.unitynetwork.bluenode.redThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;
import org.kostiskag.unitynetwork.common.routing.packet.IPv4Packet;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;

/**
 * This service runs for every user it opens a UDP socket and waits a row to
 * fill then it sends the packets
 * 
 * @author Konstantinos Kagiampakis
 */
public class RedlSend extends Thread {

    private final String pre;
    private final LocalRedNodeInstance rn;
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
    public RedlSend(LocalRedNodeInstance rn) {        
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
    
    public LocalRedNodeInstance getRn() {
		return rn;
	}
    
    public boolean getIsKilled() {
    	return kill.get();
    }
    
    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED AT " + Thread.currentThread().getName() + " ON PORT " + serverPort);        
        
        try {
            serverSocket = new DatagramSocket(serverPort);
        } catch (java.net.BindException ex) {
            App.bn.ConsolePrint(pre + "PORT ALLREADY BINDED, EXITING");
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
            App.bn.ConsolePrint(pre + "FISH SOCKET TIMEOUT");
            return;
        } catch (java.net.SocketException ex) {
            App.bn.ConsolePrint(pre + "FISH SOCKET CLOSED, EXITING");
            return;
        } catch (IOException ex) {
        	App.bn.ConsolePrint(pre + "IO EXCEPTION");
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
            	App.bn.TrafficPrint("Throwing oversized packet of size "+packet.length, 3, 0);
                continue;
            }

            DatagramPacket sendUDPPacket = new DatagramPacket(packet, packet.length, clientAddress, clientPort);
            try {
               
                if (UnityPacket.isUnity(packet)) {
					if (UnityPacket.isKeepAlive(packet)) {
						for (int i=0; i<3; i++) {
							serverSocket.send(sendUDPPacket);
							App.bn.TrafficPrint(pre +"KEEP ALIVE SENT", 0, 0);
						}
					} else if (UnityPacket.isDping(packet)) {
						serverSocket.send(sendUDPPacket);
						App.bn.TrafficPrint(pre + "DPING SENT", 1, 0);
					} else if (UnityPacket.isShortRoutedAck(packet)) {
						serverSocket.send(sendUDPPacket);
						App.bn.TrafficPrint(pre + "SHORT ACK SENT", 1, 0);
                    } else if (UnityPacket.isLongRoutedAck(packet)) {
                    	serverSocket.send(sendUDPPacket);
                    	try {
							App.bn.TrafficPrint(pre + "ACK -> "+UnityPacket.getDestAddress(packet)+" SENT", 2, 0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (UnityPacket.isMessage(packet)) {
						serverSocket.send(sendUDPPacket);
						try {
							App.bn.TrafficPrint(pre + "MESSAGE -> "+UnityPacket.getDestAddress(packet)+" SENT", 3, 0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else if (IPv4Packet.isIPv4(packet)) {
					serverSocket.send(sendUDPPacket);
					App.bn.TrafficPrint(pre + "IPV4 SENT", 3, 0);
				}
                if (App.bn.gui && !trigger) {
                    MainWindow.jCheckBox4.setSelected(true);
                    trigger = true;
                }
            } catch (java.net.SocketException ex1) {
                break;
            } catch (IOException ex) {
                ex.printStackTrace();
                App.bn.ConsolePrint(pre + "IO ERROR");
                break;
            }
        }
        serverSocket.close();
        App.bn.UDPports.releasePort(serverPort);
        App.bn.ConsolePrint(pre + "ENDED");                
    }

    public void kill() {
        kill.set(true);
        rn.getSendQueue().clear();
    }       
}
