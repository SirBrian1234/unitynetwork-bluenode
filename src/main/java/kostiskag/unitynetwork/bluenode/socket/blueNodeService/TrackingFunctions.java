package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

/**
 *
 * @author kostis
 *  
 */
class TrackingFunctions {        

    public static void check(PrintWriter outputWriter) {
        TCPSocketFunctions.sendFinalData("OK", outputWriter);
        App.bn.trackerRespond.set(0);
    }

    public static void getrns(PrintWriter outputWriter) {
    	LinkedList<String> fetched = App.bn.localRedNodesTable.buildAddrHostStringList();
        int size = fetched.size();
    	TCPSocketFunctions.sendFinalData("SENDING_LOCAL_RED_NODES " + size, outputWriter);
    	Iterator<String> it = fetched.listIterator();
        while(it.hasNext()){
        	String toSend = it.next();
        	TCPSocketFunctions.sendFinalData(toSend, outputWriter);
        }        
        TCPSocketFunctions.sendFinalData("", outputWriter);  
    }
    
    public static void killsig(PrintWriter outputWriter) {
        TCPSocketFunctions.sendFinalData("OK", outputWriter);
        App.bn.localRedNodesTable.exitAll();
        App.bn.die();
    }
}