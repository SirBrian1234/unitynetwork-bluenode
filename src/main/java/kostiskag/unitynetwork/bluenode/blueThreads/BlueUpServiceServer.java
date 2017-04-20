package kostiskag.unitynetwork.bluenode.blueThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.*;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.redThreads.RedlUpService;

/**
 *
 * @author kostis
 */
public class BlueUpServiceServer extends Thread {

    private final String pre;
    private final BlueNodeInstance blueNode;
    private final int upPort;
    private int destPort;
    private InetAddress blueNodePhAddress;
    private DatagramSocket serverSocket;    
    private boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);  

    /**
     * First the class must find all the valuable information to open the socket
     * we do this on the constructor so that the running time will be charged on
     * the AuthService Thread
     */
    public BlueUpServiceServer(BlueNodeInstance blueNode) {    	
    	this.blueNode = blueNode;
    	this.pre = "^BlueUpServiceServer "+blueNode.getName()+" ";
        this.blueNodePhAddress = blueNode.getPhaddress();
        this.upPort = App.bn.UDPports.requestPort();
    }
    
    public int getUpport() {
        return upPort;
    }    
    
    public int getDestPort() {
		return destPort;
	}
    
    public BlueNodeInstance getBlueNode() {
		return blueNode;
	}
    
    public boolean getIsKilled() {
    	return kill.get();
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED AT " + Thread.currentThread().getName() + " ON PORT " + upPort);
        App.bn.ConsolePrint("up service server " + upPort);

        try {
            serverSocket = new DatagramSocket(upPort);
        } catch (SocketException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            serverSocket.setSoTimeout(10000);
        } catch (SocketException ex) {
            ex.printStackTrace();
            return;
        }

        byte[] data = new byte[2048];
        DatagramPacket receivedUDPPacket = new DatagramPacket(data, data.length);
        try {
            serverSocket.receive(receivedUDPPacket);
        } catch (java.net.SocketTimeoutException ex) {
            App.bn.ConsolePrint(pre + "FISH SOCKET TIMEOUT");
            return;
        } catch (java.net.SocketException ex) {
            App.bn.ConsolePrint(pre + "FISH SOCKET CLOSED, EXITING");
            return;
        } catch (IOException ex) {
            Logger.getLogger(RedlUpService.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        destPort = receivedUDPPacket.getPort();
        blueNodePhAddress = receivedUDPPacket.getAddress();

        while (!kill.get()) {
        	
            try {
                data = blueNode.getQueueMan().poll();
            } catch (java.lang.NullPointerException ex1) {
                continue;
            } catch (java.util.NoSuchElementException ex) {
                continue;
            } catch (Exception e) {
            	e.printStackTrace();
            	break;
            }

            DatagramPacket sendUDPPacket = new DatagramPacket(data, data.length, blueNodePhAddress, destPort);
            try {
                serverSocket.send(sendUDPPacket);
                String version = IpPacket.getVersion(data);
                if (version.equals("0")) {
                    byte[] payload = IpPacket.getPayloadU(data);
                    String sentMessage = new String(payload);
                    String args[] = sentMessage.split("\\s+");
                    if (args.length > 1) {
                        if (args[0].equals("00000")) {
                            //keep alive
                            App.bn.TrafficPrint(pre+sentMessage, 0, 1);
                        } else if (args[0].equals("00002")) {
                            //blue node uping!
                            blueNode.setUping(true);
                            App.bn.TrafficPrint(pre + "UPING SENT", 1, 1);
                        } else if (args[0].equals("00003")) {
                            //blue node dping!
                        	blueNode.setDping(true);   
                            App.bn.TrafficPrint(pre + "DPING SENT", 1, 1);
                        } 
                    }
                }
                if (App.bn.gui && !didTrigger) {
                	didTrigger = true;
                	MainWindow.jCheckBox6.setSelected(true);                    
                }
            } catch (java.net.SocketException ex1) {
                App.bn.ConsolePrint(pre + " SOCKET ERROR");
                break;
            } catch (IOException ex) {
                App.bn.ConsolePrint(pre + "IO ERROR");
                break;
            }
        }
        App.bn.UDPports.releasePort(upPort);
        App.bn.ConsolePrint(pre + "ENDED");
    }

    public void kill() {
        kill.set(true);
        serverSocket.close();
    }         
}
