/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.BlueNodeService;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.Functions.TCPSocketFunctions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 * 
 * 
 * 
 * 
 * we are on BN!!!!
 */
class TrackingFunctions {        

    public static void check(PrintWriter outputWriter) {
        TCPSocketFunctions.sendFinalData("OK", outputWriter);
    }

    public static void getrns(PrintWriter outputWriter) {
        int size = lvl3BlueNode.localRedNodesTable.getSize();
        TCPSocketFunctions.sendFinalData("SENDING_LOCAL_RED_NODES " + size, outputWriter);
        for (int i = 0; i < size; i++) {
            String vaddress = lvl3BlueNode.localRedNodesTable.getRedNodeInstance(i).getVaddress();
            String hostname = lvl3BlueNode.localRedNodesTable.getRedNodeInstance(i).getHostname();
            TCPSocketFunctions.sendFinalData(hostname+" "+vaddress, outputWriter);
        }
        TCPSocketFunctions.sendFinalData("", outputWriter);  //line feed      
    }
}