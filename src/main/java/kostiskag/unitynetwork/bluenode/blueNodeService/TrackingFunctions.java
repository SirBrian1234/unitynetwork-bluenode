package kostiskag.unitynetwork.bluenode.blueNodeService;

import java.io.PrintWriter;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.functions.TCPSocketFunctions;

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
        int size = App.bn.localRedNodesTable.getSize();
        TCPSocketFunctions.sendFinalData("SENDING_LOCAL_RED_NODES " + size, outputWriter);
        for (int i = 0; i < size; i++) {
            String vaddress = App.bn.localRedNodesTable.getRedNodeInstance(i).getVaddress();
            String hostname = App.bn.localRedNodesTable.getRedNodeInstance(i).getHostname();
            TCPSocketFunctions.sendFinalData(hostname+" "+vaddress, outputWriter);
        }
        TCPSocketFunctions.sendFinalData("", outputWriter);  //line feed      
    }
    
    public static void killsig(PrintWriter outputWriter) {
        TCPSocketFunctions.sendFinalData("OK", outputWriter);
        App.bn.localRedNodesTable.releaseAll();
        App.bn.die();
    }
}