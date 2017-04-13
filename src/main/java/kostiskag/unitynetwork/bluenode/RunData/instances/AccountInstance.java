package kostiskag.unitynetwork.bluenode.RunData.instances;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.functions.HashFunctions;
import kostiskag.unitynetwork.bluenode.functions.ipAddrFunctions;

/**
 *
 * @author kostis
 */
public class AccountInstance {
    private String Username;
    private String password;
    private String vadress;
    private String hostname;

    public AccountInstance(String Username, String password, String hostname, String vadress) {
        try {
            this.Username = Username;
            this.password = HashFunctions.SHA256(HashFunctions.SHA256(App.SALT) +  HashFunctions.SHA256(Username) + HashFunctions.SHA256(App.SALT) +  HashFunctions.SHA256(password));			
            this.hostname = hostname;        
            this.vadress = ipAddrFunctions._10ipAddrToNumber(vadress);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AccountInstance.class.getName()).log(Level.SEVERE, null, ex);
            App.bn.die();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AccountInstance.class.getName()).log(Level.SEVERE, null, ex);
            App.bn.die();
        }
    }        
    
    public String check(String GUsername, String Gpassword, String Ghostname){
        if (GUsername.equals(Username) && Gpassword.equals(password) && Ghostname.equals(hostname)){
            return vadress;
        }
        return null;
    }               

    public String verbose() {
        return Username+" "+password+" "+hostname+" "+vadress;
    }
}
