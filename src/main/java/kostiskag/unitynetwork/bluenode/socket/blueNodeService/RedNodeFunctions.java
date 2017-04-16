package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Thread.sleep;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.RedNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackingRedNodeFunctions;

/**
 *
 * @author kostis
 */
public class RedNodeFunctions {

    static void Lease(Socket connectionSocket, BufferedReader socketReader, PrintWriter socketWriter, String hostname, String Username, String Password) {
        
    	RedNodeInstance RNclient = new RedNodeInstance(connectionSocket, hostname, Username, Password);
        if (RNclient.getStatus() > 0) {                        
            	
        		try {
					App.bn.localRedNodesTable.lease(RNclient);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
        		
                try {
                    sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RedNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                socketWriter.println("REG OK " + RNclient.getDown().getDownport() + " " + RNclient.getUp().getUpport() + " " + RNclient.getVaddress());
                App.bn.ConsolePrint("RED NODE OK " + RNclient.getVaddress() + "/" + RNclient.getHostname() + "/" + RNclient.getUsername() + " ~ " + RNclient.getPhAddress() + ":" + RNclient.getUp().getUpport() + ":" + RNclient.getDown().getDownport());
                
                //initTerm will use the session socket and will hold this thread
                RNclient.initTerm();
                //holds the thread as its statefull
                
                //after this point the thread is released and the release process follows
                System.out.println("Tasks killed!!!");
                
                //release from network
                if (App.bn.network) {
                    TrackingRedNodeFunctions.release(RNclient.getHostname());
                }
                
                System.out.println("Released from netw");
                //release from local red node table
                try {
					App.bn.localRedNodesTable.releaseByHostname(hostname);
				} catch (Exception e) {
					e.printStackTrace();
				} 
                
                System.out.println("Reached the end!!!");
        }
    }
}
