package kostiskag.unitynetwork.bluenode.trackClient;

import kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author kostis
 */
public class TrackingTracker extends Thread {

    private final String pre = "^Tracker sonar ";
    boolean kill = false;
    
    public TrackingTracker() {
    }
    
    @Override
    public void run() {        
        //reverse error catch
        if (!App.bn.joined){
            App.bn.ConsolePrint(pre +"NOT JOINED");
            return;
        } else if (App.bn.name == null){
            App.bn.ConsolePrint(pre +"NO HOSTNAME");
            return;
        }
        
        App.bn.ConsolePrint(pre+"JUST STARTED");             
        while (!kill){
            App.bn.ConsolePrint(pre+"WAITING");
            try {
                sleep(App.bn.trackerCheckSec*1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            if (kill) break;
            int passedTime = App.bn.trackerRespond.getAndAdd(App.bn.trackerCheckSec*1000);
            App.bn.ConsolePrint(pre+" BUILDING TIME "+passedTime);
            
            if (passedTime > App.bn.trackerMaxIdleTimeMin*1000*60) {
            	App.bn.ConsolePrint(pre+"GRAVE ERROR TRACKER DIED!!! REMOVING RNS, STARTING BN KILL"); 
                App.bn.localRedNodesTable.releaseAll();
                App.bn.die();                
            }
        }
        App.bn.ConsolePrint(pre+"DIED");
    }

    public void Kill() {
        this.kill = true;
    }
       
}
