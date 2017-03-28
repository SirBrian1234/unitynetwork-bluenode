/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.RedThreads;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 *
 * @author kostis
 *
 * As we go further and further to this maze more problems appear in the
 * Internet a NAT may forget the registered ports if they are not being used so
 * we have to use a timer for keeping alive our host's DOWNLINKS
 */
public class RedKeepAlive extends Thread {

    private static String pre = "^KEEP ALIVE ";
    private boolean kill = false;
    private int time;
    private String vaddress;    
    InetAddress address;
    byte[] payload = ("00000 [KEEP ALIVE]").getBytes();
    byte[] data;

    public RedKeepAlive(String vaddress) {
        this.vaddress = vaddress;
        time = kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode.keepAliveTime;              
        data = kostiskag.unitynetwork.bluenode.Routing.IpPacket.MakeUPacket(payload, null, null, true);
    }

    @Override
    public void run() {
        lvl3BlueNode.ConsolePrint(pre + "STARTED FOR " + vaddress + " AT " + Thread.currentThread().getName());
      
        while (!kill) {            
            for (int i = 0; i < 3; i++) {
                lvl3BlueNode.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).getQueueMan().offer(data);                
            }

            try {
                sleep(time * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RedKeepAlive.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lvl3BlueNode.ConsolePrint(pre + " ENDED FOR " + vaddress);
    }

    public void kill() {
        kill = true;        
    }
}
