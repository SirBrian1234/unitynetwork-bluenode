package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;
import kostiskag.unitynetwork.bluenode.functions.IpAddrFunctions;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class RedNodeFunctions {
	
	private static String pre = "^RedNodeFunctions ";

    static void lease(Socket connectionSocket, BufferedReader socketReader, PrintWriter socketWriter, String hostname, String Username, String Password) {
    	App.bn.ConsolePrint(pre + "LEASING "+hostname);
    	
    	//first check if already exists
    	if (App.bn.localRedNodesTable.checkOnlineByHostname(hostname)){
    		socketWriter.println("FAILED");
    		return;
    	}
    	
    	//get a virtual IP address
    	String Vaddress = null;
    	if (App.bn.network && App.bn.joined) {            
    		//collect vaddress from tracker
    		TrackerClient tr = new TrackerClient();
    		Vaddress = tr.leaseRn(hostname, Username, Password);
            
            //leasing - reverse error capture     
    		if (Vaddress.startsWith("10.")) {
    			
    		} else if (Vaddress.equals("WRONG_COMMAND")) {
                App.bn.ConsolePrint(pre + "WRONG_COMMAND");
                socketWriter.println("FAILED BLUENODE");
                return;
            } else if (Vaddress.equals("NOT_ONLINE")) {
                App.bn.ConsolePrint(pre + "NOT_ONLINE");
                socketWriter.println("FAILED BLUENODE");
                return;
            } else if (Vaddress.equals("NOT_REGISTERED")) {
                App.bn.ConsolePrint(pre + "NOT_REGISTERED");
                socketWriter.println("FAILED BLUENODE");
                return;
            } else if (Vaddress.equals("SYSTEM_ERROR")) {
                App.bn.ConsolePrint(pre + "SYSTEM_ERROR");
                socketWriter.println("FAILED BLUENODE");
                return;
            } else if (Vaddress.equals("AUTH_FAILED")) {
                App.bn.ConsolePrint(pre + "FAILED USER");
                socketWriter.println("FAILED USER");
                return;
            } else if (Vaddress.equals("USER_HOSTNAME_MISSMATCH")) {
                App.bn.ConsolePrint(pre + "FAILED USER");
                socketWriter.println("FAILED USER");
                return;
            } else if (Vaddress.equals("NOT_FOUND")) {
                App.bn.ConsolePrint(pre + "HOSTNAME FAILED 1");
                socketWriter.println("FAILED USER");
                return;
            } else if (Vaddress.equals("LEASE_FAILED")) {
                App.bn.ConsolePrint(pre + "HOSTNAME FAILED 1");
                socketWriter.println("FAILED USER");
                return;
            } else if (Vaddress.equals("ALLREADY_LEASED")) {
                App.bn.ConsolePrint(pre + "FAILED HOSTNAME");
                socketWriter.println("FAILED HOSTNAME");
                return;
            } else {
            	socketWriter.println("FAILED");
            	return;
            }
                            
        } else if (App.bn.useList) {
        	//collect vaddres from list
        	Vaddress = App.bn.accounts.getVaddrIfExists(hostname, Username, Password);    
        	if (Vaddress == null) {
        		socketWriter.println("FAILED USER 0");
        		return;
        	}
        } else if (!App.bn.useList && !App.bn.network) {
        	//no network, no list - each red node collects a ticket
            int addr_num = App.bn.bucket.poll();
            Vaddress = IpAddrFunctions.numberTo10ipAddr(addr_num);
            if (Vaddress == null) {
        		socketWriter.println("FAILED USER 0");
        		return;
        	}
        } else {
        	return;
        }
               
        App.bn.ConsolePrint(pre + "COLLECTED VADDRESS "+Vaddress);
        
        //building the local rn object
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
        
        socketWriter.println("REG_OK "+RNclient.getVaddress()+" "+RNclient.getReceive().getServerPort()+" "+RNclient.getSend().getServerPort());
        App.bn.ConsolePrint(pre+"RED NODE OK " +  RNclient.getHostname() + "/" + RNclient.getVaddress() +" ~ " + RNclient.getPhAddress() + ":" + RNclient.getSend().getServerPort() + ":" + RNclient.getReceive().getServerPort());
        
        //initTerm will use the session socket and will hold this thread
        RNclient.initTerm();
        //holds the thread as its statefull
        //when RNclient.initTerm() returns the release process follows
        
        //release from the network
        if (App.bn.network && App.bn.joined) {
        	TrackerClient tr = new TrackerClient();
            tr.releaseRnByHostname(RNclient.getHostname());
            App.bn.blueNodesTable.releaseLocalRedNodeByHostnameFromAll(hostname);
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
