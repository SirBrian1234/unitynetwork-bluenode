package kostiskag.unitynetwork.bluenode.RunData.tables;

import java.util.Iterator;
import java.util.LinkedList;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.RunData.instances.RedNodeInstance;
import kostiskag.unitynetwork.bluenode.trackClient.TrackingRedNodeFunctions;

/**
 * This an object of this class holds all the local red nodes connected on this bluenode
 * in the form of RedNodeInstance class.
 * The methods are synchronized in order to avoid conflict inside the  table. An external
 * method may request a RedNodeInstance obj by calling either  
 * getRedNodeInstanceByAddr or getRedNodeInstanceByHn
 *
 * @author Konstantinos Kagiampakis
 */
public class LocalRedNodesTable {

    private final String pre = "^LOCAL RN TABLE ";
    private final LinkedList<RedNodeInstance> list;
    private final int maxRednodeEntries;
    private final boolean verbose;
    private final boolean notifyGui;

    public LocalRedNodesTable(int maxRednodeEntries) {
        list = new LinkedList<RedNodeInstance>();
        this.maxRednodeEntries = maxRednodeEntries;
        this.verbose = true;
        this.notifyGui = true;
        App.bn.ConsolePrint(pre +"INIT LOCAL RED NODE TABLE");
    }
    
    public LocalRedNodesTable(int maxRednodeEntries, boolean verbose, boolean notifyGui) {
        list = new LinkedList<RedNodeInstance>();
        this.maxRednodeEntries = maxRednodeEntries;
        this.verbose = verbose;
        this.notifyGui = notifyGui;
        if (verbose) {
        	App.bn.ConsolePrint(pre +"INIT LOCAL RED NODE TABLE");
        }
    }
    
    public synchronized int getSize() {
        return list.size();
    }
    
    public synchronized RedNodeInstance getRedNodeInstanceByHn(String hostname) {
        Iterator<RedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	RedNodeInstance rn = it.next();
        	if (rn.getHostname().equals(hostname)) {
        		return rn;
        	}
        }
        return null;
    }
    
    public synchronized RedNodeInstance getRedNodeInstanceByAddr(String vaddress) {
        Iterator<RedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	RedNodeInstance rn = it.next();
        	if (rn.getVaddress().equals(vaddress)) {
        		return rn;
        	}
        }
        return null;
    }        

    public synchronized void lease(RedNodeInstance redNode) throws Exception {
        if (list.size() < maxRednodeEntries) {
            list.add(redNode);
            if (verbose) {
            	App.bn.ConsolePrint(pre + " LEASED " + redNode.getVaddress() + " ~ " + redNode.getPhAddress());
            }
            notifyGUI();
        } else {
            if (verbose) { 
            	App.bn.ConsolePrint(pre + "MAXIMUM REDNODE CAPACITY REACHED.");
            }
            throw new Exception(pre + "MAXIMUM REDNODE CAPACITY REACHED.");
        }
    }   

    public synchronized void releaseByVaddr(String vAddress) throws Exception {
    	Iterator<RedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	RedNodeInstance rn = it.next();
        	if (rn.getVaddress().equals(vAddress)) {
        		it.remove();
        		//informing tracker
        		if (App.bn.network) {
                    TrackingRedNodeFunctions.release(rn.getHostname());
                }
        		if (verbose) {
        			App.bn.ConsolePrint(pre + "RELEASED ENTRY");
        		}
        		notifyGUI();
        		rn.exit();
        		return;
        	}
        }
        throw new Exception(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");
    }
    
    public synchronized void releaseByHostname(String hostname) throws Exception {
    	Iterator<RedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	RedNodeInstance rn = it.next();
        	if (rn.getHostname().equals(hostname)) {
        		rn.exit();
        		if (App.bn.network) {
                    TrackingRedNodeFunctions.release(rn.getHostname());
                }
        		it.remove();
        		if (verbose) {
        			App.bn.ConsolePrint(pre + "RELEASED ENTRY");
        		}
        		notifyGUI();
        		return;
        	}
        }
        throw new Exception(pre + "NO ENTRY FOR " + hostname + " IN TABLE");
    }
    
    /**
     *  This is used in case of a network fall where we have to release all the connected red nodes
     */
    public synchronized void releaseAll() {
    	Iterator<RedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	RedNodeInstance rn = it.next();
        	rn.exit();
        }
        list.clear();
        if (verbose) {
        	App.bn.ConsolePrint(pre+"LOCAL RED NODE TABLE CLEARED");
        }
        notifyGUI();
    }
    
    public synchronized Boolean checkOnline(String vAddress) {
        Iterator<RedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	RedNodeInstance rn = it.next();
        	if (rn.getVaddress().equals(vAddress)) {
        		return true;
        	}
        }
        return false;
    }
    
    //builds a string list object with vaddress and hotname for BlueNodeClientFunctions
    public synchronized LinkedList<String> buildAddrHostStringList() {
		LinkedList<String> fetched =  new LinkedList<>();
		Iterator<RedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	RedNodeInstance rn = it.next();
        	fetched.add(rn.getVaddress()+" "+rn.getHostname());
        }
        return fetched;
	}

    public synchronized String[][] buildGUIObj() {
    	String[][] object = new String[list.size()][];
    	Iterator<RedNodeInstance> it = list.listIterator();
        int i=0;
    	while(it.hasNext()) {
        	RedNodeInstance rn = it.next();
        	object[i] = new String[]{rn.getVaddress(), rn.getHostname(), rn.getUsername(), rn.getPhAddress(), ""+rn.getUp().getUpport(), ""+rn.getDown().getDownport()};
        	i++;
        }
    	return object;
    }
    
    private void notifyGUI() {
        if (notifyGui) {
            App.bn.window.updateLocalRns();
        }
    }    
}
