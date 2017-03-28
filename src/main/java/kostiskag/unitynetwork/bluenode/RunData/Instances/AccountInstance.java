/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.RunData.Instances;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.Functions.MD5Functions;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.Functions.*;

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
            this.password = MD5Functions.MD5(Username + "lol!_you_just_cant_copy_hashes_and_use_them_from_the_webpage" + MD5Functions.MD5(password));
            this.hostname = hostname;        
            this.vadress = ipAddrFunctions._10ipAddrToNumber(vadress);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AccountInstance.class.getName()).log(Level.SEVERE, null, ex);
            lvl3BlueNode.die();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AccountInstance.class.getName()).log(Level.SEVERE, null, ex);
            lvl3BlueNode.die();
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
