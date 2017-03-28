/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.BlueNodeService;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.BlueNodeClient.RemoteHandle;
import kostiskag.unitynetwork.bluenode.RunData.Instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.Routing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class BlueNodeFunctions {

    private static String pre = "^Blue Node Functions";
    static void Associate(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        BlueNodeInstance BNclient = new BlueNodeInstance(hostname, false, connectionSocket);
        if (BNclient.getStatus() > 0) {
            lvl3BlueNode.BlueNodesTable.lease(BNclient);            
        }
    }

    static void FullAssociate(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        BlueNodeInstance BNclient = new BlueNodeInstance(hostname, true, connectionSocket);
        if (BNclient.getStatus() > 0) {
            lvl3BlueNode.BlueNodesTable.lease(BNclient);            
        }
    }

    static void GetRnHostname(String hostname, String RNhostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (lvl3BlueNode.localRedNodesTable.checkOnline(RNhostname)) {
            outputWriter.println("ONLINE "+RNhostname);
        } else {
            outputWriter.println("OFFLINE");
        }
    }

    static void Release(String BlueNodeHostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (lvl3BlueNode.BlueNodesTable.checkBlueNode(BlueNodeHostname)) {
            lvl3BlueNode.remoteRedNodesTable.removeAssociations(BlueNodeHostname);
            lvl3BlueNode.remoteRedNodesTable.updateTable();
            lvl3BlueNode.BlueNodesTable.removeSingle(BlueNodeHostname);
            outputWriter.println("BLUE NODE RELEASED");
            lvl3BlueNode.ConsolePrint(pre + " BLUE NODE " + BlueNodeHostname + " RELEASED HIS ENTRY");
        } else {
            outputWriter.println("BLUE_NODE OFFLINE");
        }
    }

    static void Check(String hostname, String RNhostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (lvl3BlueNode.localRedNodesTable.checkOnline(RNhostname)) {
            outputWriter.println("USER ONLINE");
        } else {
            outputWriter.println("USER OFFLINE");
        }
    }

    static void Uping(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (lvl3BlueNode.BlueNodesTable.checkBlueNode(hostname)) {
            lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(hostname).setUping(false);
        } else {
            outputWriter.println("UPING FAILED");            
            return;
        }
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getUPing()) {
            outputWriter.println("UPING OK");
        } else {
            outputWriter.println("UPING FAILED");
        }
    }

    static void Dping(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        outputWriter.println("DPING SENDING");
        byte[] payload = ("00003 " + lvl3BlueNode.Hostname + " [DPING PACKET]").getBytes();
        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
        lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getQueueMan().offer(data);
    }

    static void GetRNs(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        int size = lvl3BlueNode.localRedNodesTable.getSize();
        outputWriter.println("SENDING_LOCAL_RED_NODES " + size);
        for (int i = 0; i < size; i++) {
            String vaddress = lvl3BlueNode.localRedNodesTable.getRedNodeInstance(i).getVaddress();
            hostname = lvl3BlueNode.localRedNodesTable.getRedNodeInstance(i).getHostname();
            outputWriter.println(vaddress + " " + hostname);
        }
        outputWriter.println();
    }

    static void ExchangeRNs(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        try {
            String clientSentence = null;
            String[] args;
            int size = lvl3BlueNode.localRedNodesTable.getSize();
            outputWriter.println("SENDING_LOCAL_RED_NODES " + size);
            for (int i = 0; i < size; i++) {
                String vaddress = lvl3BlueNode.localRedNodesTable.getRedNodeInstance(i).getVaddress();
                hostname = lvl3BlueNode.localRedNodesTable.getRedNodeInstance(i).getHostname();
                outputWriter.println(vaddress + " " + hostname);
            }
            outputWriter.println();

            clientSentence = inFromClient.readLine();
            lvl3BlueNode.ConsolePrint(pre + clientSentence);
            args = clientSentence.split("\\s+");

            int count = Integer.parseInt(args[1]);
            for (int i = 0; i < count; i++) {
                clientSentence = inFromClient.readLine();
                lvl3BlueNode.ConsolePrint(pre + clientSentence);
                args = clientSentence.split("\\s+");

                if (lvl3BlueNode.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                    lvl3BlueNode.remoteRedNodesTable.lease(args[0], args[1], hostname);
                } else {
                    lvl3BlueNode.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                }
            }
            clientSentence = inFromClient.readLine();
        } catch (IOException ex) {
            Logger.getLogger(BlueNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void FeedReturnRoute(String hostname, String address, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {        
        RemoteHandle.addRemoteRedNode(address, hostname);
        if (lvl3BlueNode.remoteRedNodesTable.checkAssociated(address)) {
            outputWriter.println("OK");
        } else {
            outputWriter.println("FAILED");
        }
    }
}
