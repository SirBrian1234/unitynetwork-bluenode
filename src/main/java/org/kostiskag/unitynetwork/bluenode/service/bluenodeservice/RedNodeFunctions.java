package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.service.NextIpPoll;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.LocalRedNode;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public class RedNodeFunctions {
	
	private static String pre = "^RedNodeFunctions ";

    static void lease(String hostname, String Username, String Password, Socket connectionSocket, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) {
		AppLogger.getInstance().consolePrint(pre + "LEASING "+hostname);
    	
    	//first check if already exists
    	if (Bluenode.getInstance().localRedNodesTable.checkOnlineByHostname(hostname)){
    		try {
				SocketUtilities.sendAESEncryptedStringData("FAILED", socketWriter, sessionKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		return;
    	}
    	
    	//get a virtual IP address
    	String Vaddress = null;
    	if (Bluenode.getInstance().isJoinedNetwork()) {
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
                            
        } else if (Bluenode.getInstance().isListMode()) {
        	//collect vaddres from list
        	Vaddress = Bluenode.getInstance().getAccounts().getVaddrIfExists(hostname, Username, Password).asString();
        	if (Vaddress == null) {
        		try {
					SocketUtilities.sendAESEncryptedStringData("FAILED USER 0", socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
        		return;
        	}
        } else if (Bluenode.getInstance().isPlainMode()) {
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
    	LocalRedNode RNclient = new LocalRedNode( hostname, Vaddress, phAddress, port, socketReader, socketWriter, sessionKey);
        
    	//leasing it to the local red node table
		try {
			Bluenode.getInstance().localRedNodesTable.lease(RNclient);
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
			SocketUtilities.sendAESEncryptedStringData("REG_OK "+RNclient.getVaddress()+" "+RNclient.getReceive().getServerPort()+" "+RNclient.getSend().getServerPort(), socketWriter, sessionKey);
            AppLogger.getInstance().consolePrint(pre+"RED NODE OK " +  RNclient.getHostname() + "/" + RNclient.getVaddress() +" ~ " + RNclient.getPhAddress() + ":" + RNclient.getSend().getServerPort() + ":" + RNclient.getReceive().getServerPort());
	        
	        //initTerm will use the session socket and will hold this thread
	        RNclient.initTerm();
	        //holds the thread as its statefull
	        //when RNclient.initTerm() returns the release process follows
        } catch (Exception e1) {
			e1.printStackTrace();
		}

        //release from the network
        if (Bluenode.getInstance().isJoinedNetwork()) {
        	TrackerClient tr = new TrackerClient();
            tr.releaseRnByHostname(RNclient.getHostname());
            Bluenode.getInstance().blueNodeTable.releaseLocalRedNodeByHostnameFromAll(hostname);
        }
        
        //release from local red node table
        try {
			Bluenode.getInstance().localRedNodesTable.releaseByHostname(hostname);
		} catch (Exception e) {
			e.printStackTrace();
		}
        AppLogger.getInstance().consolePrint(pre+"ENDED");
    }
}
