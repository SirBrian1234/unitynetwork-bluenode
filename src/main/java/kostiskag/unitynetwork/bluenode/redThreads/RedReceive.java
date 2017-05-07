package kostiskag.unitynetwork.bluenode.redThreads;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.packets.IPv4Packet;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.gui.MainWindow;
import kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;

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
    private final LocalRedNodeInstance rn;
    //socket
    private DatagramSocket serverSocket;
    private int clientPort;
    private int serverPort;
    //triggers
    private Boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);

    public RedReceive(LocalRedNodeInstance rn) {
        this.rn = rn;
    	pre =  "^RedReceive "+rn.getHostname()+" ";
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
            App.bn.ConsolePrint(pre + "PORT ALLREADY IN USE, EXITING");
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
                            App.bn.TrafficPrint(pre +"KEEP ALIVE RECEIVED" ,0,0);
                        }  else if (UnityPacket.isUping(packet)){
                        	//rednode uping packet received by the aspect of the red node
                        	//the red node tests its upload
                            rn.setUPing(true);
                            App.bn.TrafficPrint(pre + "UPING RECEIVED",1,0);
                        } else if (UnityPacket.isShortRoutedAck(packet)) {
                        	//you should not do something
                        	App.bn.TrafficPrint(pre + "SHORT ACK RECEIVED", 1, 0);
                        } else if (UnityPacket.isLongRoutedAck(packet)){
                        	try {
								App.bn.TrafficPrint(pre + "ACK -> "+UnityPacket.getDestAddress(packet).getHostAddress()+" RECEIVED" ,2,0);
								//now you have control over the buffer
								// do stuff here
								rn.getReceiveQueue().offer(packet);
							} catch (Exception e) {
								e.printStackTrace();
							}
                        } else if (UnityPacket.isMessage(packet)) {
                        	try {
								App.bn.TrafficPrint(pre + "Message -> "+UnityPacket.getDestAddress(packet).getHostAddress()+" RECEIVED" ,2,0);
								//now you have controll over the buffer
								// do stuff here
								rn.getReceiveQueue().offer(packet);
								
								//build and offer a short routed ack
								byte[] ACKS = UnityPacket.buildShortRoutedAckPacket(rn.getReceiveQueue().getlen());
								rn.getSendQueue().offer(ACKS);
								
                        	} catch (Exception e) {
								e.printStackTrace();
							}
                        }                                                    
                    } else if (IPv4Packet.isIPv4(packet)){             
                        App.bn.TrafficPrint(pre + "IPv4",3,0);
                        //now you have control over the buffer
						// do stuff here
						rn.getReceiveQueue().offer(packet);   
						
						//build and offer a short routed ack
						byte[] ACKS = UnityPacket.buildShortRoutedAckPacket(rn.getReceiveQueue().getlen());
						rn.getSendQueue().offer(ACKS);
                    }
                    if (App.bn.gui && !didTrigger) {
                        MainWindow.jCheckBox3.setSelected(true);
                        didTrigger = true;
                    }
                }
            } catch (java.net.SocketException ex1) {
                break;
            } catch (IOException ex) {
            	App.bn.ConsolePrint(pre + "IO ERROR");
                ex.printStackTrace();
                break;
            }
        }               
        App.bn.UDPports.releasePort(clientPort);   
        rn.getReceiveQueue().clear();
        App.bn.ConsolePrint(pre + "ENDED");
    }

    public void kill() {
        kill.set(true);
        serverSocket.close();
    }
}