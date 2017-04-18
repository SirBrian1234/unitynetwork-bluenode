package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Thread.sleep;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;
import kostiskag.unitynetwork.bluenode.functions.IpAddrFunctions;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackingRedNodeFunctions;

/**
 *
 * @author kostis
 */
public class RedNodeFunctions {
	
	private static String pre = "^LEASE LOCAL REDNODE ";

    static void lease(Socket connectionSocket, BufferedReader socketReader, PrintWriter socketWriter, String hostname, String Username, String Password) {
    	App.bn.ConsolePrint(pre + "LEASING "+hostname);
    	
    	//first check if already exists
    	if (App.bn.localRedNodesTable.checkOnlineByHostname(hostname)){
    		socketWriter.println("REG FAILED");
    		return;
    	}
    	
    	//get a virtuall ip address
    	String Vaddress = null;
    	if (App.bn.network && App.bn.joined) {
            
    		//collect vaddress from tracker
    		Vaddress = TrackingRedNodeFunctions.lease(hostname, Username, Password);
            
            //leasing - reverse error capture     
            if (Vaddress.equals("WRONG_COMMAND")) {
                App.bn.ConsolePrint(pre + "WRONG_COMMAND");
                socketWriter.println("BLUENODE FAILED");
                return;
            } else if (Vaddress.equals("NOT_ONLINE")) {
                App.bn.ConsolePrint(pre + "NOT_ONLINE");
                socketWriter.println("BLUENODE FAILED");
                return;
            } else if (Vaddress.equals("NOT_REGISTERED")) {
                App.bn.ConsolePrint(pre + "NOT_REGISTERED");
                socketWriter.println("BLUENODE FAILED");
                return;
            } else if (Vaddress.equals("SYSTEM_ERROR")) {
                App.bn.ConsolePrint(pre + "SYSTEM_ERROR");
                socketWriter.println("BLUENODE FAILED");
                return;
            } else if (Vaddress.equals("AUTH_FAILED")) {
                App.bn.ConsolePrint(pre + "USER FAILED 1");
                socketWriter.println("USER FAILED 1");
                return;
            } else if (Vaddress.equals("USER_HOSTNAME_MISSMATCH")) {
                App.bn.ConsolePrint(pre + "HOSTNAME FAILED 3");
                socketWriter.println("HOSTNAME FAILED 3");
                return;
            } else if (Vaddress.equals("ALLREADY_LEASED")) {
                App.bn.ConsolePrint(pre + "HOSTNAME FAILED 2");
                socketWriter.println("HOSTNAME FAILED 2");
                return;
            } else if (Vaddress.equals("NOT_FOUND")) {
                App.bn.ConsolePrint(pre + "HOSTNAME FAILED 1");
                socketWriter.println("HOSTNAME FAILED 1");
                return;
            } else if (Vaddress.equals("LEASE_FAILED")) {
                App.bn.ConsolePrint(pre + "HOSTNAME FAILED 1");
                socketWriter.println("HOSTNAME FAILED 1");
                return;
            }
                            
        } else if (App.bn.useList) {
        	//collect vaddres from list
        	Vaddress = App.bn.accounts.getVaddrIfExists(hostname, Username, Password);                          	
        } else if (!App.bn.useList && !App.bn.network) {
        	//no network, no list - each red node collects a ticket
            int addr_num = App.bn.bucket.poll();
            Vaddress = IpAddrFunctions.numberTo10ipAddr(addr_num);
        } else {
        	return;
        }
               
        App.bn.ConsolePrint(pre + "COLLECTED VADDRESS "+Vaddress);
        
        //crating the local rn object
        String phAddress = connectionSocket.getInetAddress().getHostAddress();
        int port = connectionSocket.getPort();
    	LocalRedNodeInstance RNclient = new LocalRedNodeInstance(socketReader, socketWriter, hostname, Vaddress, phAddress, port);
        
    	//leasing it to the local red node table
		try {
			App.bn.localRedNodesTable.lease(RNclient);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//this time is for the pings
        try {
            sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        socketWriter.println("REG OK " + RNclient.getDown().getDestPort() + " " + RNclient.getUp().getSourcePort() + " " + RNclient.getVaddress());
        App.bn.ConsolePrint(pre+"RED NODE OK " +  RNclient.getHostname() + "/" + RNclient.getVaddress() +" ~ " + RNclient.getPhAddress() + ":" + RNclient.getUp().getSourcePort() + ":" + RNclient.getDown().getDestPort());
        
        //initTerm will use the session socket and will hold this thread
        RNclient.initTerm();
        //holds the thread as its statefull
        //when RNclient.initTerm() returns the release process follows
        
        //release from the network
        if (App.bn.network && App.bn.joined) {
            TrackingRedNodeFunctions.release(RNclient.getHostname());
            App.bn.blueNodesTable.releaseLocalRedNodeByHostnameFromAll(RNclient.getHostname());
        }
        
        //release from local red node table
        try {
			App.bn.localRedNodesTable.releaseByHostname(hostname);
		} catch (Exception e) {
			e.printStackTrace();
		}
        App.bn.ConsolePrint(pre+"ENDED");
    }
}
