package kostiskag.unitynetwork.bluenode.redThreads;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;

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
        destPort = App.bn.UDPports.requestPort();
        pre = pre + vaddress + " ";
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED FOR " + vaddress + " AT " + Thread.currentThread().getName() + " ON PORT " + destPort);
     
        try {
            serverSocket = new DatagramSocket(destPort);
        } catch (java.net.BindException ex) {
            App.bn.ConsolePrint(pre + "PORT ALLREADY IN USE, EXITING");
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
                    if (App.bn.gui && didTrigger == false) {
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
                                App.bn.TrafficPrint(pre + version+" "+"[KEEP ALIVE]" ,0,0);
                            }  else if (args[0].equals("00001")) {
                                //le wild rednode ping!                                               
                                App.bn.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).setUPing(true);
                                App.bn.TrafficPrint(pre + "LE WILD RN UPING APPEARS",1,0);
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
                                App.bn.TrafficPrint(pre + "[ACK] -> "+IpPacket.getUDestAddress(data).getHostAddress() ,2,0);
                                App.bn.manager.offer(data); 
                            }  
                        } else {
                            System.out.println(pre + "wrong length");
                        }
                    } else {             
                        App.bn.TrafficPrint(pre + "IPv4",3,0);
                        App.bn.manager.offer(data);                        
                    }
                }
            } catch (java.net.SocketException ex1) {
                App.bn.ConsolePrint(pre + "SOCKET DIED FOR " + vaddress);
            } catch (IOException ex) {
                Logger.getLogger(RedDownService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        App.bn.ConsolePrint(pre + " ENDED FOR " + vaddress);        
        App.bn.UDPports.releasePort(portToUse);
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