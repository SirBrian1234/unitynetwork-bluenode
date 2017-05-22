package kostiskag.unitynetwork.bluenode.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.crypto.SecretKey;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class GlobalSocketFunctions {

	public static void sendLocalRedNodes(DataOutputStream socketWriter, SecretKey sessionKey) {
		LinkedList<String> fetched = App.bn.localRedNodesTable.buildAddrHostStringList();
        int size = fetched.size();
        try {
        	StringBuilder str = new StringBuilder();
        	str.append("SENDING_LOCAL_RED_NODES "+size+"\n");
        	Iterator<String> it = fetched.listIterator();			
	        while(it.hasNext()) {
	        	str.append(it.next()+"\n");	        	
	        }   
	        SocketFunctions.sendAESEncryptedStringData(str.toString(), socketWriter, sessionKey);
	    } catch (Exception e) {
			e.printStackTrace();
		}            
    }
	
	public static void getRemoteRedNodes(BlueNodeInstance bn, DataInputStream socketReader, SecretKey sessionKey) {
		try {
			String received = SocketFunctions.receiveAESEncryptedString(socketReader, sessionKey);
			String[] lines = received.split("\n+"); //split into sentences
			String[] args = lines[0].split("\\s+"); //the first sentence contains the number
			int count = Integer.parseInt(args[1]);  //for the given number read the rest sentences
	        for (int i = 1; i < count+1; i++) {        	
				args = lines[i].split("\\s+");
	            try {
	            	App.bn.blueNodesTable.leaseRRn(bn, args[0], args[1]);
				} catch (Exception e) {
					e.printStackTrace();
				}				
	        }
	   } catch (Exception e1) {
		   e1.printStackTrace();
	   }
    }
}
