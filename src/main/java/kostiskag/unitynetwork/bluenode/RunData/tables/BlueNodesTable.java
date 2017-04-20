package kostiskag.unitynetwork.bluenode.RunData.tables;

import java.util.Iterator;
import java.util.LinkedList;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.RunData.instances.RemoteRedNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.blueNodeClient.BlueNodeClient;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodesTable {

    private final String pre = "^REMOTE BLUENODE TABLE ";
    private final LinkedList<BlueNodeInstance> list;
    private final boolean verbose;
    private final boolean notifyGui;

    public BlueNodesTable() {
        list = new LinkedList<BlueNodeInstance>();
        verbose = true;
        notifyGui = true;
        App.bn.ConsolePrint(pre + "INITIALIZED");
    }
    
    public BlueNodesTable(boolean verbose, boolean notifyGui) {
        list = new LinkedList<BlueNodeInstance>();
        this.verbose = verbose;
        this.notifyGui = notifyGui;
        if (verbose) {
        	App.bn.ConsolePrint(pre + "INITIALIZED");
        }
    }

    public synchronized BlueNodeInstance getBlueNodeInstanceByName(String name) throws Exception {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.getName().equals(name)) {
    			return bn;
    		}
    	}
    	throw new Exception(pre + "NO ENTRY FOR "+name+" IN TABLE");
    }
    
    public synchronized BlueNodeInstance getBlueNodeInstanceByRRNVaddr(String vaddress) throws Exception {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.table.checkByVaddr(vaddress)) {
    			return bn;
    		}
    	}
    	throw new Exception(pre + "NO RRN ENTRY WITH VADDRESS "+vaddress+" IN TABLE");
    }
    
    public synchronized BlueNodeInstance getBlueNodeInstanceByRRNHostname(String hostname) throws Exception {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.table.checkByHostname(hostname)) {
    			return bn;
    		}
    	}
    	throw new Exception(pre + "NO RRN ENTRY WITH HOSTNAME "+hostname+" IN TABLE");
    }

    //lease is applied at the end of the associate process
    public synchronized void leaseBn(BlueNodeInstance blueNode) throws Exception {
        //check if already exists
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.getName().equals(blueNode.getName())) {
    			throw new Exception(pre+"DUPLICATE ENTRY "+blueNode.getName()+" ATTEMPTED TO JOIN BNTABLE.");
    		}
    	}
    	
    	//add if not found
    	list.add(blueNode);
    	if (verbose) {
    		App.bn.ConsolePrint(pre +"LEASED BLUE NODE " + blueNode.getName());
    	}
    	notifyGUI();
    }

    public synchronized void leaseRRn(BlueNodeInstance blueNode, String hostname, String vaddress) throws Exception {
        //check if already exists from all bns
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.table.checkByHostname(hostname) || bn.table.checkByVaddr(vaddress)) {
    			throw new Exception(pre+"DUPLICATE ENTRY FOR REMOTE RED NODE "+hostname+" "+vaddress);
    		}
    	}    	
    	//notify goes to RemoteRedNodeTable
    	blueNode.table.lease(hostname, vaddress);
    }
    
    public synchronized void releaseBn(String name) throws Exception {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.getName().equals(name)) {
    			BlueNodeClient cl = new BlueNodeClient(bn);
        		cl.removeThisBlueNodesProjection();
        		bn.killtasks();  	    			
    			it.remove();
    			if (verbose) {
    				App.bn.ConsolePrint(pre +"RELEASED BLUE NODE " + bn.getName());
    			}
    			notifyGUI();
    			notifyRGUI();
    			return;
    		}
    	} 
        throw new Exception(pre + "NO ENTRY FOR "+name+" IN TABLE");
    }

    public synchronized boolean checkBlueNode(String name) {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.getName().equals(name)) {
    			return true;
    		}
    	} 
        return false;
    }
    
    public synchronized void sendKillSigsAndReleaseForAll() {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		BlueNodeClient cl = new BlueNodeClient(bn);
    		cl.removeThisBlueNodesProjection();
    		bn.killtasks();  	
    		if (verbose) {
				App.bn.ConsolePrint(pre +"RELEASED BLUE NODE " + bn.getName());
			}
    	}
    	list.clear();
    }
    
    public synchronized boolean checkRemoteRedNodeByHostname(String hostname) {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.table.checkByHostname(hostname)) {
    			return true;
    		}
    	} 
        return false;
    }
    
    public synchronized boolean checkRemoteRedNodeByVaddress(String vaddress) {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.table.checkByVaddr(vaddress)) {
    			return true;
    		}
    	} 
        return false;
    }
    
    public synchronized void releaseLocalRedNodeByHostnameFromBn(String hostname, String blueNodeName) {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		if (bn.getName().equals(blueNodeName)) {
    			BlueNodeClient cl = new BlueNodeClient(bn);
        		cl.removeRedNodeProjectionByHn(hostname);
        		return;
    		}
    	}         
    }
    
    public synchronized void releaseLocalRedNodeByHostnameFromAll(String hostname) {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		BlueNodeClient cl = new BlueNodeClient(bn);
    		cl.removeRedNodeProjectionByHn(hostname);
    	} 
    }

    public synchronized String[][] buildBNGUIObj() {
    	String[][] object = new String[list.size()][];
    	Iterator<BlueNodeInstance> it = list.listIterator();
        int i=0;
    	while(it.hasNext()) {
        	BlueNodeInstance bn = it.next();
        	object[i] = new String[]{bn.getName(), bn.isTheRemoteAServer(), bn.getPhAddressStr(), ""+bn.getRemoteAuthPort(),""+bn.getUpport(), ""+bn.getDownport()};
        	i++;
        }
    	return object;
    }
    
    public synchronized String[][] buildRRNGUIObj() {
    	//this block calculates the total size of the string array
    	int totalSize = 0;
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()) {
        	BlueNodeInstance bn = it.next();
        	totalSize += bn.table.getSize();
    	}
    	String[][] object =  new String[totalSize][];
    	
    	//this block fills the array with the rrns from each bn
    	it = list.listIterator();
        int i=0;
        while(it.hasNext()) {
         	BlueNodeInstance bn = it.next();
         	Iterator<RemoteRedNodeInstance> rit = bn.table.getList().listIterator(); 
         	while(rit.hasNext()) {
         		RemoteRedNodeInstance rn = rit.next();
         		object[i] = new String[]{rn.getHostname(), rn.getVaddress() , bn.getName(), rn.getTime()};
         		i++;
         	}         	
        }
        return object;
    }
    
    private void notifyGUI() {
    	if (notifyGui) {
    		App.bn.window.updateBNs();
    	}
    }
    
    private void notifyRGUI () {
    	if (notifyGui) {
    		App.bn.window.updateRemoteRns();
    	}
    }

    /*
     * for all the associated blue nodes where the calling bn is
     * a server...
     */
	public void rebuildTableViaAuthClient() {
		Iterator<BlueNodeInstance> iterator = list.listIterator();
    	while (iterator.hasNext()) {
    		BlueNodeInstance element = iterator.next();   
    		if (element.isServer()) {
	            try {
	            	BlueNodeClient cl = new BlueNodeClient(element);
					if (cl.checkBlueNode()) {
						System.out.println(pre+"Fetching RNs from BN "+element.getName());
					    element.updateTime();
					    cl = new BlueNodeClient(element);
					    LinkedList<RemoteRedNodeInstance> rns = cl.getRemoteRedNodesObj(); 
					    LinkedList<RemoteRedNodeInstance> in = element.table.getList();
					    LinkedList<RemoteRedNodeInstance> valid = new LinkedList<RemoteRedNodeInstance>();
					    Iterator<RemoteRedNodeInstance> rnsIt = rns.iterator();
					    
					    while (rnsIt.hasNext()) {
					    	RemoteRedNodeInstance outE = rnsIt.next();				    	
				    		Iterator<RemoteRedNodeInstance> inIt = in.iterator();
				    		while(inIt.hasNext()) {
				    			RemoteRedNodeInstance inE = inIt.next();	
					    		if (inE.getHostname().equals(outE.getHostname())) {
					    			valid.add(inE);			    			
					    		}				    	
				    		}
					    }				    
					    element.table = new RemoteRedNodeTable(element, valid);
					} else {                
						iterator.remove();               
					}
				} catch (Exception e) {
					iterator.remove();  
				}
    		}
    	}
    	System.out.println(pre+" BN Table rebuilt");
    	notifyGUI();    	
	}
}
