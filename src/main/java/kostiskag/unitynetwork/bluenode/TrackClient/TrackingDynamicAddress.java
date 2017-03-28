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
        if (!App.joined){
            App.ConsolePrint(pre +"NOT JOINED");
            return;
        } else if (App.Hostname == null){
            App.ConsolePrint(pre +"NO HOSTNAME");
            return;
        }
        
        App.ConsolePrint(pre+"JUST STARTED");             
        while (!kill){
            App.ConsolePrint(pre+"WAITING");
            try {
                sleep(App.DynAddUpdateMins*60*1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TrackingDynamicAddress.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (kill) break;
            App.ConsolePrint(pre+"UPDATING");
            boolean update = TrackingBlueNodeFunctions.update();        
            if (!update) {
                App.ConsolePrint(pre+"GRAVE ERROR TRACKER DIED!!! STARTING BN KILL");                
                App.die();                
            }
            
        }
        App.ConsolePrint(pre+"DIED");
    }

    public void Kill() {
        this.kill = true;
    }
       
}
