package kostiskag.unitynetwork.bluenode.socket.blueNodeClient;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import kostiskag.unitynetwork.bluenode.App;

/**
 * Works like the java garbage collector but for killed bluenodes and redonodes. The sonar
 * connects to the leased bluenodes and requests to get their respective rednodes back
 * When a dead bn is found, it, and its rns are removed from this bluenode
 * 
 * @author Konstantinos Kagiampakis
 */
public class SonarService extends Thread {

    private final String pre = "^SonarService ";
    private final int time;
    private AtomicBoolean kill = new AtomicBoolean(false);
    
    public SonarService(int time) {
        this.time = time;
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre+"started in thread "+Thread.currentThread()+" with time "+time+" sec");
        while (!kill.get()) {
            try {
                sleep(time*1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            if (kill.get()) break;
            App.bn.ConsolePrint(pre+"Updating BN Tables via ping");
            App.bn.blueNodesTable.rebuildTableViaAuthClient();
        }
    }
    
    public synchronized void kill(){
        kill.set(true);
    }
}
