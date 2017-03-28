/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.BlueThreads;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.RedThreads.RedDownService;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import kostiskag.unitynetwork.bluenode.Functions.Packet;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class BlueUpServiceClient extends Thread {

    private String pre = "^UpClient (DOWN) ";
    private boolean kill = false;
    private static Boolean trigger = false;
    private int upPort;    
    private String hostname;    
    private DatagramSocket clientSocket = null;
    private InetAddress BlueNodeAddress;    
    private byte[] receiveData;
    private DatagramPacket receivePacket;
    private byte[] sendData;
    private boolean didTrigger;

    /*
     * First the class must find all the valuable information to open the socket
     * we do this on the constructor so that the running time will be charged on
     * the AuthService Thread
     */
    public BlueUpServiceClient(String hostname, int upPort, String vaddress) {
        this.hostname = hostname;
        this.upPort = upPort;
        pre = pre + hostname + " ";
        try {
            BlueNodeAddress = InetAddress.getByName(vaddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(BlueUpServiceClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        lvl3BlueNode.ConsolePrint(pre + "STARTED FOR " + hostname + " AT " + Thread.currentThread().getName() + " ON PORT " + upPort);

        receiveData = new byte[2048];
        clientSocket = null;

        try {
            clientSocket = new DatagramSocket();
        } catch (java.net.BindException ex) {
            lvl3BlueNode.ConsolePrint(pre + "PORT ALLREADY IN USE, EXITING");
        } catch (SocketException ex) {
            Logger.getLogger(RedDownService.class.getName()).log(Level.SEVERE, null, ex);
        }

        sendData = "FISH PACKET".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, BlueNodeAddress, upPort);
        try {
            for (int i = 0; i < 3; i++) {
                clientSocket.send(sendPacket);
            }
        } catch (java.net.SocketException ex1) {
            lvl3BlueNode.TrafficPrint("FISH PACKET SEND ERROR",3,1);
            return;
        } catch (IOException ex) {
            lvl3BlueNode.TrafficPrint("FISH PACKET SEND ERROR",3,1);
            Logger.getLogger(BlueUpServiceClient.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        lvl3BlueNode.TrafficPrint("FISH PACKET",3,1);

        while (!kill) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                clientSocket.receive(receivePacket);
                int len = receivePacket.getLength();
                if (len > 0 && len <= 1500) {
                    byte[] packet = new byte[len];
                    System.arraycopy(receivePacket.getData(), 0, packet, 0, len);
                    if (lvl3BlueNode.gui && didTrigger == false) {
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
                                lvl3BlueNode.TrafficPrint(pre + version + " " +"[KEEP ALIVE]",0,1);
                            } else if (args[0].equals("00002")) {
                                //le wild blue node uping!
                                lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(hostname).setUping(true);                                
                                lvl3BlueNode.TrafficPrint(pre + "LE WILD BN UPING FROM " + hostname + " APPEARS",1,1);
                            } else if (args[0].equals("00003")) {
                                //le wild blue node dping!
                                lvl3BlueNode.dping = true;                                
                                lvl3BlueNode.TrafficPrint(pre + "LE WILD DPING FROM " + hostname + " APPEARS",1,1);
                            } 
                        }
                    } else {
                        lvl3BlueNode.manager.offer(packet);                        
                    }
                } else {
                    System.out.println(pre + "wrong length");
                }
            } catch (java.net.SocketException ex1) {
                lvl3BlueNode.ConsolePrint(pre + " SOCKET CLOSED FOR " + hostname);
            } catch (IOException ex) {
                lvl3BlueNode.ConsolePrint(pre + " SOCKET ERROR FOR " + hostname);
            }
        }
        lvl3BlueNode.ConsolePrint(pre + " ENDED FOR " + hostname);
        if (lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(hostname)!=null) {
            lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getQueueMan().clear();
        }
    }

    public void kill() {
        kill = true;
        clientSocket.close();
    }

    public int getUpport() {
        return upPort;
    }
}
