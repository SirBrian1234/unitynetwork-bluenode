package kostiskag.unitynetwork.bluenode.BlueNodeService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Thread.sleep;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.BlueNodeClient.RemoteHandle;
import kostiskag.unitynetwork.bluenode.RunData.Instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.Routing.*;

/**
 *
 * @author kostis
 */
public class BlueNodeFunctions {

    private static String pre = "^Blue Node Functions";
    static void Associate(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        BlueNodeInstance BNclient = new BlueNodeInstance(hostname, false, connectionSocket);
        if (BNclient.getStatus() > 0) {
            App.BlueNodesTable.lease(BNclient);            
        }
    }

    static void FullAssociate(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        BlueNodeInstance BNclient = new BlueNodeInstance(hostname, true, connectionSocket);
        if (BNclient.getStatus() > 0) {
            App.BlueNodesTable.lease(BNclient);            
        }
    }

    static void GetRnHostname(String hostname, String RNhostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (App.localRedNodesTable.checkOnline(RNhostname)) {
            outputWriter.println("ONLINE "+RNhostname);
        } else {
            outputWriter.println("OFFLINE");
        }
    }

    static void Release(String BlueNodeHostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (App.BlueNodesTable.checkBlueNode(BlueNodeHostname)) {
            App.remoteRedNodesTable.removeAssociations(BlueNodeHostname);
            App.remoteRedNodesTable.updateTable();
            App.BlueNodesTable.removeSingle(BlueNodeHostname);
            outputWriter.println("BLUE NODE RELEASED");
            App.ConsolePrint(pre + " BLUE NODE " + BlueNodeHostname + " RELEASED HIS ENTRY");
        } else {
            outputWriter.println("BLUE_NODE OFFLINE");
        }
    }

    static void Check(String hostname, String RNhostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (App.localRedNodesTable.checkOnline(RNhostname)) {
            outputWriter.println("USER ONLINE");
        } else {
            outputWriter.println("USER OFFLINE");
        }
    }

    static void Uping(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        if (App.BlueNodesTable.checkBlueNode(hostname)) {
            App.BlueNodesTable.getBlueNodeInstanceByHn(hostname).setUping(false);
        } else {
            outputWriter.println("UPING FAILED");            
            return;
        }
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (App.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getUPing()) {
            outputWriter.println("UPING OK");
        } else {
            outputWriter.println("UPING FAILED");
        }
    }

    static void Dping(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        outputWriter.println("DPING SENDING");
        byte[] payload = ("00003 " + App.Hostname + " [DPING PACKET]").getBytes();
        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
        App.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getQueueMan().offer(data);
    }

    static void GetRNs(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        int size = App.localRedNodesTable.getSize();
        outputWriter.println("SENDING_LOCAL_RED_NODES " + size);
        for (int i = 0; i < size; i++) {
            String vaddress = App.localRedNodesTable.getRedNodeInstance(i).getVaddress();
            hostname = App.localRedNodesTable.getRedNodeInstance(i).getHostname();
            outputWriter.println(vaddress + " " + hostname);
        }
        outputWriter.println();
    }

    static void ExchangeRNs(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        try {
            String clientSentence = null;
            String[] args;
            int size = App.localRedNodesTable.getSize();
            outputWriter.println("SENDING_LOCAL_RED_NODES " + size);
            for (int i = 0; i < size; i++) {
                String vaddress = App.localRedNodesTable.getRedNodeInstance(i).getVaddress();
                hostname = App.localRedNodesTable.getRedNodeInstance(i).getHostname();
                outputWriter.println(vaddress + " " + hostname);
            }
            outputWriter.println();

            clientSentence = inFromClient.readLine();
            App.ConsolePrint(pre + clientSentence);
            args = clientSentence.split("\\s+");

            int count = Integer.parseInt(args[1]);
            for (int i = 0; i < count; i++) {
                clientSentence = inFromClient.readLine();
                App.ConsolePrint(pre + clientSentence);
                args = clientSentence.split("\\s+");

                if (App.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                    App.remoteRedNodesTable.lease(args[0], args[1], hostname);
                } else {
                    App.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                }
            }
            clientSentence = inFromClient.readLine();
        } catch (IOException ex) {
            Logger.getLogger(BlueNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void FeedReturnRoute(String hostname, String address, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {        
        RemoteHandle.addRemoteRedNode(address, hostname);
        if (App.remoteRedNodesTable.checkAssociated(address)) {
            outputWriter.println("OK");
        } else {
            outputWriter.println("FAILED");
        }
    }
}
