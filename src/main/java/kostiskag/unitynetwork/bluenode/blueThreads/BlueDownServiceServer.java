package kostiskag.unitynetwork.bluenode.blueThreads;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import kostiskag.unitynetwork.bluenode.redThreads.RedDownService;
import kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author kostis
 */
public class BlueDownServiceServer extends Thread {

    private String pre = "^DownService (DOWN) ";
    private Boolean kill = false;
    private static Boolean didTrigger = false;
    private int downport;
    private byte[] receiveData;
    private DatagramSocket serverSocket;
    private DatagramPacket receivePacket;
    private String hostname;        

    public BlueDownServiceServer(String hostname) {
        downport = App.bn.UDPports.requestPort();
        this.hostname = hostname;        
        pre = pre + hostname + " ";        
    }    

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED FOR " + hostname + " AT " + Thread.currentThread().getName() + " ON PORT " + downport);       
        
        receiveData = new byte[2048];
        serverSocket = null;

        try {
            serverSocket = new DatagramSocket(downport);
        } catch (java.net.BindException ex) {
            App.bn.ConsolePrint(pre + "PORT ALLREADY IN USE, EXITING");
        } catch (SocketException ex) {
            Logger.getLogger(RedDownService.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (!kill) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
                int len = receivePacket.getLength();
                if (len > 0 && len <= 1500) {
                    byte[] packet = new byte[len];
                    System.arraycopy(receivePacket.getData(), 0, packet, 0, len);
                    if (App.bn.gui && didTrigger == false) {
                        MainWindow.jCheckBox7.setSelected(true);
                        didTrigger = true;
                    }
                    String version = IpPacket.getVersion(packet);
                    if (version.equals("0")) {                        
                        byte[] payload = IpPacket.getPayloadU(packet);
                        String receivedMessage = new String(payload);
                        String args[] = receivedMessage.split("\\s+");
                        if (args.length > 1) {                                                        
                            if (args[0].equals("00000")) {
                                //keep alive
                                App.bn.TrafficPrint(pre + version + " " +"[KEEP ALIVE]",0,1);
                            } else if (args[0].equals("00002")) {
                                //le wild blue node uping!
                                App.bn.blueNodesTable.getBlueNodeInstanceByHn(hostname).setUping(true);                                
                                App.bn.TrafficPrint(pre + "LE WILD BN UPING FROM " + hostname + " APPEARS",1,1);                                
                            } else if (args[0].equals("00003")) {
                                //le wild blue node dping!
                                App.bn.dping = true;                                
                                App.bn.TrafficPrint(pre + "LE WILD DPING FROM " + hostname + " APPEARS",1,1);
                            } else if (args[0].equals("00004")) {
                                //ack
                                App.bn.TrafficPrint(pre + version + " " + receivedMessage,2,1);
                                //BlueNode.lvl3BlueNode.BlueNodesTable.getBlueNodeAddress(hostname).getTrafficMan().gotACK();                               
                            }
                        }                        
                    } else {
                        App.bn.manager.offer(packet);                        
                    }
                } else {
                    System.out.println(pre + "wrong length");
                }
            } catch (java.net.SocketException ex1) {
                App.bn.ConsolePrint(pre + " SOCKET CLOSED FOR " + hostname);
            } catch (IOException ex) {
                App.bn.ConsolePrint(pre + " SOCKET ERROR FOR " + hostname);
            }
        }
        App.bn.ConsolePrint(pre + " ENDED FOR " + hostname);
        App.bn.UDPports.releasePort(downport);
    }

    public void kill() {
        kill = true;
        serverSocket.close();
        downport = -1;
    }        

    public int getDownport() {
        return downport;
    }   
}
