package kostiskag.unitynetwork.bluenode.RunData.tables;

import java.util.Iterator;
import java.util.LinkedList;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;
import kostiskag.unitynetwork.bluenode.RunData.instances.RemoteRedNodeInstance;

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
    			bn.killtasks();
    			it.remove();
    			if (verbose) {
    				App.bn.ConsolePrint(pre +"RELEASED BLUE NODE " + bn.getName());
    			}
    			notifyGUI();
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
    
    public synchronized void sendKillSigsAndRelease() {
    	Iterator<BlueNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		BlueNodeInstance bn = it.next();
    		bn.killtasks();
    		//send kill sig    		
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

    public synchronized String[][] buildBNGUIObj() {
    	String[][] object = new String[list.size()][];
    	Iterator<BlueNodeInstance> it = list.listIterator();
        int i=0;
    	while(it.hasNext()) {
        	BlueNodeInstance bn = it.next();
        	object[i] = new String[]{bn.getName(), bn.getPhAddressStr(), ""+bn.getUpport(), ""+bn.getDownport()};
        	i++;
        }
    	return object;
    }
    
    public synchronized String[][] buildRRNGUIObj() {
    	//TODO
    	return null;
    }
    
    private void notifyGUI() {
    	if (notifyGui) {
    		App.bn.window.updateBNs();
    	}
    }
}
