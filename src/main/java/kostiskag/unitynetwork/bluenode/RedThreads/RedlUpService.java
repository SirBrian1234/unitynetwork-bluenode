/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.RedThreads;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 *
 * This service runs for every user it opens a UDP socket and waits a row to
 * fill then it sends the packets
 */
public class RedlUpService extends Thread {

    private String pre = "^UpService   ";
    private boolean kill = false;
    private static Boolean trigger = false;
    private int destPort;
    private int sourcePort;
    private String vaddress;
    private String serverStr;
    private byte[] buffer = new byte[2048];
    private byte[] data = null;
    private DatagramPacket receivedUDPPacket;
    private DatagramPacket sendUDPPacket;
    private String clientStr;
    private DatagramSocket serverSocket = null;
    private InetAddress clientAddress;

    /*
     * HERE IS A BIG BUG, IF FISH NEVER FISHES THEN EVERYTHING IS STUCK AND WE
     * HAVE A DEAD ENTRY
     *
     */
    /*
     * First the class must find all the valuable information to open the socket
     * we do this on the constructor so that the running time will be charged on
     * the AuthService Thread
     */
    public RedlUpService(String vaddress) {
        this.vaddress = vaddress;
        sourcePort = lvl3BlueNode.UDPports.requestPort();
        pre = pre + vaddress+" ";
    }

    @Override
    public void run() {
        lvl3BlueNode.ConsolePrint(pre + "STARTED FOR " + vaddress + " AT " + Thread.currentThread().getName() + " ON PORT " + sourcePort);        
        
        try {
            serverSocket = new DatagramSocket(sourcePort);
        } catch (java.net.BindException ex) {
            lvl3BlueNode.ConsolePrint(pre + "PORT ALLREADY BINDED, EXITING");
            return;
        } catch (SocketException ex) {
            Logger.getLogger(RedlUpService.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            serverSocket.setSoTimeout(10000);
        } catch (SocketException ex) {
            Logger.getLogger(RedlUpService.class.getName()).log(Level.SEVERE, null, ex);
        }

        receivedUDPPacket = new DatagramPacket(buffer, buffer.length);
        try {
            serverSocket.receive(receivedUDPPacket);
        } catch (java.net.SocketTimeoutException ex) {
            lvl3BlueNode.ConsolePrint(pre + "FISH SOCKET TIMEOUT");
            return;
        } catch (java.net.SocketException ex) {
            lvl3BlueNode.ConsolePrint(pre + "FISH SOCKET CLOSED, EXITING");
            return;
        } catch (IOException ex) {
            Logger.getLogger(RedlUpService.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        clientAddress = receivedUDPPacket.getAddress();
        destPort = receivedUDPPacket.getPort();

        /*
         * Now a difficult task approaches as we have to create a lifo queue and
         * a while to get packets from lifo and send them to the socket
         *
         */

        while (!kill) {
            data = null;
            try {
                data = lvl3BlueNode.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).getQueueMan().poll();
            } catch (java.lang.NullPointerException ex1) {
                continue;
            } catch (java.util.NoSuchElementException ex) {
                continue;
            }

            if (data.length <= 0 || data.length > 1500) {
                System.out.println(pre + "wrong length");
                continue;
            }

            sendUDPPacket = new DatagramPacket(data, data.length, clientAddress, destPort);
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
                            lvl3BlueNode.TrafficPrint(pre + version + " " + "[KEEP ALIVE]", 0, 0);
                        } else if (args[0].equals("00001")) {
                            //le wild rednode ping!                                               
                            lvl3BlueNode.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).setUPing(true);
                            lvl3BlueNode.TrafficPrint(pre + "LE WILD RN DPING LEAVES", 1, 0);
                        }
                    } else {
                        System.out.println(pre + "wrong length");
                    }
                } 
                if (lvl3BlueNode.gui && trigger == false) {
                    MainWindow.jCheckBox4.setSelected(true);
                    trigger = true;
                }
            } catch (java.net.SocketException ex1) {
                lvl3BlueNode.ConsolePrint(pre + " SOCKET DIED FOR " + vaddress);
            } catch (IOException ex) {
                Logger.getLogger(RedlUpService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lvl3BlueNode.ConsolePrint(pre + " ENDED FOR " + vaddress);        
        lvl3BlueNode.UDPports.releasePort(sourcePort);
        sourcePort = -1;
    }

    public void kill() {
        kill = true;
        serverSocket.close();        
        lvl3BlueNode.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).getQueueMan().clear();
    }
    
    public int getUpport() {
        return sourcePort;
    }
}
