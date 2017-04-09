package kostiskag.unitynetwork.bluenode.RunData.Tables;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.Instances.AccountInstance;

/**
 *
 * @author kostis
 */
public class AccountsTable {
    String pre = "^AccountsTable ";
    AccountInstance[] array;
    int count;
    int len;

    public AccountsTable() {        
        array = new AccountInstance[100];
        count = 0;
        len=100;
        App.bn.ConsolePrint(pre+"using local user table");
    }
        
    public void insert(String username, String password, String hostname, String vadress){
        array[count] = new AccountInstance(username, password, hostname, vadress);
        count++;
    }
    
    public String search(String Ghostname, String Gusername, String Gpassword) {
        for (int i=0; i<count; i++){
            String vadress = array[i].check(Gusername, Gpassword, Ghostname);
            if (vadress != null){
                return vadress;
            }
        }
        return null;
    }
    
     public void verbose(){
        for(int i=0; i<count; i++){
            App.bn.ConsolePrint(array[i].verbose());
        }
    }
}
