package org.kostiskag.unitynetwork.bluenode.routing;

import java.util.concurrent.atomic.AtomicBoolean;
import java.security.PublicKey;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.common.entry.NodeType;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.AppLogger.MessageType;
import org.kostiskag.unitynetwork.bluenode.App;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public class FlyRegister extends Thread {

    private String pre = "^FlyRegister ";
    QueuePair queue = new QueuePair(100);
    private AtomicBoolean kill = new AtomicBoolean(false);

    public FlyRegister() {    
    	
    }

    @Override
    public void run() {    	
        while (!kill.get()) {                    	
        	
        	//waits...
        	SourceDestPair pair =  queue.poll();
        	//This obj makes the thread to wait when empty
        	
            if (kill.get()) {
                break;
            } else if (pair == null) {
            	continue;
            }
           
            String sourcevaddress = pair.sourcevaddress;
            String destvaddress = pair.destvaddress;

            AppLogger.getInstance().consolePrint(pre + "Seeking to associate "+sourcevaddress+" with "+destvaddress);

            if (App.bn.blueNodeTable.checkRemoteRedNodeByVaddress(destvaddress)) {
            	//check if it was associated one loop back
                AppLogger.getInstance().consolePrint(pre + "Allready associated entry");
                continue;
            } else {
            	//make stuff
            	TrackerClient tr = new TrackerClient();
                String BNHostname = tr.checkRnOnlineByVaddr(destvaddress);                
                if (BNHostname != null) {                    
                    if (App.bn.blueNodeTable.checkBlueNode(BNHostname)) {
                    	//we might have him associated but we may not have his rrd
                    	BlueNode bn;
						try {
							bn = App.bn.blueNodeTable.getBlueNodeInstanceByName(BNHostname);
							BlueNodeClient cl = new BlueNodeClient(bn);
							String remoteHostname = cl.getRedNodeHostnameByVaddress(destvaddress);
	                    	if (!remoteHostname.equals("OFFLINE")) {
	                    		App.bn.blueNodeTable.leaseRRn(bn, remoteHostname, destvaddress);
	                    	}
						} catch (Exception e) {
							e.printStackTrace();
						}   
					} else {
						//if he is not associated at all, then associate
						//first collect its ip address and port
                    	tr = new TrackerClient();
                        String[] args = tr.getPhysicalBn(BNHostname);
                        if (args[0].equals("OFFLINE")) {
                            AppLogger.getInstance().trafficPrint(pre + "FAILED TO ASSOCIATE WITH BLUE NODE, OFFLINE " + BNHostname, MessageType.ROUTING, NodeType.BLUENODE);
                        	continue;
                        }
                        String address = args[0];
                        int port = Integer.parseInt(args[1]);
                        
                        //then, collect its public key
                        tr = new TrackerClient();
                        PublicKey pub = tr.getBlueNodesPubKey(BNHostname);
                        if (pub == null) {
                            AppLogger.getInstance().trafficPrint(pre + "FAILED TO ASSOCIATE WITH BLUE NODE, NO PUBLIC KEY " + BNHostname, MessageType.ROUTING, NodeType.BLUENODE);
                        	continue;
                        }
                        
                        //use the above data to connect
                        BlueNodeClient cl = new BlueNodeClient(BNHostname, pub, address, port);
                        try {
							cl.associateClient();
						} catch (Exception e) {
                            AppLogger.getInstance().trafficPrint(pre + "FAILED TO ASSOCIATE WITH BLUE NODE " + BNHostname, MessageType.ROUTING, NodeType.BLUENODE);
							 continue;
						}
                        AppLogger.getInstance().trafficPrint(pre + "BLUE NODE " + BNHostname + " ASSOCIATED", MessageType.ROUTING, NodeType.BLUENODE);
                        
                        //we were associated now it's time to feed return route
                        BlueNode bn;
						try {
							bn = App.bn.blueNodeTable.getBlueNodeInstanceByName(BNHostname);
							cl = new BlueNodeClient(bn);
							cl.feedReturnRoute(App.bn.localRedNodesTable.getRedNodeInstanceByAddr(sourcevaddress).getHostname(), sourcevaddress);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
                        //and then request the dest rn hotsname
                        try {
							bn = App.bn.blueNodeTable.getBlueNodeInstanceByName(BNHostname);
							cl = new BlueNodeClient(bn);
	                    	String remoteHostname = cl.getRedNodeHostnameByVaddress(destvaddress);
	                    	if (!remoteHostname.equals("OFFLINE")) {
	                    		App.bn.blueNodeTable.leaseRRn(bn, remoteHostname, destvaddress);
	                    	}
						} catch (Exception e) {
							e.printStackTrace();
						} 						
                    }                    
                } else {
                    AppLogger.getInstance().consolePrint(pre + "NOT FOUND "+destvaddress+" ON NETWORK");
                }
            }
        }
        AppLogger.getInstance().consolePrint(pre+"ENDED");
    }

    public void seekDest(String sourcevaddress, String destvaddress) {
        SourceDestPair pair = new SourceDestPair(sourcevaddress, destvaddress);        
        queue.offer(pair);        
    }
    
    public void kill() {
        kill.set(true);
        queue.exit();
    }
}
