package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.util.Iterator;
import java.util.LinkedList;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.LocalRedNode;

/**
 * This an object of this class holds all the local red nodes connected on this bluenode
 * in the form of RedNodeInstance class.
 * The methods are synchronized in order to avoid conflict inside the  table. An external
 * method may request a RedNodeInstance obj by calling either  
 * getRedNodeInstanceByAddr or getRedNodeInstanceByHn
 *
 * @author Konstantinos Kagiampakis
 */
public final class LocalRedNodeTable {

    private static final String PRE = "^LOCAL RN TABLE ";
    private static LocalRedNodeTable LOCAL_REDNODE_TABLE;

    private final LinkedList<LocalRedNode> list;
    private final int maxRednodeEntries;
    private final boolean verbose;
    private final boolean notifyGui;

    public static LocalRedNodeTable newInstance(int maxEntries) {
        if (LOCAL_REDNODE_TABLE == null) {
            LOCAL_REDNODE_TABLE = new LocalRedNodeTable(maxEntries);
        }
        return LOCAL_REDNODE_TABLE;
    }

    public static LocalRedNodeTable getInstance() {
        return LOCAL_REDNODE_TABLE;
    }

    public LocalRedNodeTable(int maxRednodeEntries) {
        list = new LinkedList<LocalRedNode>();
        this.maxRednodeEntries = maxRednodeEntries;
        this.verbose = true;
        this.notifyGui = true;
        AppLogger.getInstance().consolePrint(PRE +"INIT LOCAL RED NODE TABLE");
    }
    
    public LocalRedNodeTable(int maxRednodeEntries, boolean verbose, boolean notifyGui) {
        list = new LinkedList<LocalRedNode>();
        this.maxRednodeEntries = maxRednodeEntries;
        this.verbose = verbose;
        this.notifyGui = notifyGui;
        if (verbose) {
            AppLogger.getInstance().consolePrint(PRE +"INIT LOCAL RED NODE TABLE");
        }
    }
    
    public synchronized int getSize() {
        return list.size();
    }
    
    public synchronized LocalRedNode getRedNodeInstanceByHn(String hostname) {
        Iterator<LocalRedNode> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNode rn = it.next();
        	if (rn.getHostname().equals(hostname)) {
        		return rn;
        	}
        }
        return null;
    }
    
    public synchronized LocalRedNode getRedNodeInstanceByAddr(String vaddress) {
        Iterator<LocalRedNode> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNode rn = it.next();
        	if (rn.getVaddress().equals(vaddress)) {
        		return rn;
        	}
        }
        return null;
    }        

    public synchronized void lease(LocalRedNode redNode) throws Exception {
        if (list.size() < maxRednodeEntries) {
            list.add(redNode);
            if (verbose) {
                AppLogger.getInstance().consolePrint(PRE + " LEASED " + redNode.getVaddress() + " ~ " + redNode.getPhAddress());
            }
            notifyGUI();
        } else {
            if (verbose) {
                AppLogger.getInstance().consolePrint(PRE + "MAXIMUM REDNODE CAPACITY REACHED.");
            }
            throw new Exception(PRE + "MAXIMUM REDNODE CAPACITY REACHED.");
        }
    }   
    
    public synchronized void releaseByHostname(String hostname) throws Exception {
    	Iterator<LocalRedNode> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNode rn = it.next();
        	if (rn.getHostname().equals(hostname)) {
        		it.remove();
        		if (verbose) {
                    AppLogger.getInstance().consolePrint(PRE + "RELEASED LOCAL RED NODE "+hostname+" ENTRY FROM TABLE");
        		}
        		notifyGUI();
        		return;
        	}
        }
        throw new Exception(PRE + "NO ENTRY FOR " + hostname + " IN TABLE");
    }
    
    /**
     *  This is used in case of a network fall where we have to release all the connected red nodes
     *  but we should not remove them as there is already their socket which will do the remove task
     */
    public synchronized void exitAll() {
    	Iterator<LocalRedNode> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNode rn = it.next();
        	rn.exit();
        }        
    }
    
    public synchronized Boolean checkOnlineByVaddress(String vAddress) {
        Iterator<LocalRedNode> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNode rn = it.next();
        	if (rn.getVaddress().equals(vAddress)) {
        		return true;
        	}
        }
        return false;
    }
    
    public synchronized Boolean checkOnlineByHostname(String hostname) {
        Iterator<LocalRedNode> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNode rn = it.next();
        	if (rn.getHostname().equals(hostname)) {
        		return true;
        	}
        }
        return false;
    }
    
    //builds a string list object with vaddress and hotname for BlueNodeClientFunctions
    public synchronized LinkedList<String> buildAddrHostStringList() {
		LinkedList<String> fetched =  new LinkedList<>();
		Iterator<LocalRedNode> it = list.listIterator();
        while(it.hasNext()) {
        	LocalRedNode rn = it.next();
        	fetched.add(rn.getHostname()+" "+rn.getVaddress());
        }
        return fetched;
	}

    public synchronized String[][] buildGUIObj() {
    	String[][] object = new String[list.size()][];
    	Iterator<LocalRedNode> it = list.listIterator();
        int i=0;
    	while(it.hasNext()) {
        	LocalRedNode rn = it.next();
        	object[i] = new String[]{rn.getHostname(), rn.getVaddress(), rn.getPhAddress(), ""+rn.getPort(), ""+rn.getSend().getServerPort(), ""+rn.getReceive().getServerPort()};
        	i++;
        }
    	return object;
    }
    
    private void notifyGUI() {
        if (notifyGui) {
            MainWindow.getInstance().updateLocalRns(buildGUIObj());
        }
    }    
}
