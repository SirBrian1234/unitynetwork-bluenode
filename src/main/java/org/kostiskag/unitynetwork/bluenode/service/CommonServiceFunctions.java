package org.kostiskag.unitynetwork.bluenode.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;


/**
 * 
 * @author Konstantinos Kagiampakis
 */
public final class CommonServiceFunctions {

	public static void sendLocalRedNodes(LocalRedNodeTable redNodeTable, DataOutputStream socketWriter, SecretKey sessionKey) throws GeneralSecurityException, IOException {
		LinkedList<String> fetched = redNodeTable.buildAddrHostStringList();
        int size = fetched.size();
		StringBuilder str = new StringBuilder();
		str.append("SENDING_LOCAL_RED_NODES "+size+"\n");
		Iterator<String> it = fetched.listIterator();
		while(it.hasNext()) {
			str.append(it.next()+"\n");
		}
		SocketUtilities.sendAESEncryptedStringData(str.toString(), socketWriter, sessionKey);
	}
	
	public static void getRemoteRedNodes(BlueNode bn, DataInputStream socketReader, SecretKey sessionKey) throws IOException, GeneralSecurityException, UnknownHostException, IllegalAccessException, InterruptedException {
		String received = SocketUtilities.receiveAESEncryptedString(socketReader, sessionKey);
		String[] lines = received.split("\n+"); //split into sentences
		String[] args = lines[0].split("\\s+"); //the first sentence contains the number
		int count = Integer.parseInt(args[1]);  //for the given number read the rest sentences

		Collection<RemoteRedNode> given = new ArrayList<>();
		for (int i = 1; i < count+1; i++) {
			args = lines[i].split("\\s+");
			given.add(RemoteRedNode.newInstance(args[0], VirtualAddress.valueOf(args[1]), bn));
		}

		Lock lock= null;
		try {
			lock = bn.getTable().aquireLock();
			bn.getTable().renewAll(lock, given, true);
		} finally {
			lock.unlock();
		}
    }
	
	/**
	 * The difference of this method from getRemoteRedNodes is that the former requests and
	 * then internally leases the returned results although this method requests but returns the 
	 * results as a built object.
	 * 
	 * @return a linked list with all the retrieved RemoteRedNodeInstance
	 */
	public static Collection<RemoteRedNode> getRemoteRedNodeCollection(BlueNode bn, DataInputStream socketReader, SecretKey sessionKey) throws IOException, GeneralSecurityException, IllegalAccessException, InterruptedException {
		Collection<RemoteRedNode> fetched = new ArrayList<>();
		String received = SocketUtilities.receiveAESEncryptedString(socketReader, sessionKey);
		String[] lines = received.split("\n+"); //split into sentences
		String[] args = lines[0].split("\\s+"); //the first sentence contains the number
		int count = Integer.parseInt(args[1]);  //for the given number read the rest sentences
		for (int i = 1; i < count+1; i++) {
			args = lines[i].split("\\s+");
			RemoteRedNode r =  RemoteRedNode.newInstance(args[0], VirtualAddress.valueOf(args[1]), bn);
			fetched.add(r);
		}
	    return fetched;
    }
}
