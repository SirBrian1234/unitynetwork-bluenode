package kostiskag.unitynetwork.bluenode.blueNodeService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Thread.sleep;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.blueNodeClient.RemoteHandle;
import kostiskag.unitynetwork.bluenode.Routing.*;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

/**
 *
 * @author kostis
 */
public class BlueNodeFunctions {

    private static String pre = "^Blue Node Functions";
    static void Associate(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        BlueNodeInstance BNclient = new BlueNodeInstance(hostname, false, connectionSocket);
        if (BNclient.getStatus() > 0) {
            App.bn.blueNodesTable.lease(BNclient);            
        }
    }

    static void FullAssociate(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        BlueNodeInstance BNclient = new BlueNodeInstance(hostname, true, connectionSocket);
        if (BNclient.getStatus() > 0) {
            App.bn.blueNodesTable.lease(BNclient);            
        }
    }

    static void GetRnHostname(String hostname, String RNhostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (App.bn.localRedNodesTable.checkOnline(RNhostname)) {
            outputWriter.println("ONLINE "+RNhostname);
        } else {
            outputWriter.println("OFFLINE");
        }
    }

    static void Release(String BlueNodeHostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (App.bn.blueNodesTable.checkBlueNode(BlueNodeHostname)) {
            App.bn.remoteRedNodesTable.removeAssociations(BlueNodeHostname);
            App.bn.remoteRedNodesTable.updateTable();
            App.bn.blueNodesTable.removeSingle(BlueNodeHostname);
            outputWriter.println("BLUE NODE RELEASED");
            App.bn.ConsolePrint(pre + " BLUE NODE " + BlueNodeHostname + " RELEASED HIS ENTRY");
        } else {
            outputWriter.println("BLUE_NODE OFFLINE");
        }
    }

    static void Check(String hostname, String RNhostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (App.bn.localRedNodesTable.checkOnline(RNhostname)) {
            outputWriter.println("USER ONLINE");
        } else {
            outputWriter.println("USER OFFLINE");
        }
    }

    static void Uping(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (App.bn.blueNodesTable.checkBlueNode(hostname)) {
            App.bn.blueNodesTable.getBlueNodeInstanceByHn(hostname).setUping(false);
        } else {
            outputWriter.println("UPING FAILED");            
            return;
        }
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (App.bn.blueNodesTable.getBlueNodeInstanceByHn(hostname).getUPing()) {
            outputWriter.println("UPING OK");
        } else {
            outputWriter.println("UPING FAILED");
        }
    }

    static void Dping(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        outputWriter.println("DPING SENDING");
        byte[] payload = ("00003 " + App.bn.name + " [DPING PACKET]").getBytes();
        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
        App.bn.blueNodesTable.getBlueNodeInstanceByHn(hostname).getQueueMan().offer(data);
    }

    static void GetRNs(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        int size = App.bn.localRedNodesTable.getSize();
        outputWriter.println("SENDING_LOCAL_RED_NODES " + size);
        for (int i = 0; i < size; i++) {
            String vaddress = App.bn.localRedNodesTable.getRedNodeInstance(i).getVaddress();
            hostname = App.bn.localRedNodesTable.getRedNodeInstance(i).getHostname();
            outputWriter.println(vaddress + " " + hostname);
        }
        outputWriter.println();
    }

    static void ExchangeRNs(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        try {
            String clientSentence = null;
            String[] args;
            int size = App.bn.localRedNodesTable.getSize();
            outputWriter.println("SENDING_LOCAL_RED_NODES " + size);
            for (int i = 0; i < size; i++) {
                String vaddress = App.bn.localRedNodesTable.getRedNodeInstance(i).getVaddress();
                hostname = App.bn.localRedNodesTable.getRedNodeInstance(i).getHostname();
                outputWriter.println(vaddress + " " + hostname);
            }
            outputWriter.println();

            clientSentence = inFromClient.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            args = clientSentence.split("\\s+");

            int count = Integer.parseInt(args[1]);
            for (int i = 0; i < count; i++) {
                clientSentence = inFromClient.readLine();
                App.bn.ConsolePrint(pre + clientSentence);
                args = clientSentence.split("\\s+");

                if (App.bn.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                    App.bn.remoteRedNodesTable.lease(args[0], args[1], hostname);
                } else {
                    App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                }
            }
            clientSentence = inFromClient.readLine();
        } catch (IOException ex) {
            Logger.getLogger(BlueNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void FeedReturnRoute(String hostname, String address, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {        
        RemoteHandle.addRemoteRedNode(address, hostname);
        if (App.bn.remoteRedNodesTable.checkAssociated(address)) {
            outputWriter.println("OK");
        } else {
            outputWriter.println("FAILED");
        }
    }
}
