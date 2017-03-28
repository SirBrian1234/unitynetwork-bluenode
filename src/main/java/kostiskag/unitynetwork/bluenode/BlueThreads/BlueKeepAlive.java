package kostiskag.unitynetwork.bluenode.BlueThreads;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;

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
    byte[] payload = ("00000 "+App.Hostname+" [KEEP ALIVE]  ").getBytes();
    byte[] data;    

    public BlueKeepAlive(String hostname) {
        time = App.keepAliveTime;
        this.hostname = hostname;
        data = IpPacket.MakeUPacket(payload, null, null, true);                
    }

    @Override
    public void run() {
        App.ConsolePrint(pre + "STARTED FOR " + hostname+ " AT " + Thread.currentThread().getName());        
        
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueKeepAlive.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        while (!kill) {                                    
            for (int i=0; i<3; i++) {                    
                App.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getQueueMan().offer(data);                                    
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
        App.ConsolePrint(pre+" ENDED FOR "+hostname);        
    }
}
