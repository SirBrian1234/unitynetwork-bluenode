package kostiskag.unitynetwork.bluenode.TrackClient;

import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author kostis
 */
public class TrackingDynamicAddress extends Thread {

    String pre = "^DYN ADDRESS ";
    boolean kill = false;
    
    public TrackingDynamicAddress() {
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
                sleep(App.bn.DynAddUpdateMins*60*1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TrackingDynamicAddress.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (kill) break;
            App.bn.ConsolePrint(pre+"UPDATING");
            boolean update = TrackingBlueNodeFunctions.update();        
            if (!update) {
                App.bn.ConsolePrint(pre+"GRAVE ERROR TRACKER DIED!!! STARTING BN KILL");                
                App.bn.die();                
            }
            
        }
        App.bn.ConsolePrint(pre+"DIED");
    }

    public void Kill() {
        this.kill = true;
    }
       
}
