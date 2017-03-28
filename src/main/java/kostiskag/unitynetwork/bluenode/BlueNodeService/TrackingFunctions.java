package kostiskag.unitynetwork.bluenode.BlueNodeService;

import java.io.PrintWriter;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Functions.TCPSocketFunctions;

/**
 *
 * @author kostis
 *  
 */
class TrackingFunctions {        

    public static void check(PrintWriter outputWriter) {
        TCPSocketFunctions.sendFinalData("OK", outputWriter);
    }

    public static void getrns(PrintWriter outputWriter) {
        int size = App.localRedNodesTable.getSize();
        TCPSocketFunctions.sendFinalData("SENDING_LOCAL_RED_NODES " + size, outputWriter);
        for (int i = 0; i < size; i++) {
            String vaddress = App.localRedNodesTable.getRedNodeInstance(i).getVaddress();
            String hostname = App.localRedNodesTable.getRedNodeInstance(i).getHostname();
            TCPSocketFunctions.sendFinalData(hostname+" "+vaddress, outputWriter);
        }
        TCPSocketFunctions.sendFinalData("", outputWriter);  //line feed      
    }
}