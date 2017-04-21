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

            if (App.bn.blueNodesTable.checkRemoteRedNodeByVaddress(destvaddress)) {
            	//check if it was associated one loop back
                App.bn.ConsolePrint(pre + "Allready associated entry");
                continue;
            } else {
            	//make stuff
            	TrackerClient tr = new TrackerClient();
                String BNHostname = tr.checkRnOnlineByVaddr(destvaddress);                
                if (BNHostname != null) {                    
                    if (App.bn.blueNodesTable.checkBlueNode(BNHostname)) {
                    	//we might have him associated but we may not have his rrd
                    	BlueNodeInstance bn;
						try {
							bn = App.bn.blueNodesTable.getBlueNodeInstanceByName(BNHostname);
							BlueNodeClient cl = new BlueNodeClient(bn);
							String remoteHostname = cl.getRedNodeHostnameByVaddress(destvaddress);
	                    	if (!remoteHostname.equals("OFFLINE")) {
	                    		App.bn.blueNodesTable.leaseRRn(bn, remoteHostname, destvaddress);
	                    	}
						} catch (Exception e) {
							e.printStackTrace();
						}   
					} else {
						//if he is not associated at all, then associate
                    	tr = new TrackerClient();
                        String[] args = tr.getPhysicalBn(BNHostname);
                        String address = args[0];
                        int port = Integer.parseInt(args[1]);
                        
                        BlueNodeClient cl = new BlueNodeClient(BNHostname, address, port);
                        try {
							cl.associateClient();
						} catch (Exception e) {
							 App.bn.TrafficPrint(pre + "FAILED TO ASSOCIATE WITH BLUE NODE " + BNHostname, 3, 1);  
							 continue;
						} 
                        App.bn.TrafficPrint(pre + "BLUE NODE " + BNHostname + " ASSOCIATED", 3, 1);
                        
                        //we were associated now it's time to feed return route
                        BlueNodeInstance bn;
						try {
							bn = App.bn.blueNodesTable.getBlueNodeInstanceByName(BNHostname);
							cl = new BlueNodeClient(bn);
							cl.feedReturnRoute(App.bn.localRedNodesTable.getRedNodeInstanceByAddr(sourcevaddress).getHostname(), sourcevaddress);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
                        //and then request the dest rn hotsname
                        try {
							bn = App.bn.blueNodesTable.getBlueNodeInstanceByName(BNHostname);
							cl = new BlueNodeClient(bn);
	                    	String remoteHostname = cl.getRedNodeHostnameByVaddress(destvaddress);
	                    	if (!remoteHostname.equals("OFFLINE")) {
	                    		App.bn.blueNodesTable.leaseRRn(bn, remoteHostname, destvaddress);
	                    	}
						} catch (Exception e) {
							e.printStackTrace();
						} 						
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
