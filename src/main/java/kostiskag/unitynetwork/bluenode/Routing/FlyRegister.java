package kostiskag.unitynetwork.bluenode.Routing;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.blueNodeClient.BlueNodeClient;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;

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
            
            App.bn.ConsolePrint(pre + "Seeking to associate "+sourcevaddress+" with "+destvaddress);

            //maybe it associated one loop back
            if (App.bn.blueNodesTable.checkRemoteRedNodeByVaddress(destvaddress)) {
                App.bn.ConsolePrint(pre + "Allready associated entry");
                continue;
            } else {                
            	TrackerClient tr = new TrackerClient();
                String BNHostname = tr.checkRnOnlineByVaddr(destvaddress);                
                if (BNHostname != null) {
                    //we might have him associated but we may not have his rrd
                    if (!App.bn.blueNodesTable.checkBlueNode(BNHostname)) {
                    	tr = new TrackerClient();
                        String[] args = tr.getPhysicalBn(BNHostname);
                        String address = args[0];
                        int port = Integer.parseInt(args[1]);
                        
                        BlueNodeClient cl = new BlueNodeClient(BNHostname, address, port);
                        try {
							cl.associateClient();
						} catch (Exception e) {
							e.printStackTrace();
						}                        
                    } else {
                    	BlueNodeInstance bn;
						try {
							bn = App.bn.blueNodesTable.getBlueNodeInstanceByName(BNHostname);
							BlueNodeClient cl = new BlueNodeClient(bn);
	                    	cl.exchangeRedNodes();
						} catch (Exception e) {
							e.printStackTrace();
						}                     	
                    }
                    if (App.bn.blueNodesTable.checkBlueNode(BNHostname)) {                        
                        App.bn.TrafficPrint(pre + "BLUE NODE " + BNHostname + " ASSOCIATED", 3, 1);
                    } else {
                        App.bn.TrafficPrint(pre + "FAILED TO ASSOCIATE WITH BLUE NODE " + BNHostname, 3, 1);                                                
                    }
                } else {
                   App.bn.ConsolePrint(pre + "NOT FOUND "+destvaddress+" ON NETWORK");                     
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
