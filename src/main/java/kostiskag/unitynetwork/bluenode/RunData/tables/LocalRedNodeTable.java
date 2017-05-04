package kostiskag.unitynetwork.bluenode.RunData.tables;

import java.util.Iterator;
import java.util.LinkedList;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;

/**
 * This an object of this class holds all the local red nodes connected on this bluenode
 * in the form of RedNodeInstance class.
 * The methods are synchronized in order to avoid conflict inside the  table. An external
 * method may request a RedNodeInstance obj by calling either  
 * getRedNodeInstanceByAddr or getRedNodeInstanceByHn
 *
 * @author Konstantinos Kagiampakis
 */
public class LocalRedNodeTable {

    private final String pre = "^LOCAL RN TABLE ";
    private final LinkedList<LocalRedNodeInstance> list;
    private final int maxRednodeEntries;
    private final boolean verbose;
    private final boolean notifyGui;

    public LocalRedNodeTable(int maxRednodeEntries) {
        list = new LinkedList<LocalRedNodeInstance>();
        this.maxRednodeEntries = maxRednodeEntries;
        this.verbose = true;
        this.notifyGui = true;
        App.bn.ConsolePrint(pre +"INIT LOCAL RED NODE TABLE");
    }
    
    public LocalRedNodeTable(int maxRednodeEntries, boolean verbose, boolean notifyGui) {
        list = new LinkedList<LocalRedNodeInstance>();
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
    
    public synchronized LocalRedNodeInstance getRedNodeInstanceByHn(String hostname) {
        Iterator<LocalRedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNodeInstance rn = it.next();
        	if (rn.getHostname().equals(hostname)) {
        		return rn;
        	}
        }
        return null;
    }
    
    public synchronized LocalRedNodeInstance getRedNodeInstanceByAddr(String vaddress) {
        Iterator<LocalRedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNodeInstance rn = it.next();
        	if (rn.getVaddress().equals(vaddress)) {
        		return rn;
        	}
        }
        return null;
    }        

    public synchronized void lease(LocalRedNodeInstance redNode) throws Exception {
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
    
    public synchronized void releaseByHostname(String hostname) throws Exception {
    	Iterator<LocalRedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNodeInstance rn = it.next();
        	if (rn.getHostname().equals(hostname)) {
        		it.remove();
        		if (verbose) {
        			App.bn.ConsolePrint(pre + "RELEASED LOCAL RED NODE "+hostname+" ENTRY FROM TABLE");
        		}
        		notifyGUI();
        		return;
        	}
        }
        throw new Exception(pre + "NO ENTRY FOR " + hostname + " IN TABLE");
    }
    
    /**
     *  This is used in case of a network fall where we have to release all the connected red nodes
     *  but we should not remove them as there is already their socket which will do the remove task
     */
    public synchronized void exitAll() {
    	Iterator<LocalRedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNodeInstance rn = it.next();
        	rn.exit();
        }        
    }
    
    public synchronized Boolean checkOnlineByVaddress(String vAddress) {
        Iterator<LocalRedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNodeInstance rn = it.next();
        	if (rn.getVaddress().equals(vAddress)) {
        		return true;
        	}
        }
        return false;
    }
    
    public synchronized Boolean checkOnlineByHostname(String hostname) {
        Iterator<LocalRedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNodeInstance rn = it.next();
        	if (rn.getHostname().equals(hostname)) {
        		return true;
        	}
        }
        return false;
    }
    
    //builds a string list object with vaddress and hotname for BlueNodeClientFunctions
    public synchronized LinkedList<String> buildAddrHostStringList() {
		LinkedList<String> fetched =  new LinkedList<>();
		Iterator<LocalRedNodeInstance> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNodeInstance rn = it.next();
        	fetched.add(rn.getHostname()+" "+rn.getVaddress());
        }
        return fetched;
	}

    public synchronized String[][] buildGUIObj() {
    	String[][] object = new String[list.size()][];
    	Iterator<LocalRedNodeInstance> it = list.listIterator();
        int i=0;
    	while(it.hasNext()) {
        	LocalRedNodeInstance rn = it.next();
        	object[i] = new String[]{rn.getHostname(), rn.getVaddress(), rn.getPhAddress(), ""+rn.getPort(), ""+rn.getSend().getServerPort(), ""+rn.getReceive().getServerPort()};
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
