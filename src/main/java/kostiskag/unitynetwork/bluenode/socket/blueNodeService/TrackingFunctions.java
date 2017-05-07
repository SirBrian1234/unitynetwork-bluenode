package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

/**
 *
 * @author Konstantinos Kagiampakis
 */
class TrackingFunctions {        

    public static void check(PrintWriter outputWriter) {
        TCPSocketFunctions.sendFinalData("OK", outputWriter);
        App.bn.trackerRespond.set(0);
    }

    public static void getrns(PrintWriter outputWriter) {
    	GlobalSocketFunctions.sendLocalRedNodes(outputWriter);
    }
    
    public static void killsig(PrintWriter outputWriter) {
        TCPSocketFunctions.sendFinalData("OK", outputWriter);
        App.bn.localRedNodesTable.exitAll();
        App.bn.die();
    }
}