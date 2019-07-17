package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.util.Iterator;
import java.util.LinkedList;
import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNodeInstance;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNodeInstance;

/**
 * Here we keep all the remote red nodes leased by each blue node.
 * In other words, every object of BlueNodeInstance keeps an object of this class.
 * 
 * @author Konstantinos Kagiampakis
 */
public class RemoteRedNodeTable {

    private final String pre = "^REDNODE REMOTE TABLE ";
    private final LinkedList<RemoteRedNodeInstance> list;
    private final BlueNodeInstance blueNode;
    private final boolean verbose;
    private final boolean notifyGui;

    public RemoteRedNodeTable(BlueNodeInstance blueNode) {
    	this.blueNode = blueNode;
        list =  new LinkedList<RemoteRedNodeInstance>();
        verbose = true;
        notifyGui = true;
    	App.bn.ConsolePrint(pre + "INITIALIZED FOR "+blueNode.getName());
    }
    
    public RemoteRedNodeTable(BlueNodeInstance blueNode, LinkedList<RemoteRedNodeInstance> list) {
    	this.blueNode = blueNode;
        this.list =  list;
        verbose = true;
        notifyGui = true;
    	App.bn.ConsolePrint(pre + "INITIALIZED FOR "+blueNode.getName());
    }
    
    public RemoteRedNodeTable(BlueNodeInstance blueNode, boolean verbose, boolean notifyGui) {
    	this.blueNode = blueNode;
        list =  new LinkedList<RemoteRedNodeInstance>();
        this.verbose = verbose;
        this.notifyGui = notifyGui;
        if (verbose) {
        	App.bn.ConsolePrint(pre + "INITIALIZED FOR "+blueNode.getName());
        }
    }
    
    public BlueNodeInstance getBlueNode() {
		return blueNode;
	}
    
    public LinkedList<RemoteRedNodeInstance> getList() {
		return list;
	}
    
    public int getSize() {
    	return list.size();
    }

    public synchronized RemoteRedNodeInstance getByHostname(String hostname) throws Exception {
    	Iterator<RemoteRedNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNodeInstance rn = it.next();
    		if (rn.getHostname().equals(hostname)) {
    			return rn;
    		}
    	}
    	throw new Exception(pre + "NO ENTRY FOR " + hostname + " IN TABLE");
    }
    
    public synchronized RemoteRedNodeInstance getByVaddress(String vAddress) throws Exception {
    	Iterator<RemoteRedNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNodeInstance rn = it.next();
    		if (rn.getVaddress().equals(vAddress)) {
    			return rn;
    		}
    	}
    	throw new Exception(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");        
    }
    
    public synchronized void lease(String hostname, String vAddress) {
    	//the duplicate check is applied at bntable lease rn on bn
    	RemoteRedNodeInstance rn = new RemoteRedNodeInstance(hostname, vAddress, blueNode);
    	list.add(rn);
    	if (verbose) {
    		App.bn.ConsolePrint(pre +"LEASED " + hostname + " - " + vAddress + " ON BLUE NODE " + blueNode.getName());
    	}
    	notifyGUI();
    }

    public synchronized void releaseByHostname(String hostname) throws Exception {
    	Iterator<RemoteRedNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNodeInstance rn = it.next();
    		if (rn.getHostname().equals(hostname)) {
    			it.remove();
    			if (verbose) {
    				App.bn.ConsolePrint(pre +"RELEASED " + rn.getHostname() + " - " + rn.getVaddress() + " FROM BLUE NODE " + blueNode.getName());
    			}
    			notifyGUI();
    			return;
    		}
    	}
    	throw new Exception(pre + "NO ENTRY FOR " + hostname + " IN TABLE");
    }

    public synchronized void releaseByVaddr(String vAddress) throws Exception {
    	Iterator<RemoteRedNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNodeInstance rn = it.next();
    		if (rn.getVaddress().equals(vAddress)) {
    			it.remove();
    			if (verbose) {
    				App.bn.ConsolePrint(pre +"RELEASED " + rn.getHostname() + " - " + rn.getVaddress() + " FROM BLUE NODE " + blueNode.getName());
    			}
    			notifyGUI();
    			return;
    		}
    	}
        throw new Exception(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");
    }
        
    public synchronized Boolean checkByHostname(String hostname) {
        Iterator<RemoteRedNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNodeInstance rn = it.next();
    		if (rn.getHostname().equals(hostname)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public synchronized Boolean checkByVaddr(String vAddress) {
    	Iterator<RemoteRedNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNodeInstance rn = it.next();
    		if (rn.getVaddress().equals(vAddress)) {
    			return true;
    		}
    	}
        return false;
    }
    
    public synchronized void renewAll() {
    	Iterator<RemoteRedNodeInstance> it = list.listIterator();
    	while(it.hasNext()){
    		RemoteRedNodeInstance rn = it.next();
    		rn.updateTime();
    	}
    	notifyGUI();
    }
    
    public synchronized void removeAll() {
        list.clear();
        if (verbose) {
        	App.bn.ConsolePrint(pre + "REMOVED ALL ENTRIES FOR BLUENODE "+blueNode.getName());
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
