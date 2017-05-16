package kostiskag.unitynetwork.bluenode.socket;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
			SocketFunctions.sendAESEncryptedStringData("SENDING_LOCAL_RED_NODES " + size, socketWriter, sessionKey);
			Iterator<String> it = fetched.listIterator();
	        while(it.hasNext()){
	        	String toSend = it.next();
	        	SocketFunctions.sendAESEncryptedStringData(toSend, socketWriter, sessionKey);
	        }     
	        SocketFunctions.sendAESEncryptedStringData("", socketWriter, sessionKey);
        } catch (Exception e) {
			e.printStackTrace();
		}            
    }
	
	public static void getRemoteRedNodes(BlueNodeInstance bn, DataInputStream socketReader, SecretKey sessionKey) {
		try {
			String[] args = SocketFunctions.receiveAESEncryptedStringData(socketReader, sessionKey);
			int count = Integer.parseInt(args[1]);
	        for (int i = 0; i < count; i++) {        	
				args = SocketFunctions.receiveAESEncryptedStringData(socketReader, sessionKey);
	            try {
					App.bn.blueNodesTable.leaseRRn(bn, args[0], args[1]);
				} catch (Exception e) {
					
				}				
	        }
	        SocketFunctions.receiveAESEncryptedStringData(socketReader, sessionKey);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }
}
