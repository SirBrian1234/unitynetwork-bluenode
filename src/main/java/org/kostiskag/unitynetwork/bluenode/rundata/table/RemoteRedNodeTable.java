package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;

/**
 * Here we keep all the remote red nodes leased by each blue node.
 * In other words, every object of BlueNodeInstance keeps an object of this class.
 * 
 * @author Konstantinos Kagiampakis
 */
public class RemoteRedNodeTable {

    private final String pre = "^REDNODE REMOTE TABLE ";
    private final LinkedList<RemoteRedNode> list;
    private final BlueNode blueNode;
    private final boolean verbose;
    private final boolean notifyGui;

    public RemoteRedNodeTable(BlueNode blueNode) {
    	this.blueNode = blueNode;
        list =  new LinkedList<RemoteRedNode>();
        verbose = true;
        notifyGui = true;
		AppLogger.getInstance().consolePrint(pre + "INITIALIZED FOR "+blueNode.getName());
    }
    
    public RemoteRedNodeTable(BlueNode blueNode, LinkedList<RemoteRedNode> list) {
    	this.blueNode = blueNode;
        this.list =  list;
        verbose = true;
        notifyGui = true;
		AppLogger.getInstance().consolePrint(pre + "INITIALIZED FOR "+blueNode.getName());
    }
    
    public RemoteRedNodeTable(BlueNode blueNode, boolean verbose, boolean notifyGui) {
    	this.blueNode = blueNode;
        list =  new LinkedList<RemoteRedNode>();
        this.verbose = verbose;
        this.notifyGui = notifyGui;
        if (verbose) {
			AppLogger.getInstance().consolePrint(pre + "INITIALIZED FOR "+blueNode.getName());
        }
    }
    
    public BlueNode getBlueNode() {
		return blueNode;
	}
    
    public LinkedList<RemoteRedNode> getList() {
		return list;
	}
    
    public int getSize() {
    	return list.size();
    }

    public synchronized RemoteRedNode getByHostname(String hostname) throws Exception {
    	Iterator<RemoteRedNode> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNode rn = it.next();
    		if (rn.getHostname().equals(hostname)) {
    			return rn;
    		}
    	}
    	throw new Exception(pre + "NO ENTRY FOR " + hostname + " IN TABLE");
    }
    
    public synchronized RemoteRedNode getByVaddress(String vAddress) throws Exception {
    	Iterator<RemoteRedNode> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNode rn = it.next();
    		if (rn.getAddress().asString().equals(vAddress)) {
    			return rn;
    		}
    	}
    	throw new Exception(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");        
    }
    
    public synchronized void lease(String hostname, String vAddress) {
    	//the duplicate check is applied at bntable lease rn on bn
		RemoteRedNode rn = null;
		try {
			rn = RemoteRedNode.newInstance(hostname, vAddress, blueNode);
			list.add(rn);
			if (verbose) {
				AppLogger.getInstance().consolePrint(pre +"LEASED " + hostname + " - " + vAddress + " ON BLUE NODE " + blueNode.getName());
			}
			notifyGUI();
		} catch (UnknownHostException | IllegalAccessException e) {
			AppLogger.getInstance().consolePrint(pre +"failed to lease " + hostname + " - " + vAddress + " ON BLUE NODE " + blueNode.getName()+" "+e.getLocalizedMessage());
		}
    }

    public synchronized void releaseByHostname(String hostname) throws Exception {
    	Iterator<RemoteRedNode> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNode rn = it.next();
    		if (rn.getHostname().equals(hostname)) {
    			it.remove();
    			if (verbose) {
					AppLogger.getInstance().consolePrint(pre +"RELEASED " + rn.getHostname() + " - " + rn.getAddress().asString() + " FROM BLUE NODE " + blueNode.getName());
    			}
    			notifyGUI();
    			return;
    		}
    	}
    	throw new Exception(pre + "NO ENTRY FOR " + hostname + " IN TABLE");
    }

    public synchronized void releaseByVaddr(String vAddress) throws Exception {
    	Iterator<RemoteRedNode> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNode rn = it.next();
    		if (rn.getAddress().asString().equals(vAddress)) {
    			it.remove();
    			if (verbose) {
					AppLogger.getInstance().consolePrint(pre +"RELEASED " + rn.getHostname() + " - " + rn.getAddress().asString() + " FROM BLUE NODE " + blueNode.getName());
    			}
    			notifyGUI();
    			return;
    		}
    	}
        throw new Exception(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");
    }
        
    public synchronized Boolean checkByHostname(String hostname) {
        Iterator<RemoteRedNode> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNode rn = it.next();
    		if (rn.getHostname().equals(hostname)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public synchronized Boolean checkByVaddr(String vAddress) {
    	Iterator<RemoteRedNode> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNode rn = it.next();
    		if (rn.getAddress().asString().equals(vAddress)) {
    			return true;
    		}
    	}
        return false;
    }
    
    public synchronized void renewAll() {
    	Iterator<RemoteRedNode> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNode rn = it.next();
    		rn.updateTimestamp();
    	}
    	notifyGUI();
    }
    
    public synchronized void removeAll() {
        list.clear();
        if (verbose) {
			AppLogger.getInstance().consolePrint(pre + "REMOVED ALL ENTRIES FOR BLUENODE "+blueNode.getName());
        }
        notifyGUI();
    }
    
    //no build guiObj here it will be called from  thhe bns table
    //here we just notify
    private void notifyGUI () {
    	if (notifyGui) {
    		MainWindow.getInstance().updateRemoteRns();
    	}
    }
}
