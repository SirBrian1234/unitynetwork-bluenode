package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;
import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.service.CommonServiceFunctions;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.AppLogger;


/**
 *
 * @author Konstantinos Kagiampakis
 */
final class BlueNodeFunctions {

    private static String pre = "^Blue Node Functions";
    
    static void associate(Lock lock, BlueNodeTable blueNodeTable, String localBluenodeName, String name, PublicKey bnPub, Socket connectionSocket, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) throws IllegalAccessException, InterruptedException, GeneralSecurityException, IOException {
    	AppLogger.getInstance().consolePrint(pre + "STARTING A BLUE AUTH AT " + Thread.currentThread().getName());
    	var bno = blueNodeTable.getOptionalEntry(lock, name);
    	if (localBluenodeName.equals(name)) {
			SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
			throw new IllegalAccessException("Bluenodes have the same name!");
		} else if (bno.isPresent()) {
    		//blueNodeTable lookup
			SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
			throw new IllegalAccessException("Bluenode allready exists!");
		} else {
			//tracker lookup
			TrackerClient tr = new TrackerClient();
			String[] args = tr.getPhysicalBn(name);
			int authPort = Integer.parseInt(args[1]);
			var address = PhysicalAddress.valueOf(connectionSocket.getInetAddress().getAddress());

			if (args[0].equals("OFFLINE")) {
				SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
				throw new IllegalAccessException("Bluenode is Offline!");
			} else if (!args[0].equals(address.asString())) {
				SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
				throw new IllegalAccessException("Bluenode is found in a different physical address!");
			}

			AppLogger.getInstance().consolePrint(pre + "BN "+name+" IS VALID AT ADDR "+address.asString()+": "+authPort);
			//create the obj first in order to open its threads
			BlueNode bn = null;
			try {
				bn = new BlueNode(name, bnPub, address, authPort);
				SocketUtilities.sendAESEncryptedStringData("ASSOSIATING "+bn.getServerSendPort()+" "+bn.getServerReceivePort(), socketWriter, sessionKey);
				AppLogger.getInstance().consolePrint(pre + "remote auth port "+bn.getRemoteAuthPort()+" upport "
						+bn.getServerSendPort()+" downport "+bn.getServerReceivePort());
				blueNodeTable.leaseBlueNode(lock, bn);
				AppLogger.getInstance().consolePrint(pre + "LEASED REMOTE BN "+name);
			} catch (Exception e) {
				bn.killTasks();
				SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
				throw e;
			}
		}
    }

    /**
     * A Bn has requested to tell him if we can receive his packets
     */
    static void uPing(BlueNode bn, DataOutputStream outputWriter, SecretKey sessionKey) throws GeneralSecurityException, IOException, InterruptedException {
    	bn.setUping(false);
    	SocketUtilities.sendAESEncryptedStringData("SET", outputWriter, sessionKey);
    	sleep(2000);
		if (bn.getUPing()) {
			SocketUtilities.sendAESEncryptedStringData("OK", outputWriter, sessionKey);
		} else {
			SocketUtilities.sendAESEncryptedStringData("FAILED", outputWriter, sessionKey);
		}
    }

    /**
     * A Bn has requested to get some packets. That's all!
     */
    static void dPing(BlueNode bn) throws InterruptedException {
        byte[] data = UnityPacket.buildDpingPacket();
        for (int i=0; i<3; i++) {
			bn.getSendQueue().offer(data);
			sleep(200);
		}
    }
    
    static void releaseBn(Lock lock, BlueNodeTable blueNodeTable, String BlueNodeName) throws InterruptedException, IllegalAccessException {
		blueNodeTable.releaseBlueNode(lock, BlueNodeName);
	}
    
    static void giveLRNs(LocalRedNodeTable localRedNodeTable, DataOutputStream socketWriter, SecretKey sessionKey) throws GeneralSecurityException, IOException {
    	CommonServiceFunctions.sendLocalRedNodes(localRedNodeTable, socketWriter, sessionKey);
    }
    
    static void getLRNs(BlueNode bn, DataInputStream socketReader, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IllegalAccessException, IOException {
		CommonServiceFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);
	}

    static void exchangeRNs(LocalRedNodeTable localRedNodeTable, BlueNode bn, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) throws GeneralSecurityException, IOException, InterruptedException, IllegalAccessException {
    	CommonServiceFunctions.sendLocalRedNodes(localRedNodeTable, socketWriter, sessionKey);
    	CommonServiceFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);
    }
    
    static void getLocalRnHostnameByVaddress(LocalRedNodeTable localRedNodeTable, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) throws GeneralSecurityException, IOException {
		if (localRedNodeTable.checkOnlineByVaddress(vaddress)) {
			SocketUtilities.sendAESEncryptedStringData(localRedNodeTable.getRedNodeInstanceByAddr(vaddress).getHostname(), socketWriter, sessionKey);
		} else {
			SocketUtilities.sendAESEncryptedStringData("OFFLINE", socketWriter, sessionKey);
		}
	}

    static void getLocalRnVaddressByHostname(LocalRedNodeTable localRedNodeTable, String hostname, DataOutputStream socketWriter, SecretKey sessionKey) throws GeneralSecurityException, IOException {
		if (localRedNodeTable.checkOnlineByHostname(hostname)) {
			SocketUtilities.sendAESEncryptedStringData(localRedNodeTable.getRedNodeInstanceByHn(hostname).getAddress().asString(), socketWriter, sessionKey);
		} else {
			SocketUtilities.sendAESEncryptedStringData("OFFLINE", socketWriter, sessionKey);
		}
	}

    static void getFeedReturnRoute(BlueNode bn, String hostname, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) throws UnknownHostException, IllegalAccessException, InterruptedException {
    	Lock lock = null;
    	try {
    		lock = bn.getTable().aquireLock();
			var feed = RemoteRedNode.newInstance(hostname, VirtualAddress.valueOf(vaddress), bn);
			bn.getTable().lease(lock, feed);
		} finally {
    		lock.unlock();
		}
    }

	public static void getRRNToBeReleasedByHn(BlueNode bn, String hostname, DataOutputStream socketWriter, SecretKey sessionKey) throws IllegalAccessException, InterruptedException {
		Lock lock = null;
    	try {
    		lock = bn.getTable().aquireLock();
			bn.getTable().release(lock, hostname);
		} finally {
    		lock.unlock();
		}
	}
	
	public static void getRRNToBeReleasedByVaddr(BlueNode bn, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) throws InterruptedException, UnknownHostException, IllegalAccessException {
		Lock lock = null;
    	try {
    		lock = bn.getTable().aquireLock();
			bn.getTable().release(lock, VirtualAddress.valueOf(vaddress));
		} finally {
    		lock.unlock();
		}
	}

	public static void check(Lock lock, BlueNodeTable blueNodeTable, String blueNodeName, DataOutputStream socketWriter, SecretKey sessionKey) throws InterruptedException, GeneralSecurityException, IOException {
		//if associated reset idleTime and update timestamp as well
		var o = blueNodeTable.getOptionalEntry(lock, blueNodeName);
		if (o.isPresent()) {
			o.get().resetIdleTime();
			o.get().updateTimestamp();
			SocketUtilities.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
		} else {
			SocketUtilities.sendAESEncryptedStringData("FAILED", socketWriter, sessionKey);
		}
	}
}	
