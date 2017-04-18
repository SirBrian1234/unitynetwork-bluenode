package kostiskag.unitynetwork.bluenode.redThreads;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
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
public class RedDownService extends Thread {

    private final String pre;
    private final LocalRedNodeInstance rn;
    //socket
    private DatagramSocket serverSocket;
    private int sourcePort;
    private int destPort;
    //triggers
    private Boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);

    public RedDownService(LocalRedNodeInstance rn ) {
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
                    if (App.bn.gui && !didTrigger) {
                        MainWindow.jCheckBox3.setSelected(true);
                        didTrigger = true;
                    }
                    String version = IpPacket.getVersion(data);
                    if (version.equals("0")) {                        
                        byte[] payload = IpPacket.getPayloadU(data);
                        String receivedMessage = new String(payload);
                        String args[] = receivedMessage.split("\\s+");
                        if (args.length > 1) {                            
                            if (args[0].equals("00000")){
                                //keep alive packet received
                                App.bn.TrafficPrint(pre + version+" [KEEP ALIVE]" ,0,0);
                            }  else if (args[0].equals("00001")) {
                                //rednode uping packet received                                              
                                rn.setUPing(true);
                                App.bn.TrafficPrint(pre + "REDNODE UPING RECEIVED",1,0);
                            } 
                        } else {
                        	App.bn.TrafficPrint(pre + "WRONG LENGTH",1,0);
                        }
                    } else if (version.equals("1")) {                        
                        byte[] payload = IpPacket.getPayloadU(data);
                        String receivedMessage = new String(payload);
                        String args[] = receivedMessage.split("\\s+");
                        if (args.length > 1) {                            
                            if (args[0].equals("00004")){
                                //ack
                                App.bn.TrafficPrint(pre + "[ACK] -> "+IpPacket.getUDestAddress(data).getHostAddress() ,2,0);
                                App.bn.manager.offer(data); 
                            }  
                        } else {
                        	App.bn.TrafficPrint(pre + "WRONG LENGTH",2,0);
                        }
                    } else {             
                        App.bn.TrafficPrint(pre + "IPv4",3,0);
                        App.bn.manager.offer(data);                        
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