package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;

/**
 *
 * @author kostis
 */
public class BlueNodeFunctions {

    private static String pre = "^Blue Node Functions";
    static void associate(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        BlueNodeInstance BNclient;
		try {
			BNclient = new BlueNodeInstance(hostname, false, connectionSocket);
			if (BNclient.getStatus() > 0) {
	            App.bn.blueNodesTable.leaseBn(BNclient);            
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}        
    }

    static void fullAssociate(String hostname, Socket connectionSocket, BufferedReader inFromClient, PrintWriter outputWriter) {
        BlueNodeInstance BNclient;
		try {
			BNclient = new BlueNodeInstance(hostname, true, connectionSocket);
			if (BNclient.getStatus() > 0) {
	            App.bn.blueNodesTable.leaseBn(BNclient);            
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}        
    }
    
    static void Uping(BlueNodeInstance bn, PrintWriter outputWriter) {
    	bn.setUping(false);        
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }        
        if (bn.getUPing()) {
            outputWriter.println("UPING OK");
        } else {
            outputWriter.println("UPING FAILED");
        }
    }

    static void Dping(BlueNodeInstance bn, PrintWriter outputWriter) {
        outputWriter.println("DPING SENDING");
        byte[] payload = ("00003 " + App.bn.name + " [DPING PACKET]").getBytes();
        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
        try {
			bn.getQueueMan().offer(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    static void releaseBn(String BlueNodeName, PrintWriter outputWriter) {
        try {
			App.bn.blueNodesTable.releaseBn(BlueNodeName);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    static void giveLRNs(PrintWriter outputWriter) {
    	GlobalSocketFunctions.sendLocalRedNodes(outputWriter);
    }

    static void exchangeRNs(BlueNodeInstance bn, BufferedReader inFromClient, PrintWriter outputWriter) {
        try {
            String clientSentence = null;
            String[] args;
            GlobalSocketFunctions.sendLocalRedNodes(outputWriter);

            clientSentence = inFromClient.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            args = clientSentence.split("\\s+");

            int count = Integer.parseInt(args[1]);
            for (int i = 0; i < count; i++) {
                clientSentence = inFromClient.readLine();
                App.bn.ConsolePrint(pre + clientSentence);
                args = clientSentence.split("\\s+");

                if (!App.bn.blueNodesTable.checkRemoteRedNodeByHostname(args[0])) {
                    bn.table.lease(args[0], args[1]);
                }
            }
            clientSentence = inFromClient.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static void getLocalRnVaddressByHostname(String hostname, PrintWriter outputWriter) {
        if (App.bn.localRedNodesTable.checkOnlineByHostname(hostname)) {
            outputWriter.println("ONLINE "+App.bn.localRedNodesTable.getRedNodeInstanceByHn(hostname).getVaddress());
        } else {
            outputWriter.println("OFFLINE");
        }
    }
    
    static void getLocalRnHostnameByVaddress(String vaddress, PrintWriter outputWriter) {
        if (App.bn.localRedNodesTable.checkOnlineByVaddress(vaddress)) {
        	outputWriter.println("ONLINE "+App.bn.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).getHostname());
        } else {
            outputWriter.println("OFFLINE");
        }
    }

    static void getFeedReturnRoute(BlueNodeInstance bn, String hostname, String vaddress, PrintWriter outputWriter) {        
        bn.table.lease(hostname, vaddress);
        outputWriter.println("OK");        
    }
}	
