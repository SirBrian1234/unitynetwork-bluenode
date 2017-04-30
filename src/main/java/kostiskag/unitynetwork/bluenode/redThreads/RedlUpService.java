package kostiskag.unitynetwork.bluenode.redThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.packets.IPv4Packet;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.gui.MainWindow;
import kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;

/**
 * This service runs for every user it opens a UDP socket and waits a row to
 * fill then it sends the packets
 * 
 * @author kostis
 */
public class RedlUpService extends Thread {

    private final String pre;
    private final LocalRedNodeInstance rn;
    //socket    
    private int destPort;
    private int sourcePort;
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
    public RedlUpService(LocalRedNodeInstance rn) {        
        this.rn = rn;
        pre = "^RedlUpService "+rn.getHostname()+" ";
        sourcePort = App.bn.UDPports.requestPort();
    }
    
    public int getSourcePort() {
        return sourcePort;
    }
    
    public int getDestPort() {
		return destPort;
	}
    
    public LocalRedNodeInstance getRn() {
		return rn;
	}
    
    public boolean getIsKilled() {
    	return kill.get();
    }
    
    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED AT " + Thread.currentThread().getName() + " ON PORT " + sourcePort);        
        
        try {
            serverSocket = new DatagramSocket(sourcePort);
        } catch (java.net.BindException ex) {
            App.bn.ConsolePrint(pre + "PORT ALLREADY BINDED, EXITING");
            return;
        } catch (SocketException ex) {
            Logger.getLogger(RedlUpService.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            serverSocket.setSoTimeout(10000);
        } catch (SocketException ex) {
            Logger.getLogger(RedlUpService.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(RedlUpService.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        InetAddress clientAddress = receivedUDPPacket.getAddress();
        destPort = receivedUDPPacket.getPort();

        /*
         * The task at hand is to use a synchronized packet queue and
         * a while to get the packets from the queue and send them to the socket.
         * In other words when there is a packet on queue write on socket.
         */
        while (!kill.get()) {
            byte[] data = null;
            try {
            	data = rn.getQueueMan().poll();                   
            } catch (Exception ex1) {
                continue;
            }
            
            if (kill.get()) {
            	break;
            } else if (data == null) {
            	continue;
            } else if (data.length == 0) {
            	continue;
            } else if (data.length > 1500) {
            	App.bn.TrafficPrint("Throwing oversized packet of size "+data.length, 3, 0);
                continue;
            }

            DatagramPacket sendUDPPacket = new DatagramPacket(data, data.length, clientAddress, destPort);
            try {
                serverSocket.send(sendUDPPacket);
                String version = IPv4Packet.getVersion(data);                
                if (version.equals("0")) {
                    byte[] payload = UnityPacket.getPayload(data);
                    String receivedMessage = new String(payload);
                    String args[] = receivedMessage.split("\\s+");
                    if (args.length > 1) {
                        if (args[0].equals("00000")) {
                            //keep alive
                            App.bn.TrafficPrint(pre+version +" [KEEP ALIVE]", 0, 0);
                        } else if (args[0].equals("00001")) {
                            //rednode ping                                               
                            rn.setUPing(true);
                            App.bn.TrafficPrint(pre + "DPING LEAVES", 1, 0);
                        }
                    } else {
                    	App.bn.TrafficPrint(pre + "WRONG LENGTH", 1, 0);                        
                    }
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
        App.bn.UDPports.releasePort(sourcePort);
        App.bn.ConsolePrint(pre + "ENDED");                
    }

    public void kill() {
        kill.set(true);
        serverSocket.close();
        rn.getQueueMan().exit();
    }       
}
