package kostiskag.unitynetwork.bluenode.blueThreads;

import java.util.concurrent.atomic.AtomicBoolean;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

/**
 * It has been detected that some NATs may remove entries from their
 * tables if they were idle for a long time. This is a problem for us as
 * we need to keep alive the peer UDP connections at all times. This
 * class's thread will offer three packets every timeInSec in order to 
 * keep alive the connections. 
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueKeepAlive extends Thread {
    private final String pre;
    private final BlueNodeInstance blueNode;
    private final int numOfPacketsToSend;
    private final int timeInSec;
    private final byte[] packet; 
    private AtomicBoolean kill = new AtomicBoolean(false);
    
    public BlueKeepAlive(BlueNodeInstance blueNode) {        
    	this.blueNode = blueNode;
        this.pre = "^KEEP ALIVE "+blueNode.getName()+" ";
    	this.timeInSec = App.bn.trackerMaxIdleTime;
    	this.numOfPacketsToSend = 3;      
    	//0 is the keep alive packet
        this.packet = UnityPacket.buildKeepAlivePacket();             
    }
    
    public BlueNodeInstance getBlueNode() {
		return blueNode;
	}
    
    public boolean isKilled() {
		return kill.get();
	}

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTED AT "+Thread.currentThread().getName());        
        
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        //this offers three packets and sleeps again
        while (!kill.get()) {                                    
            for (int i=0; i<numOfPacketsToSend; i++) {                    
                blueNode.getQueueMan().offer(packet);                                    
            }            
            try {
                sleep(timeInSec * 1000);
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
