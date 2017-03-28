package kostiskag.unitynetwork.bluenode.RedThreads;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * down service listens for virtual packets then sends them to the target
 * specified by viewing the table if it fails to find the target the packet is
 * discarded
 *
 * down service runs differently for every rednode and every assosiated blue
 * node
 */
public class RedDownService extends Thread {

    private String pre = "^DownService ";
    private Boolean kill = false;
    private static Boolean didTrigger = false;
    private byte[] data;
    private DatagramSocket serverSocket = null;
    private DatagramPacket receivePacket;
    private int portToUse;
    private String vaddress;
    private int destPort;    

    public RedDownService(String vaddress) {
        this.vaddress = vaddress;
        destPort = lvl3BlueNode.UDPports.requestPort();
        pre = pre + vaddress + " ";
    }

    @Override
    public void run() {
        lvl3BlueNode.ConsolePrint(pre + "STARTED FOR " + vaddress + " AT " + Thread.currentThread().getName() + " ON PORT " + destPort);
     
        try {
            serverSocket = new DatagramSocket(destPort);
        } catch (java.net.BindException ex) {
            lvl3BlueNode.ConsolePrint(pre + "PORT ALLREADY IN USE, EXITING");
        } catch (SocketException ex) {
            Logger.getLogger(RedDownService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RedDownService.class.getName()).log(Level.SEVERE, null, ex);
        }

        byte[] receiveData = new byte[2048];
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        while (!kill) {
            try {
                serverSocket.receive(receivePacket);
                int len = receivePacket.getLength();
                if (len > 0 && len <= 1500) {
                    data = new byte[len];
                    System.arraycopy(receivePacket.getData(), 0, data, 0, len);
                    if (lvl3BlueNode.gui && didTrigger == false) {
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
                                //keep alive
                                lvl3BlueNode.TrafficPrint(pre + version+" "+"[KEEP ALIVE]" ,0,0);
                            }  else if (args[0].equals("00001")) {
                                //le wild rednode ping!                                               
                                lvl3BlueNode.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).setUPing(true);
                                lvl3BlueNode.TrafficPrint(pre + "LE WILD RN UPING APPEARS",1,0);
                            } 
                        } else {
                            System.out.println(pre + "wrong length");
                        }
                    } else if (version.equals("1")) {                        
                        byte[] payload = IpPacket.getPayloadU(data);
                        String receivedMessage = new String(payload);
                        String args[] = receivedMessage.split("\\s+");
                        if (args.length > 1) {                            
                            if (args[0].equals("00004")){
                                //ack
                                lvl3BlueNode.TrafficPrint(pre + "[ACK] -> "+IpPacket.getUDestAddress(data).getHostAddress() ,2,0);
                                lvl3BlueNode.manager.offer(data); 
                            }  
                        } else {
                            System.out.println(pre + "wrong length");
                        }
                    } else {             
                        lvl3BlueNode.TrafficPrint(pre + "IPv4",3,0);
                        lvl3BlueNode.manager.offer(data);                        
                    }
                }
            } catch (java.net.SocketException ex1) {
                lvl3BlueNode.ConsolePrint(pre + "SOCKET DIED FOR " + vaddress);
            } catch (IOException ex) {
                Logger.getLogger(RedDownService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lvl3BlueNode.ConsolePrint(pre + " ENDED FOR " + vaddress);        
        lvl3BlueNode.UDPports.releasePort(portToUse);
        destPort = -1;
        portToUse = -1;
    }

    public void kill() {
        kill = true;        
        serverSocket.close();
    }

    public int getDownport() {
        return destPort;
    }        
}