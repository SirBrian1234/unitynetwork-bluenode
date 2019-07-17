package org.kostiskag.unitynetwork.bluenode.rundata.entry;


import java.security.GeneralSecurityException;
import java.util.Objects;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.HashUtilities;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

/**
 * An object of the accounts instance loads the user details when a host.list is
 * being used. 
 *
 * @author Konstantinos Kagiampakis
 */
public final class LocalAccount {

    private final String username;
    private final String password;
    private final String hostname;
    private final VirtualAddress vaddress;

    public LocalAccount(String username, String password, String hostname, VirtualAddress vadress) throws GeneralSecurityException {
            this.username = username;
            //derive from common
            this.password = HashUtilities.SHA256(HashUtilities.SHA256(CryptoUtilities.SALT) +  HashUtilities.SHA256(username) + HashUtilities.SHA256(CryptoUtilities.SALT + password));
            this.hostname = hostname;        
            this.vaddress = vadress;
    }        
    
    public String getUsername() {
		return username;
	}
    
    public String getPassword() {
		return password;
	}
    
    public String getHostname() {
		return hostname;
	}
    
    public VirtualAddress getVaddress() {
		return vaddress;
	}

    public boolean check(String username, String password, String hostname) {
        if (this.username.equals(username) && this.password.equals(password) && this.hostname.equals(hostname)){
            return true;
        } else {
        	return false;
        }
    } 

    //get optional of VirtualAddress
    public VirtualAddress checkAndGetVaddr(String username, String password, String hostname) {
        if (this.username.equals(username) && this.password.equals(password) && this.hostname.equals(hostname)){
            return vaddress;
        } else {
        	return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalAccount that = (LocalAccount) o;
        return hostname.equals(that.hostname) ||
                vaddress.equals(that.vaddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, hostname, vaddress);
    }

    @Override
    public String toString(){
        return username+" "+password+" "+hostname+" "+vaddress.asString();
    }
}
