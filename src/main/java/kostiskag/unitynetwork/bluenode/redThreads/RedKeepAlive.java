package kostiskag.unitynetwork.bluenode.redThreads;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;

/**
 * Some NAT tables drop idle entries. This is why
 * we have to send packets periodically to keep alive our host's DOWNLINKS
 * 
 * @author kostis
 */
public class RedKeepAlive extends Thread {

    private final String pre;    
    private final LocalRedNodeInstance rn;
    //data
    private final int keepAliveTime;
    private final int numOfPacketsToSend = 3;
    private final byte[] payload = ("00000 [KEEP ALIVE]").getBytes();
    private final byte[] data;
    //triggers
    private AtomicBoolean kill = new AtomicBoolean(false);

    public RedKeepAlive(LocalRedNodeInstance rn) {
        this.rn = rn;
        this.pre = "^RedKeepAlive "+rn.getHostname()+" ";
        this.keepAliveTime = kostiskag.unitynetwork.bluenode.App.bn.keepAliveTime;              
        this.data = kostiskag.unitynetwork.bluenode.Routing.IpPacket.MakeUPacket(payload, null, null, true);
    }
    
    public LocalRedNodeInstance getRn() {
		return rn;
	}
    
    public boolean getIsKilled() {
    	return kill.get();
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED AT " + Thread.currentThread().getName());
      
        while (!kill.get()) {            
            for (int i = 0; i < numOfPacketsToSend; i++) {
                rn.getQueueMan().offer(data);                
            }

            try {
                sleep(keepAliveTime * 1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        App.bn.ConsolePrint(pre+"ENDED");
    }

    public void kill() {
        kill.set(true);       
    }
}
