package org.kostiskag.unitynetwork.bluenode.RunData.instances;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.common.utilities.HashUtilities;

/**
 * An object of the accounts instance loads the user details when a host.list is
 * being used. 
 *
 * @author Konstantinos Kagiampakis
 */
public class AccountInstance {
    private final String username;
    private final String password;
    private final String hostname;
    private final String vaddress;

    public AccountInstance(String username, String password, String hostname, String vadress) throws NoSuchAlgorithmException, UnsupportedEncodingException {
            this.username = username;
            this.password = HashUtilities.SHA256(HashUtilities.SHA256(App.SALT) +  HashUtilities.SHA256(username) + HashUtilities.SHA256(App.SALT + password));
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
    
    public String getVaddress() {
		return vaddress;
	}
    
    public boolean check(String username, String password, String hostname){
        if (this.username.equals(username) && this.password.equals(password) && this.hostname.equals(hostname)){
            return true;
        } else {
        	return false;
        }
    } 
    
    public String checkAndGetVaddr(String username, String password, String hostname){
        if (this.username.equals(username) && this.password.equals(password) && this.hostname.equals(hostname)){
            return vaddress;
        } else {
        	return null;
        }
    }               

    public String toString(){
        return username+" "+password+" "+hostname+" "+vaddress;
    }
}
