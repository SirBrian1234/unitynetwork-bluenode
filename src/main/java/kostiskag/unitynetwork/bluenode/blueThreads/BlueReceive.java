package kostiskag.unitynetwork.bluenode.blueThreads;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import kostiskag.unitynetwork.bluenode.gui.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.packets.IPv4Packet;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author kostis
 */
public class BlueReceiveServer extends Thread {

    private final String pre;
    private final BlueNodeInstance blueNode;
    private final int downPort;
    private DatagramSocket serverSocket;
    private Boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);    

    public BlueReceiveServer(BlueNodeInstance blueNode) {
        this.downPort = App.bn.UDPports.requestPort();
        this.blueNode = blueNode;
        this.pre = "^BlueDownServiceServer "+blueNode.getName()+" ";        
    }
    
    public int getDownport() {
        return downPort;
    }   

    public BlueNodeInstance getBlueNode() {
		return blueNode;
	}
    
    public boolean isKilled() {
		return kill.get();
	}
    
    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED AT " + Thread.currentThread().getName() + " ON PORT " + downPort);       
        
        byte[] receiveData = new byte[2048];
        serverSocket = null;
        try {
            serverSocket = new DatagramSocket(downPort);
        } catch (java.net.BindException ex) {
            ex.printStackTrace();
            return;
        } catch (SocketException ex) {
            ex.printStackTrace();
            return;
        }

        while (!kill.get()) {
        	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
                int len = receivePacket.getLength();
                if (len > 0 && len <= 1500) {
                    byte[] packet = new byte[len];
                    System.arraycopy(receivePacket.getData(), 0, packet, 0, len);
                    if (App.bn.gui && !didTrigger) {
                        MainWindow.jCheckBox7.setSelected(true);
                        didTrigger = true;
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
    							App.bn.TrafficPrint(pre + "ACK-> "+UnityPacket.getDestAddress(packet)+" RECEIVED", 2, 1);
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
    					} else if (UnityPacket.isMessage(packet)) {
    						App.bn.manager.offer(packet); 
    						App.bn.TrafficPrint(pre + "MESSAGE RECEIVED", 3, 1);
    					}        				
                    } else if (IPv4Packet.isIPv4(packet)){
                        App.bn.manager.offer(packet);                        
                    }
                    
                } else {
                    System.out.println(pre + "MAXIMUM LENGTH EXCEDED");
                }
            } catch (java.net.SocketException ex1) {
                App.bn.ConsolePrint(pre + "SOCKET ERROR");
            } catch (IOException ex) {
                App.bn.ConsolePrint(pre + "IO ERROR");
            }
        }
        App.bn.UDPports.releasePort(downPort);
        App.bn.ConsolePrint(pre + "ENDED");        
    }

    public void kill() {
    	kill.set(true);
    	serverSocket.close();             
    }        
}
