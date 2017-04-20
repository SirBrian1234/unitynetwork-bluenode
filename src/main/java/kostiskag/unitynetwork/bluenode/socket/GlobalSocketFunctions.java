package kostiskag.unitynetwork.bluenode.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

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
	
	public static void getRemoteRedNodes(BlueNodeInstance bn, BufferedReader socketReader, PrintWriter socketWriter) {
		String[] args = TCPSocketFunctions.readData(socketReader);
        int count = Integer.parseInt(args[1]);
        for (int i = 0; i < count; i++) {        	
			args = TCPSocketFunctions.readData(socketReader);
            try {
				App.bn.blueNodesTable.leaseRRn(bn, args[0], args[1]);
			} catch (Exception e) {
				
			}				
        }
        TCPSocketFunctions.readData(socketReader);
	}
}
