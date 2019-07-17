package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.LocalRedNodeInstance;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class RedNodeFunctions {
	
	private static String pre = "^RedNodeFunctions ";

    static void lease(String hostname, String Username, String Password, Socket connectionSocket, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) {
    	App.bn.ConsolePrint(pre + "LEASING "+hostname);
    	
    	//first check if already exists
    	if (App.bn.localRedNodesTable.checkOnlineByHostname(hostname)){
    		try {
				SocketUtilities.sendAESEncryptedStringData("FAILED", socketWriter, sessionKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		return;
    	}
    	
    	//get a virtual IP address
    	String Vaddress = null;
    	if (App.bn.network && App.bn.joined) {            
    		//collect vaddress from tracker
    		TrackerClient tr = new TrackerClient();
    		Vaddress = tr.leaseRn(hostname, Username, Password);
            
            //leasing - reverse error capture     
    		try {
	    		if (Vaddress.startsWith("10.")) {
	    			
	    		} else if (Vaddress.equals("WRONG_COMMAND")) {
	                App.bn.ConsolePrint(pre + "WRONG_COMMAND");
	                SocketUtilities.sendAESEncryptedStringData("FAILED BLUENODE", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("NOT_ONLINE")) {
	                App.bn.ConsolePrint(pre + "NOT_ONLINE");
	                SocketUtilities.sendAESEncryptedStringData("FAILED BLUENODE", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("NOT_REGISTERED")) {
	                App.bn.ConsolePrint(pre + "NOT_REGISTERED");
	                SocketUtilities.sendAESEncryptedStringData("FAILED BLUENODE", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("SYSTEM_ERROR")) {
	                App.bn.ConsolePrint(pre + "SYSTEM_ERROR");
	                SocketUtilities.sendAESEncryptedStringData("FAILED BLUENODE", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("AUTH_FAILED")) {
	                App.bn.ConsolePrint(pre + "FAILED USER");
	                SocketUtilities.sendAESEncryptedStringData("FAILED USER", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("USER_HOSTNAME_MISSMATCH")) {
	                App.bn.ConsolePrint(pre + "FAILED USER");
	                SocketUtilities.sendAESEncryptedStringData("FAILED USER", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("NOT_FOUND")) {
	                App.bn.ConsolePrint(pre + "HOSTNAME FAILED 1");
	                SocketUtilities.sendAESEncryptedStringData("FAILED USER", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("LEASE_FAILED")) {
	                App.bn.ConsolePrint(pre + "HOSTNAME FAILED 1");
	                SocketUtilities.sendAESEncryptedStringData("FAILED USER", socketWriter, sessionKey);
	                return;
	            } else if (Vaddress.equals("ALLREADY_LEASED")) {
	                App.bn.ConsolePrint(pre + "FAILED HOSTNAME");
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
                            
        } else if (App.bn.useList) {
        	//collect vaddres from list
        	Vaddress = App.bn.accounts.getVaddrIfExists(hostname, Username, Password).asString();
        	if (Vaddress == null) {
        		try {
					SocketUtilities.sendAESEncryptedStringData("FAILED USER 0", socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
        		return;
        	}
        } else if (!App.bn.useList && !App.bn.network) {
        	//no network, no list - each red node collects a ticket
            int addr_num = App.bn.bucket.poll();
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
               
        App.bn.ConsolePrint(pre + "COLLECTED VADDRESS "+Vaddress);
        
        //building the local rn object
        String phAddress = connectionSocket.getInetAddress().getHostAddress();
        int port = connectionSocket.getPort();
    	LocalRedNodeInstance RNclient = new LocalRedNodeInstance( hostname, Vaddress, phAddress, port, socketReader, socketWriter, sessionKey);
        
    	//leasing it to the local red node table
		try {
			App.bn.localRedNodesTable.lease(RNclient);
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
	        App.bn.ConsolePrint(pre+"RED NODE OK " +  RNclient.getHostname() + "/" + RNclient.getVaddress() +" ~ " + RNclient.getPhAddress() + ":" + RNclient.getSend().getServerPort() + ":" + RNclient.getReceive().getServerPort());
	        
	        //initTerm will use the session socket and will hold this thread
	        RNclient.initTerm();
	        //holds the thread as its statefull
	        //when RNclient.initTerm() returns the release process follows
        } catch (Exception e1) {
			e1.printStackTrace();
		}

        //release from the network
        if (App.bn.network && App.bn.joined) {
        	TrackerClient tr = new TrackerClient();
            tr.releaseRnByHostname(RNclient.getHostname());
            App.bn.blueNodeTable.releaseLocalRedNodeByHostnameFromAll(hostname);
        }
        
        //release from local red node table
        try {
			App.bn.localRedNodesTable.releaseByHostname(hostname);
		} catch (Exception e) {
			e.printStackTrace();
		}
        App.bn.ConsolePrint(pre+"ENDED");
    }
}
