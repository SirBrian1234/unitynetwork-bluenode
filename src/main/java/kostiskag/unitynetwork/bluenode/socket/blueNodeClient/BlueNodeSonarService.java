package kostiskag.unitynetwork.bluenode.socket.blueNodeClient;

import java.util.concurrent.atomic.AtomicBoolean;

import kostiskag.unitynetwork.bluenode.App;

/**
 * Works like the java garbage collector but for killed bluenodes and remote redonodes. The sonar
 * connects to the associated bluenodes where the calling bluenode is a server
 * and requests to get their status and remote rednodes back
 * When a dead bn is found, it, and its rns are removed from this bluenode
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeSonarService extends Thread {

    private final String pre = "^BlueNodeSonarService ";
    private final int timeInSec;
    private AtomicBoolean kill = new AtomicBoolean(false);
    
    public BlueNodeSonarService(int timeInSec) {
        this.timeInSec = timeInSec;
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre+"started in thread "+Thread.currentThread()+" with time pulse "+timeInSec+" sec");
        while (!kill.get()) {
            try {
                sleep(timeInSec*1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            if (kill.get()) break;
            App.bn.ConsolePrint(pre+"Updating BN Tables via ping");
            App.bn.blueNodesTable.rebuildTableViaAuthClient();
        }
    }
    
    public synchronized void kill() {
        kill.set(true);
    }
}
