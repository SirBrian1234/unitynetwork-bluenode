package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.rundata.table.AccountTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.service.NextIpPoll;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.LocalRedNode;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.ModeOfOperation;
import org.kostiskag.unitynetwork.bluenode.AppLogger;


/**
 *
 * @author Konstantinos Kagiampakis
 */
final class RedNodeFunctions {
	
	private static String pre = "^RedNodeFunctions ";

    static void lease(ModeOfOperation mode,
					  AccountTable accounts,
					  LocalRedNodeTable redonodeTable,
					  BlueNodeTable bluenodeTable,
					  String hostname,
					  String Username,
					  String Password,
					  Socket connectionSocket,
					  DataInputStream socketReader,
					  DataOutputStream socketWriter,
					  SecretKey sessionKey) {

		AppLogger.getInstance().consolePrint(pre + "LEASING "+hostname);
    	
    	//first check if already exists
    	if (redonodeTable.checkOnlineByHostname(hostname)) {
    		try {
				SocketUtilities.sendAESEncryptedStringData("FAILED", socketWriter, sessionKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		return;
    	}
    	
    	//get a virtual IP address
    	String Vaddress = null;
    	if (mode.equals(ModeOfOperation.NETWORK)) {
    		//collect vaddress from tracker
    		TrackerClient tr = new TrackerClient();
    		Vaddress = tr.leaseRn(hostname, Username, Password);
            
            //leasing - reverse error capture     
    		try {
	    		if (Vaddress.startsWith("10.")) {
	    			
	    		} else if (Vaddress.equals("WRONG_COMMAND")) {
                    AppLogger.getInstance().consolePrint(pre + "WRONG_COMMAND");
	                SocketUtilities.sendAESEncryptedStringData("FAILED BLUENODE", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("NOT_ONLINE")) {
                    AppLogger.getInstance().consolePrint(pre + "NOT_ONLINE");
	                SocketUtilities.sendAESEncryptedStringData("FAILED BLUENODE", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("NOT_REGISTERED")) {
                    AppLogger.getInstance().consolePrint(pre + "NOT_REGISTERED");
	                SocketUtilities.sendAESEncryptedStringData("FAILED BLUENODE", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("SYSTEM_ERROR")) {
                    AppLogger.getInstance().consolePrint(pre + "SYSTEM_ERROR");
	                SocketUtilities.sendAESEncryptedStringData("FAILED BLUENODE", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("AUTH_FAILED")) {
                    AppLogger.getInstance().consolePrint(pre + "FAILED USER");
	                SocketUtilities.sendAESEncryptedStringData("FAILED USER", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("USER_HOSTNAME_MISSMATCH")) {
                    AppLogger.getInstance().consolePrint(pre + "FAILED USER");
	                SocketUtilities.sendAESEncryptedStringData("FAILED USER", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("NOT_FOUND")) {
                    AppLogger.getInstance().consolePrint(pre + "HOSTNAME FAILED 1");
	                SocketUtilities.sendAESEncryptedStringData("FAILED USER", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("LEASE_FAILED")) {
                    AppLogger.getInstance().consolePrint(pre + "HOSTNAME FAILED 1");
	                SocketUtilities.sendAESEncryptedStringData("FAILED USER", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("ALLREADY_LEASED")) {
                    AppLogger.getInstance().consolePrint(pre + "FAILED HOSTNAME");
	                SocketUtilities.sendAESEncryptedStringData("FAILED HOSTNAME", socketWriter, sessionKey);
	                return;
	            } else {
	            	SocketUtilities.sendAESEncryptedStringData("FAILED", socketWriter, sessionKey);
	            	return;
	            }
    		} catch (Exception e) {
    			e.printStackTrace();
    			return;
    		}
                            
        } else if (mode.equals(ModeOfOperation.LIST)) {
        	//collect vaddres from list
        	Vaddress = accounts.getVaddrIfExists(hostname, Username, Password).asString();
        	if (Vaddress == null) {
        		try {
					SocketUtilities.sendAESEncryptedStringData("FAILED USER 0", socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
        		return;
        	}
        } else if (mode.equals(ModeOfOperation.PLAIN)) {
        	//no network, no list - each red node collects a ticket
            int addr_num = NextIpPoll.getInstance().poll();
			try {
				Vaddress = VirtualAddress.numberTo10ipAddr(addr_num);
			} catch (UnknownHostException e) {
				Vaddress = null;
			}
			if (Vaddress == null) {
            	try {
					SocketUtilities.sendAESEncryptedStringData("FAILED USER 0", socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
        		return;
        	}
        } else {
        	return;
        }

        AppLogger.getInstance().consolePrint(pre + "COLLECTED VADDRESS "+Vaddress);
        
        //building the local rn object
        String phAddress = connectionSocket.getInetAddress().getHostAddress();
        int port = connectionSocket.getPort();
		LocalRedNode redNodeClient = null;
        try {
			redNodeClient = new LocalRedNode( hostname, Vaddress, phAddress, port, socketReader, socketWriter, sessionKey);
		} catch (IllegalAccessException | UnknownHostException e) {
        	AppLogger.getInstance().consolePrint(pre+"could not create red node object.");
        	return;
		}

    	//leasing it to the local red node table
		try {
			redonodeTable.lease(redNodeClient);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		//this time is for the pings
        try {
            sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        try {
			SocketUtilities.sendAESEncryptedStringData("REG_OK "+redNodeClient.getAddress().asString()+" "+redNodeClient.getReceive().getServerPort()+" "+redNodeClient.getSend().getServerPort(), socketWriter, sessionKey);
            AppLogger.getInstance().consolePrint(pre+"RED NODE OK " +  redNodeClient.getHostname() + "/" + redNodeClient.getAddress().asString() +" ~ " + redNodeClient.getPhAddress() + ":" + redNodeClient.getSend().getServerPort() + ":" + redNodeClient.getReceive().getServerPort());
	        
	        //initTerm will use the session socket and will hold this thread
	        redNodeClient.initTerm();
	        //holds the thread as its stateful
	        //when RNclient.initTerm() returns the release process follows
        } catch (Exception e1) {
			e1.printStackTrace();
		}

        //release from the network
        if (mode == ModeOfOperation.NETWORK) {
        	TrackerClient tr = new TrackerClient();
            tr.releaseRnByHostname(redNodeClient.getHostname());

            Lock lock = null;
            try {
				lock = bluenodeTable.aquireLock();
				bluenodeTable.releaseLocalRedNodeProjectionFromAll(lock, hostname);
			} catch (IllegalAccessException | InterruptedException e) {
				AppLogger.getInstance().consolePrint(e.getMessage());
			} finally {
				lock.unlock();
			}
        }
        
        //release from local red node table
        try {
			redonodeTable.releaseByHostname(hostname);
		} catch (Exception e) {
			e.printStackTrace();
		}
        AppLogger.getInstance().consolePrint(pre+"ENDED");
    }
}
