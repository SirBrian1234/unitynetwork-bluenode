package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.util.Iterator;
import java.util.LinkedList;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public final class BlueNodeTable {

    private final String pre = "^REMOTE BLUENODE TABLE ";
    private static boolean INSTANTIATED;

    private final LinkedList<BlueNode> list;
    private final boolean verbose;
    private final boolean notifyGui;

	public static BlueNodeTable newInstance() {
		//the only one who can get the only one reference is the first caller
		if (!INSTANTIATED) {
			INSTANTIATED = true;
			return new BlueNodeTable();
		}
		return null;
	}

    private BlueNodeTable() {
        this(true, true);
    }
    
    private BlueNodeTable(boolean verbose, boolean notifyGui) {
        list = new LinkedList<BlueNode>();
        this.verbose = verbose;
        this.notifyGui = notifyGui;
        if (verbose) {
			AppLogger.getInstance().consolePrint(pre + "INITIALIZED");
        }
    }

    public synchronized BlueNode getBlueNodeInstanceByName(String name) throws Exception {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.getName().equals(name)) {
    			return bn;
    		}
    	}
    	throw new Exception(pre + "NO ENTRY FOR "+name+" IN TABLE");
    }
    
    public synchronized BlueNode getBlueNodeInstanceByRRNVaddr(String vaddress) throws Exception {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.table.checkByVaddr(vaddress)) {
    			return bn;
    		}
    	}
    	throw new Exception(pre + "NO RRN ENTRY WITH VADDRESS "+vaddress+" IN TABLE");
    }
    
    public synchronized BlueNode getBlueNodeInstanceByRRNHostname(String hostname) throws Exception {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.table.checkByHostname(hostname)) {
    			return bn;
    		}
    	}
    	throw new Exception(pre + "NO RRN ENTRY WITH HOSTNAME "+hostname+" IN TABLE");
    }

    //lease is applied at the end of the associate process
    public synchronized void leaseBn(BlueNode blueNode) throws Exception {
        //check if already exists
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.getName().equals(blueNode.getName())) {
    			throw new Exception(pre+"DUPLICATE ENTRY "+blueNode.getName()+" ATTEMPTED TO JOIN BNTABLE.");
    		}
    	}
    	
    	//add if not found
    	list.add(blueNode);
    	if (verbose) {
			AppLogger.getInstance().consolePrint(pre +"LEASED BLUE NODE " + blueNode.getName());
    	}
    	notifyGUI();
    }

    public synchronized void leaseRRn(BlueNode blueNode, String hostname, String vaddress) throws Exception {
        //check if already exists from all bns
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.table.checkByHostname(hostname) || bn.table.checkByVaddr(vaddress)) {
    			throw new Exception(pre+"DUPLICATE ENTRY FOR REMOTE RED NODE "+hostname+" "+vaddress);
    		}
    	}    	
    	//notify goes to RemoteRedNodeTable
    	blueNode.table.lease(hostname, vaddress);
    }
    
    public synchronized void releaseBn(String name) throws Exception {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.getName().equals(name)) {
    			BlueNodeClient cl = new BlueNodeClient(bn);
        		cl.removeThisBlueNodesProjection();
        		bn.killtasks();  	    			
    			it.remove();
    			if (verbose) {
					AppLogger.getInstance().consolePrint(pre +"RELEASED BLUE NODE " + bn.getName());
    			}
    			notifyGUI();
    			notifyRGUI();
    			return;
    		}
    	} 
        throw new Exception(pre + "NO ENTRY FOR "+name+" IN TABLE");
    }

    public synchronized boolean checkBlueNode(String name) {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.getName().equals(name)) {
    			return true;
    		}
    	} 
        return false;
    }
    
    /**
     * This is triggered when a BN needs to exit the network.
     * In this case and since the bn may exit bn.killtasks(); 
     * is not required.
     */
    public synchronized void sendKillSigsAndReleaseForAll() {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		BlueNodeClient cl = new BlueNodeClient(bn);
    		cl.removeThisBlueNodesProjection();
    		//bn.killtasks();  	
    		if (verbose) {
                AppLogger.getInstance().consolePrint(pre +"RELEASED BLUE NODE " + bn.getName());
			}
    	}
    	list.clear();
    }
    
    public synchronized boolean checkRemoteRedNodeByHostname(String hostname) {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.table.checkByHostname(hostname)) {
    			return true;
    		}
    	} 
        return false;
    }
    
    public synchronized boolean checkRemoteRedNodeByVaddress(String vaddress) {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.table.checkByVaddr(vaddress)) {
    			return true;
    		}
    	} 
        return false;
    }
    
    public synchronized void releaseLocalRedNodeByHostnameFromBn(String hostname, String blueNodeName) {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		if (bn.getName().equals(blueNodeName)) {
    			BlueNodeClient cl = new BlueNodeClient(bn);
        		cl.removeRedNodeProjectionByHn(hostname);
        		return;
    		}
    	}         
    }
    
    public synchronized void releaseLocalRedNodeByHostnameFromAll(String hostname) {
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNode bn = it.next();
    		BlueNodeClient cl = new BlueNodeClient(bn);
    		cl.removeRedNodeProjectionByHn(hostname);
    	} 
    }

    public synchronized String[][] buildBNGUIObj() {
    	String[][] object = new String[list.size()][];
    	Iterator<BlueNode> it = list.listIterator();
        int i=0;
    	while(it.hasNext()) {
        	BlueNode bn = it.next();
        	object[i] = new String[]{bn.getName(), bn.isTheRemoteAServer(), bn.getPhAddressStr(), ""+bn.getRemoteAuthPort(),""+bn.getPortToSend(), ""+bn.getPortToReceive(), bn.getTime()};
        	i++;
        }
    	return object;
    }
    
    public synchronized String[][] buildRRNGUIObj() {
    	//this block calculates the total size of the string array
    	int totalSize = 0;
    	Iterator<BlueNode> it = list.listIterator();
    	while(it.hasNext()) {
        	BlueNode bn = it.next();
        	totalSize += bn.table.getSize();
    	}
    	String[][] object =  new String[totalSize][];
    	
    	//this block fills the array with the rrns from each bn
    	it = list.listIterator();
        int i=0;
        while(it.hasNext()) {
         	BlueNode bn = it.next();
         	Iterator<RemoteRedNode> rit = bn.table.getList().listIterator();
         	while(rit.hasNext()) {
         		RemoteRedNode rn = rit.next();
         		object[i] = new String[]{rn.getHostname(), rn.getAddress().asString() , bn.getName(), rn.getTimestamp().asDate().toString()};
         		i++;
         	}         	
        }
        return object;
    }
       
    /**
     * This is triggered by the sonarService 
     * for all the associated blue nodes inside the table where the calling bn is
     * a server this has to be called in order to detect dead entries and disconnected rrns
     */
	public synchronized void rebuildTableViaAuthClient() {
		Iterator<BlueNode> iterator = list.listIterator();
    	while (iterator.hasNext()) {
    		BlueNode element = iterator.next();
    		if (element.isServer()) {
	            try {
	            	BlueNodeClient cl = new BlueNodeClient(element);
					if (cl.checkBlueNode()) {
						if (verbose) {
							AppLogger.getInstance().consolePrint(pre+"Fetching RNs from BN "+element.getName());
						}
					    element.updateTime();
					    cl = new BlueNodeClient(element);
					    LinkedList<RemoteRedNode> rns = cl.getRemoteRedNodesObj();
					    LinkedList<RemoteRedNode> in = element.table.getList();
					    LinkedList<RemoteRedNode> valid = new LinkedList<RemoteRedNode>();
					    Iterator<RemoteRedNode> rnsIt = rns.iterator();
					    
					    while (rnsIt.hasNext()) {
					    	RemoteRedNode outE = rnsIt.next();
				    		Iterator<RemoteRedNode> inIt = in.iterator();
				    		while(inIt.hasNext()) {
				    			RemoteRedNode inE = inIt.next();
					    		if (inE.getHostname().equals(outE.getHostname())) {
					    			valid.add(inE);			    			
					    		}				    	
				    		}
					    }				    
					    element.table = new RemoteRedNodeTable(element, valid);
					} else { 
						element.killtasks();
						iterator.remove();      
						if (verbose) {
							AppLogger.getInstance().consolePrint(pre +"RELEASED NON RESPONDING BLUE NODE " + element.getName());
						}
					}
				} catch (Exception e) {
					element.killtasks();
					iterator.remove();  
				}
    		}
    	}
    	System.out.println(pre+" BN Table rebuilt");
    	notifyGUI();    	
    	notifyRGUI();
	}
	
	private void notifyGUI() {
    	if (notifyGui) {
    		MainWindow.getInstance().updateBNs();
    	}
    }
    
    private void notifyRGUI () {
    	if (notifyGui) {
			MainWindow.getInstance().updateRemoteRns();
    	}
    }
}
