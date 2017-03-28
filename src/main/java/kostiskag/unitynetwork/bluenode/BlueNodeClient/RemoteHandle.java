package kostiskag.unitynetwork.bluenode.BlueNodeClient;

import kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author kostis
 *
 * Here are the functions that handle all the remote associations like: adding a
 * blue node adding a remote red associating checking online
 *
 * These functions have 2 goals they connect to the remote blue node they set
 * all the remote tables with data they use ClientFunctions to connect
 * ClientFunctions use socketFunctions
 */
public class RemoteHandle {

    public static String pre = "^ADDREMOTE ";

    //blue node stuff  
    public static void addBlueNode(String address, int authport, String AuthHostname, boolean exclusive, boolean full) {
        BlueNodeClientFunctions.addRemoteBlueNode(address, authport, AuthHostname, exclusive, full);
    }

    public static void addBlueNodeWithExchange(String BNHostname, String address, int blueauthport, String sourcevaddress, String destvaddress) {                        
        
        if (blueauthport<=0){
            blueauthport = 7000;
        } 

        BlueNodeClientFunctions.addRemoteBlueNode(address, blueauthport, BNHostname, true, false);
        if (App.BlueNodesTable.checkBlueNode(BNHostname)) {
            addRemoteRedNode(destvaddress, BNHostname);
            FeedReturnRoute(BNHostname, sourcevaddress);
        }
    }

    public static void BlueNodeExchange(String BNHostname, String sourcevaddress, String destvaddress) {
        if (App.BlueNodesTable.checkBlueNode(BNHostname)) {
            addRemoteRedNode(destvaddress, BNHostname);
            RemoteHandle.FeedReturnRoute(BNHostname, sourcevaddress);
        }
    }

    public static void removeBlueNode(String BlueNodeHostname) {
        App.ConsolePrint(pre + "REMOVING 1 BLUE NODE");
        removeBlueNodesRedAssociations(BlueNodeHostname);
        BlueNodeClientFunctions.removeBlueNodeProjection(BlueNodeHostname);
        App.remoteRedNodesTable.updateTable();
        App.BlueNodesTable.removeSingle(BlueNodeHostname);
    }

    public static void upingBlueNodes(int[] table) {
        for (int i = table.length; i > 0; i--) {
            String hostname = App.BlueNodesTable.getBlueNodeInstance(table[i - 1]).getHostname();
            if (!BlueNodeClientFunctions.checkBlueNode(hostname)) {               
                removeBlueNodesRedAssociations(table[i - 1]);
                App.BlueNodesTable.removeSingle(hostname);
            }
        }
        App.BlueNodesTable.updateTable();
        App.remoteRedNodesTable.updateTable();
    }

    public static void removeBlueNodes(int[] table) {
        App.ConsolePrint(pre + "REMOVING PROJECTIONS FOR " + table.length + " BLUE NODES");
        for (int i = table.length; i > 0; i--) {
            removeBlueNodesRedAssociations(i - 1);
            BlueNodeClientFunctions.removeBlueNodeProjection(i - 1);
        }
        App.remoteRedNodesTable.updateTable();
        App.BlueNodesTable.delete(table);
    }

    //red node stuff
    public static void addRemoteRedNode(String vaddress, String BlueNodeHostname) {
        //first we check if bluenode is on table - kathisteraaas prwta eleghoume ama uparxei
        if (!App.remoteRedNodesTable.checkAssociated(vaddress)) {
            App.ConsolePrint(pre + "ADDING REMOTE RED NODE " + vaddress + " " + BlueNodeHostname);
            if (App.BlueNodesTable.checkBlueNode(BlueNodeHostname) == false) {
                App.ConsolePrint(pre + "NO BLUE NODE FOUND ON DATABASE REGISTER THE BLUE NODE FIRST");
            } else {
                App.ConsolePrint(pre + "USING A REASSOSIATED BLUENODE");
                //go and ask bn if he knows the guy            
                String redhostname = BlueNodeClientFunctions.getRedHostname(BlueNodeHostname, vaddress);
                if (redhostname.equals("OFFLINE")) {
                    App.ConsolePrint(pre + "REMOTE RED NODE " + vaddress + " IS NOT ON THIS BLUE NODE");
                } else if (!redhostname.equals("")) {
                    App.remoteRedNodesTable.lease(vaddress, redhostname, BlueNodeHostname);
                    App.ConsolePrint(pre + "REMOTE RED NODE " + vaddress + " ~ " + redhostname + " LEASED");
                } else {
                    App.ConsolePrint(pre + "REMOTE RED NODE " + vaddress + " FAILED");
                }
            }
        }
    }

    public static void GetRemoteRedNodes(int[] table) {
        for (int i = table.length; i > 0; i--) {
            String hostname = App.BlueNodesTable.getBlueNodeInstance(table[i - 1]).getHostname();
            App.ConsolePrint(pre + "GETTING REMOTE RED NODES FROM " + hostname);
            if (BlueNodeClientFunctions.getRemoteRedNodesU(hostname) == -1) {
                removeBlueNodesRedAssociations(table[i - 1]);
                App.BlueNodesTable.removeSingle(hostname);
            }
        }
        App.remoteRedNodesTable.updateTable();
    }

    public static void ExchangeRedNodes(int[] table) {
        for (int i = table.length; i > 0; i--) {
            String hostname = App.BlueNodesTable.getBlueNodeInstance(table[i - 1]).getHostname();
            App.ConsolePrint(pre + "EXCHANGING RED NODES WITH " + hostname);
            if (BlueNodeClientFunctions.ExchangeRedNodesU(hostname) == -1) {
                removeBlueNodesRedAssociations(table[i - 1]);
                App.BlueNodesTable.removeSingle(hostname);
            }
        }
        App.remoteRedNodesTable.updateTable();
    }

    public static void checkRemoteRedNodes(int[] table) {
        String vaddress = null;
        int out = 0;
        int bnid = 0;
        String BNHostname;

        for (int i = table.length; i > 0; i--) {
            vaddress = App.remoteRedNodesTable.getRedRemoteAddress(table[i - 1]).getVAddress();
            App.ConsolePrint(pre + "CHECKING REMOTE RED NODE " + vaddress);
            out = BlueNodeClientFunctions.checkRemoteRedNode(vaddress);
            if (out == 1) {
                App.ConsolePrint(pre + "UPDATING " + vaddress);
                App.remoteRedNodesTable.getRedRemoteAddress(vaddress).updateTime();
            } else if (out == 0) {
                App.remoteRedNodesTable.releaseByAddr(vaddress);
            } else {
                BNHostname = App.remoteRedNodesTable.getRedRemoteAddress(table[i - 1]).getBlueNodeHostname();
                App.ConsolePrint(pre + "KILLING OFFLINE BLUENODE " + BNHostname);
                App.BlueNodesTable.removeSingle(BNHostname);
                App.remoteRedNodesTable.releaseByAddr(vaddress);
            }
        }
        App.remoteRedNodesTable.updateTable();
    }

    public static void removeBlueNodesRedAssociations(int i) {
        String hostname = App.BlueNodesTable.getBlueNodeInstance(i).getHostname();
        App.remoteRedNodesTable.removeAssociations(hostname);
    }

    public static void removeBlueNodesRedAssociations(String BlueNodeHostname) {
        App.remoteRedNodesTable.removeAssociations(BlueNodeHostname);
    }

    public static void FeedReturnRoute(String BNHostname, String sourcevaddress) {
        BlueNodeClientFunctions.feedReturnRoute(BNHostname, sourcevaddress);
    }
}
