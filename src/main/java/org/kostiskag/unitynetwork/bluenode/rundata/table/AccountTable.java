package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.LocalAccount;


/**
 * When a local host.list is imported the data may be transfered
 * to an object of this class.
 *
 * @author Konstantinos Kagiampakis
 */
public class AccountTable { //todo extend simpleTable
    private final String pre = "^AccountsTable ";
    private final List<LocalAccount> list;

    public AccountTable() {
        list = new LinkedList<LocalAccount>();
    }
    
    public int getSize() {
    	return list.size();
    }
        
    public synchronized void insert(String username, String password, String hostname, int vadressNum) throws Exception {
        //data validate
    	if (password != null && hostname != null) {
    		if (!username.isEmpty() && !password.isEmpty() && !hostname.isEmpty()) {
    			if (vadressNum > 0 && vadressNum <= (NumericConstraints.VIRTUAL_NETWORK_ADDRESS_EFFECTIVE_CAPACITY.size())) {
	    			//check if unique
	    			VirtualAddress effectveVaddress = VirtualAddress.valueOf(vadressNum);
	    			Iterator<LocalAccount> it = list.listIterator();
	    	        while(it.hasNext()) {
	    	        	LocalAccount element = it.next();
	    	        	if (element.getHostname().equals(hostname) || element.getVaddress().equals(effectveVaddress)) {
	    	        		throw new Exception(pre+"duplicate hostname or vaddress entry.\nHostnames and addresses have to be unique");
	    	        	}
	    	        }
	    	        //insert
	    	        list.add(new LocalAccount(username, password, hostname, effectveVaddress));
    			} else {
    				throw new Exception(pre+"bad numeric address was given.");
    			}
    		} else {
    			throw new Exception(pre+"bad data were given.");
    		}
    	} else {
    		throw new Exception(pre+"null data were given.");
    	}
    }
    
    public synchronized boolean checkList(String hostname, String username, String password) {
        Iterator<LocalAccount> it = list.listIterator();
        while(it.hasNext()) {
        	LocalAccount element = it.next();
        	if (element.check(username, password, hostname)) {
        		return true;
        	}
        }
        return false;
    }
    
    public synchronized VirtualAddress getVaddrIfExists(String hostname, String username, String password) {
        Iterator<LocalAccount> it = list.listIterator();
        while(it.hasNext()) {
        	LocalAccount element = it.next();
        	VirtualAddress vaddr = element.checkAndGetVaddr(username, password, hostname);
        	if (vaddr != null) {
        		return vaddr;
        	}
        }
        return null;
    }

    @Override
	public synchronized String toString() {
		StringBuilder strB = new StringBuilder();
		for(LocalAccount a : this.list) {
			strB.append(a.toString()+"\n");
		}
    	return strB.toString();
    }
}
