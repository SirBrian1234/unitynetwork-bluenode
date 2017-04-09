package kostiskag.unitynetwork.bluenode.RunData;

import kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author kostis
 */
public class IPpoll {
    
    int count;
    
    public IPpoll() {
        count = 0;
        App.bn.ConsolePrint("GRAVE WARNING BLUENODE DOES NOT USE EITHER NETWORK NOR A USERLIST\nTHAT MEANS THAT ANYONE WHO KNOWS BN AND IS PHYSICALY ABLE TO CONNECT CAN LOGIN");
    }
    
    public String poll(){
        count++;
        return new String(""+count);
    }
    
    public static void main(String[] args) {
        IPpoll poll = new IPpoll();
    }
}
