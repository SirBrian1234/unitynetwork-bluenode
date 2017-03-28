/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.TrackClient;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        if (!lvl3BlueNode.joined){
            lvl3BlueNode.ConsolePrint(pre +"NOT JOINED");
            return;
        } else if (lvl3BlueNode.Hostname == null){
            lvl3BlueNode.ConsolePrint(pre +"NO HOSTNAME");
            return;
        }
        
        lvl3BlueNode.ConsolePrint(pre+"JUST STARTED");             
        while (!kill){
            lvl3BlueNode.ConsolePrint(pre+"WAITING");
            try {
                sleep(lvl3BlueNode.DynAddUpdateMins*60*1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(TrackingDynamicAddress.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (kill) break;
            lvl3BlueNode.ConsolePrint(pre+"UPDATING");
            boolean update = TrackingBlueNodeFunctions.update();        
            if (!update) {
                lvl3BlueNode.ConsolePrint(pre+"GRAVE ERROR TRACKER DIED!!! STARTING BN KILL");                
                lvl3BlueNode.die();                
            }
            
        }
        lvl3BlueNode.ConsolePrint(pre+"DIED");
    }

    public void Kill() {
        this.kill = true;
    }
       
}
