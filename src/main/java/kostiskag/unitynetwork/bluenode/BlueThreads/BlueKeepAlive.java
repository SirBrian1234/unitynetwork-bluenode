/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.BlueThreads;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.RedThreads.RedKeepAlive;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class BlueKeepAlive extends Thread {
    private static String pre = "^KEEP ALIVE ";
    private boolean kill = false;
    private int time;
    private String hostname;
    InetAddress address;
    byte[] payload = ("00000 "+lvl3BlueNode.Hostname+" [KEEP ALIVE]  ").getBytes();
    byte[] data;    

    public BlueKeepAlive(String hostname) {
        time = lvl3BlueNode.keepAliveTime;
        this.hostname = hostname;
        data = IpPacket.MakeUPacket(payload, null, null, true);                
    }

    @Override
    public void run() {
        lvl3BlueNode.ConsolePrint(pre + "STARTED FOR " + hostname+ " AT " + Thread.currentThread().getName());        
        
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueKeepAlive.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        while (!kill) {                                    
            for (int i=0; i<3; i++) {                    
                lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getQueueMan().offer(data);                                    
            }            
            try {
                sleep(time * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(BlueKeepAlive.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void kill(){
        kill=true;
        lvl3BlueNode.ConsolePrint(pre+" ENDED FOR "+hostname);        
    }
}
