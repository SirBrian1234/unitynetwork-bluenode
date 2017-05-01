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
 * @author kostis
 */
public class RedReceive extends Thread {

    private final String pre;
    private final LocalRedNodeInstance rn;
    //socket
    private DatagramSocket serverSocket;
    private int sourcePort;
    private int destPort;
    //triggers
    private Boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);

    public RedReceive(LocalRedNodeInstance rn ) {
        this.rn = rn;
    	pre =  "^RedDownService "+rn.getHostname()+" ";
    	destPort = App.bn.UDPports.requestPort();        
    }

    public int getDestPort() {
        return destPort;
    }        
    
    public int getSourcePort() {
		return sourcePort;
	}
    
    public LocalRedNodeInstance getRn() {
		return rn;
	}
    
    public boolean getIsKilled() {
    	return kill.get();
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED AT " + Thread.currentThread().getName() + " ON PORT " + destPort);
     
        try {
            serverSocket = new DatagramSocket(destPort);
        } catch (java.net.BindException ex) {
            App.bn.ConsolePrint(pre + "PORT ALLREADY IN USE, EXITING");
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        byte[] receiveData = new byte[2048];
        byte[] data = null;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        while (!kill.get()) {
            try {
                serverSocket.receive(receivePacket);
                int len = receivePacket.getLength();
                if (len > 0 && len <= 1500) {
                    data = new byte[len];
                    System.arraycopy(receivePacket.getData(), 0, data, 0, len);                    
                    if (UnityPacket.isUnity(data)) {                                                
                    	if (UnityPacket.isKeepAlive(data)) {
                            //keep alive packet received
                            App.bn.TrafficPrint(pre +"KEEP ALIVE RECEIVED" ,0,0);
                        }  else if (UnityPacket.isUping(data)){
                        	//rednode uping packet received by the aspect of the red node
                        	//the red node tests its upload
                            rn.setUPing(true);
                            App.bn.TrafficPrint(pre + "UPING RECEIVED",1,0);
                        } else if (UnityPacket.isAck(data)){
                        	try {
								App.bn.TrafficPrint(pre + "ACK ->"+UnityPacket.getDestAddress(data).getHostAddress()+" RECEIVED" ,2,0);
								App.bn.manager.offer(data); 
							} catch (Exception e) {
								e.printStackTrace();
							}
                        } else if (UnityPacket.isMessage(data)) {
                        	try {
								App.bn.TrafficPrint(pre + "Message -> "+UnityPacket.getDestAddress(data).getHostAddress()+" RECEIVED" ,2,0);
								App.bn.manager.offer(data);
                        	} catch (Exception e) {
								e.printStackTrace();
							}
                        }                                                    
                    } else if (IPv4Packet.isIPv4(data)){             
                        App.bn.TrafficPrint(pre + "IPv4",3,0);
                        App.bn.manager.offer(data);                        
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
        App.bn.UDPports.releasePort(sourcePort);        
        App.bn.ConsolePrint(pre + "ENDED");
    }

    public void kill() {
        kill.set(true);
        serverSocket.close();
    }
}