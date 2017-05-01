package kostiskag.unitynetwork.bluenode.blueThreads;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.packets.IPv4Packet;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.gui.MainWindow;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.redThreads.RedReceive;

/**
 *
 * @author kostis
 */
public class BlueReceiveClient extends Thread {

    private final String pre;
    private final BlueNodeInstance blueNode;
    private final InetAddress blueNodePhAddress;
    private final int upPort;
    private DatagramSocket clientSocket;
    private boolean didTrigger;
    private AtomicBoolean kill = new AtomicBoolean(false);
    
    /**
     * First the class must find all the valuable information to open the socket
     * we do this on the constructor so that the running time will be charged on
     * the AuthService Thread
     */
    public BlueReceiveClient(BlueNodeInstance blueNode, int upPort) {
        this.blueNode = blueNode;
        this.pre = "^BlueUpServiceClient "+blueNode.getName()+" ";
        this.blueNodePhAddress = blueNode.getPhaddress();    	
    	this.upPort = upPort;        
    }
    
    public int getUpport() {
        return upPort;
    }
    
    public BlueNodeInstance getBlueNode() {
		return blueNode;
	}
    
    public boolean isKilled() {
		return kill.get();
	}

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED AT " + Thread.currentThread().getName() + " ON PORT " + upPort);

        byte[] data = new byte[2048];
        clientSocket = null;
        try {
            clientSocket = new DatagramSocket();
        } catch (java.net.BindException ex) {
            App.bn.ConsolePrint(pre + "PORT ALLREADY IN USE, EXITING");
            return;
        } catch (SocketException ex) {
            Logger.getLogger(RedReceive.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        byte[] sendData = "FISH PACKET".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, blueNodePhAddress, upPort);
        try {
            for (int i = 0; i < 3; i++) {
                clientSocket.send(sendPacket);
            }
        } catch (java.net.SocketException ex1) {
            App.bn.TrafficPrint("FISH PACKET SEND ERROR",3,1);
            return;
        } catch (IOException ex) {
            App.bn.TrafficPrint("FISH PACKET SEND ERROR",3,1);
            Logger.getLogger(BlueReceiveClient.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        App.bn.TrafficPrint("FISH PACKET",3,1);

        while (!kill.get()) {
        	DatagramPacket receivePacket = new DatagramPacket(data, data.length);
            try {
                clientSocket.receive(receivePacket);
                int len = receivePacket.getLength();
                if (len > 0 && len <= 1500) {
                    byte[] packet = new byte[len];
                    System.arraycopy(receivePacket.getData(), 0, packet, 0, len);
                    if (App.bn.gui && !didTrigger) {
                    	didTrigger = true;
                    	MainWindow.jCheckBox7.setSelected(true);                        
                    }
                    
                    if (UnityPacket.isUnity(packet)) {
    					if (UnityPacket.isKeepAlive(packet)) {
    						// keep alive
    						App.bn.TrafficPrint(pre +"KEEP ALIVE RECEIVED", 0, 1);
    					} else if (UnityPacket.isUping(packet)) {
                            //blue node uping!
    						blueNode.setUping(true);    
    						App.bn.TrafficPrint(pre + "UPING RECEIVED", 1, 1);
                        } else if (UnityPacket.isDping(packet)) {
    						// blue node dping!
                        	blueNode.setDping(true);   
    						App.bn.TrafficPrint(pre + "DPING RECEIVED", 1, 1);
    					} else if (UnityPacket.isAck(packet)) {
    						try {
    							App.bn.manager.offer(packet); 
    							App.bn.TrafficPrint(pre + "ACK -> "+UnityPacket.getDestAddress(packet)+" RECEIVED", 2, 1);
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
    					} else if (UnityPacket.isMessage(packet)) {
    						App.bn.manager.offer(packet); 
    						App.bn.TrafficPrint(pre + "MESSAGE RECEIVED", 3, 1);
    					}        				
                    } else if (IPv4Packet.isIPv4(packet)){
                        App.bn.manager.offer(packet);                        
                    } else {
                    	System.out.println(pre + "wrong type");
                    }
                } else {
                    System.out.println(pre + "wrong length");
                }
            } catch (java.net.SocketException ex1) {
                App.bn.ConsolePrint(pre + "SOCKET ERROR");
            } catch (IOException ex) {
                App.bn.ConsolePrint(pre + "IO ERROR");
            }
        }
        blueNode.getQueueMan().clear();
        App.bn.ConsolePrint(pre + "ENDED");        
    }

    public void kill() {
        kill.set(true);
        clientSocket.close();
    }    
}
