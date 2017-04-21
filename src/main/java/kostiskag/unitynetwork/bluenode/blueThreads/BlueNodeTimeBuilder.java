package kostiskag.unitynetwork.bluenode.blueThreads;

import java.util.concurrent.atomic.AtomicBoolean;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeTimeBuilder extends Thread {

    private final String pre;
    private final BlueNodeInstance bn;
    private final int buildStepSec;
    private final int maxWaitTimeSec;
    private AtomicBoolean kill = new AtomicBoolean(false);
    
    public BlueNodeTimeBuilder(BlueNodeInstance bn, int buildStepSec, int maxWaitTimeSec) {
    	this.bn = bn;
    	this.pre = "^BlueNodeTimeBuilder "+bn.getName()+" ";
    	this.buildStepSec = buildStepSec;
    	this.maxWaitTimeSec = maxWaitTimeSec;
    }
    
    @Override
    public void run() {        
        
    	App.bn.ConsolePrint(pre+"JUST STARTED");             
        while (!kill.get()){
            App.bn.ConsolePrint(pre+"WAITING");
            try {
                sleep(buildStepSec*1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            if (kill.get()) break;
            int passedTime = bn.idleTime.addAndGet(buildStepSec*1000);
            
            if (bn.idleTime.get() > maxWaitTimeSec*1000) {
            	App.bn.ConsolePrint(pre+"BlueNode is not responding releasing from the local bn table");
            	try {
					App.bn.blueNodesTable.releaseBn(bn.getName());
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        }
        App.bn.ConsolePrint(pre+"ENDED");
    }

    public void Kill() {
        kill.set(true);
    }       
}
