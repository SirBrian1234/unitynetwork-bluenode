/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.Routing;

import kostiskag.unitynetwork.bluenode.TrackClient.TrackingBlueNodeFunctions;
import kostiskag.unitynetwork.bluenode.TrackClient.TrackingRedNodeFunctions;
import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.BlueNodeClient.RemoteHandle;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class FlyRegister extends Thread {

    private String pre = "^FLY REG ";
    Queue<SourceDestPair> hotAddresses;
    private boolean kill = false;

    public FlyRegister() {    
        hotAddresses = new LinkedList<SourceDestPair>();
    }

    @Override
    public void run() {
        seek();
    }

    public synchronized void seek() {

        while (!kill) {

            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(FlyRegister.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (kill) {
                continue;
            }
            if (hotAddresses.isEmpty()) {
                continue;
            }
                                                
            SourceDestPair pair =  hotAddresses.poll();
            String sourcevaddress = pair.sourcevaddress;
            String destvaddress = pair.destvaddress;
            
            lvl3BlueNode.ConsolePrint(pre + "Seeking to associate "+sourcevaddress+" with "+destvaddress);

            //maybe it associated one loop back
            if (lvl3BlueNode.remoteRedNodesTable.checkAssociated(destvaddress) == true) {
                lvl3BlueNode.ConsolePrint(pre + "Allready associated entry");
                continue;
            } else {                
                String BNHostname = TrackingRedNodeFunctions.checkOnlineByAddr(destvaddress);                
                if (BNHostname != null) {
                    //we might have him associated but we may not have his rrd
                    if (!lvl3BlueNode.BlueNodesTable.checkBlueNode(BNHostname)) {
                        String phaddress = TrackingBlueNodeFunctions.getPhysical(BNHostname);
                        String[] args = phaddress.split(":");
                        String address = args[0];
                        int port;
                        
                        if (args.length > 1) {
                            port = Integer.parseInt(args[1]);
                        } else {
                            port = 7000;
                        }
                        
                        RemoteHandle.addBlueNodeWithExchange(BNHostname, address, port, sourcevaddress, destvaddress);
                    } else {
                        RemoteHandle.BlueNodeExchange(BNHostname, sourcevaddress, destvaddress);
                    }
                    if (lvl3BlueNode.BlueNodesTable.checkBlueNode(BNHostname)) {                        
                        lvl3BlueNode.TrafficPrint(pre + "BLUE NODE " + BNHostname + " ASSOCIATED", 3, 1);
                    } else {
                        lvl3BlueNode.TrafficPrint(pre + "FAILED TO ASSOCIATE WITH BLUE NODE " + BNHostname, 3, 1);                                                
                    }
                } else {
                   lvl3BlueNode.ConsolePrint(pre + "NOT FOUND "+destvaddress+" ON NETWORK");                     
                }
            }
        }
    }

    public synchronized void seekDest(String sourcevaddress, String destvaddress) {
        SourceDestPair pair = new SourceDestPair(sourcevaddress, destvaddress);        
        hotAddresses.offer(pair);
        notify();
    }

    public synchronized void kill() {
        kill = true;
        notify();
    }
}
