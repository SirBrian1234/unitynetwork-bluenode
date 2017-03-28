/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.BlueNodeService;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.RunData.Instances.RedNodeInstance;
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
public class RedNodeFunctions {

    static void Lease(Socket connectionSocket, String hostname, String Username, String Password) {
        PrintWriter outputWriter = null;
        RedNodeInstance RNclient = new RedNodeInstance(connectionSocket, hostname, Username, Password);
        if (RNclient.getStatus() > 0) {                        
            try {
                lvl3BlueNode.localRedNodesTable.lease(RNclient);
                RNclient.startServices();                
                
                try {
                    sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RedNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                outputWriter = new PrintWriter(connectionSocket.getOutputStream(), true);
                outputWriter.println("REG OK " + RNclient.getDown().getDownport() + " " + RNclient.getUp().getUpport() + " " + RNclient.getVaddress());
                lvl3BlueNode.ConsolePrint("RED NODE OK " + RNclient.getVaddress() + "/" + RNclient.getHostname() + "/" + RNclient.getUsername() + " ~ " + RNclient.getPhAddress() + ":" + RNclient.getUp().getUpport() + ":" + RNclient.getDown().getDownport());
                lvl3BlueNode.localRedNodesTable.updateTable();
                RNclient.initTerm();
                
            } catch (IOException ex) {
                Logger.getLogger(RedNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                outputWriter.close();
            }
        }
    }
}
