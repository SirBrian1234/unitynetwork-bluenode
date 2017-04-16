package kostiskag.unitynetwork.bluenode.socket;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

import kostiskag.unitynetwork.bluenode.App;

public class GlobalSocketFunctions {

	public static void sendLocalRedNodes(PrintWriter socketWriter) {
		LinkedList<String> fetched = App.bn.localRedNodesTable.buildAddrHostStringList();
        int size = fetched.size();
        socketWriter.println("SENDING_LOCAL_RED_NODES " + size);            
        Iterator<String> it = fetched.listIterator();
        while(it.hasNext()){
        	String toSend = it.next();
        	socketWriter.println(toSend);
        }     
        socketWriter.println();
	}
}
