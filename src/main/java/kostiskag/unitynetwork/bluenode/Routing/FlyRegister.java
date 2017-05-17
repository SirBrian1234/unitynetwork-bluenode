package kostiskag.unitynetwork.bluenode.Routing;

import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicBoolean;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.blueNodeClient.BlueNodeClient;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;

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
						//first collect its ip address and port
                    	tr = new TrackerClient();
                        String[] args = tr.getPhysicalBn(BNHostname);
                        if (args[0].equals("OFFLINE")) {
                        	App.bn.TrafficPrint(pre + "FAILED TO ASSOCIATE WITH BLUE NODE, OFFLINE " + BNHostname, 3, 1);
                        	continue;
                        }
                        String address = args[0];
                        int port = Integer.parseInt(args[1]);
                        
                        //then, collect its public key
                        tr = new TrackerClient();
                        PublicKey pub = tr.getBlueNodesPubKey(BNHostname);
                        if (pub == null) {
                        	App.bn.TrafficPrint(pre + "FAILED TO ASSOCIATE WITH BLUE NODE, NO PUBLIC KEY " + BNHostname, 3, 1);
                        	continue;
                        }
                        
                        //use the above data to connect
                        BlueNodeClient cl = new BlueNodeClient(BNHostname, pub, address, port);
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
        App.bn.ConsolePrint(pre+"ENDED");
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
