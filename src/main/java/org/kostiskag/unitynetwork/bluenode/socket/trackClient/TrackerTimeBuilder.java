package org.kostiskag.unitynetwork.bluenode.socket.trackClient;

import java.util.concurrent.atomic.AtomicBoolean;

import org.kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class TrackerTimeBuilder extends Thread {

    private final String pre = "^Tracker sonar ";
    private AtomicBoolean kill = new AtomicBoolean(false);
    
    public TrackerTimeBuilder() {
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
        while (!kill.get()){
            App.bn.ConsolePrint(pre+"WAITING");
            try {
                sleep(App.bn.trackerCheckSec*1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            if (kill.get()) break;
            int passedTime = App.bn.trackerRespond.addAndGet(App.bn.trackerCheckSec*1000);
            App.bn.ConsolePrint(pre+" BUILDING TIME "+passedTime);
            
            if (passedTime > App.bn.trackerMaxIdleTimeMin*1000*60) {
            	App.bn.ConsolePrint(pre+"GRAVE ERROR TRACKER DIED!!! REMOVING RNS, STARTING BN KILL"); 
                App.bn.localRedNodesTable.exitAll();
                App.bn.die();                
            }
        }
        App.bn.ConsolePrint(pre+"DIED");
    }

    public void Kill() {
        kill.set(true);
    }       
}
