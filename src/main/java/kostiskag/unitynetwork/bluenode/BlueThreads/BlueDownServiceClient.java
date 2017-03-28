package kostiskag.unitynetwork.bluenode.BlueThreads;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.*;
import kostiskag.unitynetwork.bluenode.RedThreads.RedlUpService;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;

/**
 *
 * @author kostis
 * 
 */
public class BlueDownServiceClient extends Thread{
	//remember = BDSClient is a client to BNS UP so he SENDS
    private String pre = "^DownClient (UP) ";
    private boolean kill = false;
    
    private static Boolean trigger = false;    
    
    private int downport;
    private int sourcePort;
    private String hostname;
    
    private DatagramPacket sendUDPPacket;
    private DatagramSocket serverSocket = null;
    private InetAddress BlueNodeAddress;
    private byte[] data;
    private DatagramPacket receivedUDPPacket;
    private int destPort;

    /*
     * First the class must find all the valuable information to open the socket
     * we do this on the constructor so that the running time will be charged on
     * the AuthService Thread
     */
    
    public BlueDownServiceClient(String hostname, int downport, String vaddress) {
        this.hostname = hostname;
        this.downport = downport;  
        pre = pre + hostname + " ";
        try {
            BlueNodeAddress = InetAddress.getByName(vaddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(BlueUpServiceServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        App.ConsolePrint(pre + "STARTED FOR " + hostname + " AT " + Thread.currentThread().getName()+ " ON PORT "+downport);        
        
        try {
            serverSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(RedlUpService.class.getName()).log(Level.SEVERE, null, ex);
        }                              
        
        while (!kill) {            
            
            try {
                data = App.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getQueueMan().poll();
            } catch (java.lang.NullPointerException ex1){
                continue;
            } catch (java.util.NoSuchElementException ex) {
                continue;
            }            
            sendUDPPacket = new DatagramPacket(data, data.length, BlueNodeAddress, downport);
            //BlueNode.lvl3BlueNode.BlueNodesTable.getBlueNodeAddress(hostname).getTrafficMan().clearToSend();
            try {
                serverSocket.send(sendUDPPacket);
                String version = IpPacket.getVersion(data);
                if (version.equals("0")) {
                    byte[] payload = IpPacket.getPayloadU(data);
                    String receivedMessage = new String(payload);
                    String args[] = receivedMessage.split("\\s+");
                    if (args.length > 1) {
                        if (args[0].equals("00000")) {
                            //keep alive
                            App.TrafficPrint(pre + version + " " + "[KEEP ALIVE]", 0, 1);
                        } else if (args[0].equals("00002")) {
                            //le wild blue node uping!
                            App.BlueNodesTable.getBlueNodeInstanceByHn(hostname).setUping(true);
                            App.TrafficPrint(pre + "LE WILD RN UPING LEAVES", 1, 1);
                        } else if (args[0].equals("00003")) {
                            //le wild blue node dping!
                            App.dping = true;
                            App.TrafficPrint(pre + "LE WILD RN DPING LEAVES", 1, 1);
                        }
                    }
                }                  
                if (App.gui && trigger == false) {
                    MainWindow.jCheckBox6.setSelected(true);
                    trigger = true;
                }
            } catch (java.net.SocketException ex1) {
                App.ConsolePrint(pre + " SOCKET DIED FOR " + hostname);
            } catch (IOException ex) {                
                App.ConsolePrint(pre + "SOCKET ERROR FOR " + hostname);
            }
        }
        App.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getQueueMan().clear();
        App.UDPports.releasePort(sourcePort);  
        App.ConsolePrint(pre + "ENDED FOR " + hostname);        
    }

    public void kill() {
        kill = true;               
        serverSocket.close();             
    }

    public int getDownport() {        
        return downport;
    }        
}
