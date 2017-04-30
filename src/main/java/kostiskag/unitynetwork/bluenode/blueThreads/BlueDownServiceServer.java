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
public class BlueDownServiceServer extends Thread {

    private final String pre;
    private final BlueNodeInstance blueNode;
    private final int downPort;
    private DatagramSocket serverSocket;
    private Boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);    

    public BlueDownServiceServer(BlueNodeInstance blueNode) {
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
                    String version = IPv4Packet.getVersion(packet);
                    if (version.equals("0")) {                        
                        byte[] payload = UnityPacket.getPayload(packet);
                        String receivedMessage = new String(payload);
                        String args[] = receivedMessage.split("\\s+");
                        if (args.length > 1) {                                                        
                            if (args[0].equals("00000")) {
                                //keep alive
                                App.bn.TrafficPrint(pre + receivedMessage,0,1);
                            } else if (args[0].equals("00002")) {
                                //blue node dping!
                            	blueNode.setUping(true);                            
                                App.bn.TrafficPrint(pre + "UPING RECEIVED",1,1);
                            } else if (args[0].equals("00003")) {
                                //blue node dping!
                            	blueNode.setDping(true);                            
                                App.bn.TrafficPrint(pre + "DPING RECEIVED",1,1);
                            } else if (args[0].equals("00004")) {
                                //ack
                                App.bn.TrafficPrint(pre + version + " " + receivedMessage,2,1);
                            }
                        }                        
                    } else {
                        App.bn.manager.offer(packet);                        
                    }
                } else {
                    System.out.println(pre + "WRONG LENGTH");
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
