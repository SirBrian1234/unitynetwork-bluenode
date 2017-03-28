/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.BlueNodeClient;

import kostiskag.unitynetwork.bluenode.BlueNode.*;

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
        if (lvl3BlueNode.BlueNodesTable.checkBlueNode(BNHostname)) {
            addRemoteRedNode(destvaddress, BNHostname);
            FeedReturnRoute(BNHostname, sourcevaddress);
        }
    }

    public static void BlueNodeExchange(String BNHostname, String sourcevaddress, String destvaddress) {
        if (lvl3BlueNode.BlueNodesTable.checkBlueNode(BNHostname)) {
            addRemoteRedNode(destvaddress, BNHostname);
            RemoteHandle.FeedReturnRoute(BNHostname, sourcevaddress);
        }
    }

    public static void removeBlueNode(String BlueNodeHostname) {
        lvl3BlueNode.ConsolePrint(pre + "REMOVING 1 BLUE NODE");
        removeBlueNodesRedAssociations(BlueNodeHostname);
        BlueNodeClientFunctions.removeBlueNodeProjection(BlueNodeHostname);
        lvl3BlueNode.remoteRedNodesTable.updateTable();
        lvl3BlueNode.BlueNodesTable.removeSingle(BlueNodeHostname);
    }

    public static void upingBlueNodes(int[] table) {
        for (int i = table.length; i > 0; i--) {
            String hostname = lvl3BlueNode.BlueNodesTable.getBlueNodeInstance(table[i - 1]).getHostname();
            if (!BlueNodeClientFunctions.checkBlueNode(hostname)) {               
                removeBlueNodesRedAssociations(table[i - 1]);
                lvl3BlueNode.BlueNodesTable.removeSingle(hostname);
            }
        }
        lvl3BlueNode.BlueNodesTable.updateTable();
        lvl3BlueNode.remoteRedNodesTable.updateTable();
    }

    public static void removeBlueNodes(int[] table) {
        lvl3BlueNode.ConsolePrint(pre + "REMOVING PROJECTIONS FOR " + table.length + " BLUE NODES");
        for (int i = table.length; i > 0; i--) {
            removeBlueNodesRedAssociations(i - 1);
            BlueNodeClientFunctions.removeBlueNodeProjection(i - 1);
        }
        lvl3BlueNode.remoteRedNodesTable.updateTable();
        lvl3BlueNode.BlueNodesTable.delete(table);
    }

    //red node stuff
    public static void addRemoteRedNode(String vaddress, String BlueNodeHostname) {
        //first we check if bluenode is on table - kathisteraaas prwta eleghoume ama uparxei
        if (!lvl3BlueNode.remoteRedNodesTable.checkAssociated(vaddress)) {
            lvl3BlueNode.ConsolePrint(pre + "ADDING REMOTE RED NODE " + vaddress + " " + BlueNodeHostname);
            if (lvl3BlueNode.BlueNodesTable.checkBlueNode(BlueNodeHostname) == false) {
                lvl3BlueNode.ConsolePrint(pre + "NO BLUE NODE FOUND ON DATABASE REGISTER THE BLUE NODE FIRST");
            } else {
                lvl3BlueNode.ConsolePrint(pre + "USING A REASSOSIATED BLUENODE");
                //go and ask bn if he knows the guy            
                String redhostname = BlueNodeClientFunctions.getRedHostname(BlueNodeHostname, vaddress);
                if (redhostname.equals("OFFLINE")) {
                    lvl3BlueNode.ConsolePrint(pre + "REMOTE RED NODE " + vaddress + " IS NOT ON THIS BLUE NODE");
                } else if (!redhostname.equals("")) {
                    lvl3BlueNode.remoteRedNodesTable.lease(vaddress, redhostname, BlueNodeHostname);
                    lvl3BlueNode.ConsolePrint(pre + "REMOTE RED NODE " + vaddress + " ~ " + redhostname + " LEASED");
                } else {
                    lvl3BlueNode.ConsolePrint(pre + "REMOTE RED NODE " + vaddress + " FAILED");
                }
            }
        }
    }

    public static void GetRemoteRedNodes(int[] table) {
        for (int i = table.length; i > 0; i--) {
            String hostname = lvl3BlueNode.BlueNodesTable.getBlueNodeInstance(table[i - 1]).getHostname();
            lvl3BlueNode.ConsolePrint(pre + "GETTING REMOTE RED NODES FROM " + hostname);
            if (BlueNodeClientFunctions.getRemoteRedNodesU(hostname) == -1) {
                removeBlueNodesRedAssociations(table[i - 1]);
                lvl3BlueNode.BlueNodesTable.removeSingle(hostname);
            }
        }
        lvl3BlueNode.remoteRedNodesTable.updateTable();
    }

    public static void ExchangeRedNodes(int[] table) {
        for (int i = table.length; i > 0; i--) {
            String hostname = lvl3BlueNode.BlueNodesTable.getBlueNodeInstance(table[i - 1]).getHostname();
            lvl3BlueNode.ConsolePrint(pre + "EXCHANGING RED NODES WITH " + hostname);
            if (BlueNodeClientFunctions.ExchangeRedNodesU(hostname) == -1) {
                removeBlueNodesRedAssociations(table[i - 1]);
                lvl3BlueNode.BlueNodesTable.removeSingle(hostname);
            }
        }
        lvl3BlueNode.remoteRedNodesTable.updateTable();
    }

    public static void checkRemoteRedNodes(int[] table) {
        String vaddress = null;
        int out = 0;
        int bnid = 0;
        String BNHostname;

        for (int i = table.length; i > 0; i--) {
            vaddress = lvl3BlueNode.remoteRedNodesTable.getRedRemoteAddress(table[i - 1]).getVAddress();
            lvl3BlueNode.ConsolePrint(pre + "CHECKING REMOTE RED NODE " + vaddress);
            out = BlueNodeClientFunctions.checkRemoteRedNode(vaddress);
            if (out == 1) {
                lvl3BlueNode.ConsolePrint(pre + "UPDATING " + vaddress);
                lvl3BlueNode.remoteRedNodesTable.getRedRemoteAddress(vaddress).updateTime();
            } else if (out == 0) {
                lvl3BlueNode.remoteRedNodesTable.releaseByAddr(vaddress);
            } else {
                BNHostname = lvl3BlueNode.remoteRedNodesTable.getRedRemoteAddress(table[i - 1]).getBlueNodeHostname();
                lvl3BlueNode.ConsolePrint(pre + "KILLING OFFLINE BLUENODE " + BNHostname);
                lvl3BlueNode.BlueNodesTable.removeSingle(BNHostname);
                lvl3BlueNode.remoteRedNodesTable.releaseByAddr(vaddress);
            }
        }
        lvl3BlueNode.remoteRedNodesTable.updateTable();
    }

    public static void removeBlueNodesRedAssociations(int i) {
        String hostname = lvl3BlueNode.BlueNodesTable.getBlueNodeInstance(i).getHostname();
        lvl3BlueNode.remoteRedNodesTable.removeAssociations(hostname);
    }

    public static void removeBlueNodesRedAssociations(String BlueNodeHostname) {
        lvl3BlueNode.remoteRedNodesTable.removeAssociations(BlueNodeHostname);
    }

    public static void FeedReturnRoute(String BNHostname, String sourcevaddress) {
        BlueNodeClientFunctions.feedReturnRoute(BNHostname, sourcevaddress);
    }
}
