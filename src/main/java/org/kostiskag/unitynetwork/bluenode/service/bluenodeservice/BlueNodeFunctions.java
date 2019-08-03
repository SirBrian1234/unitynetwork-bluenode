package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.service.GlobalSocketFunctions;
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
    
    static void associate(String localBluenodeName, BlueNodeTable blueNodeTable, String name, PublicKey bnPub, Socket connectionSocket, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) {
    	try {
			AppLogger.getInstance().consolePrint(pre + "STARTING A BLUE AUTH AT " + Thread.currentThread().getName());
	    	InetAddress phAddress;
	    	String phAddressStr;
	    	int authPort = 0;
	    	String[] args;
	        
	        if (localBluenodeName.equals(name)) {
	        	SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
	        	return;
	        } else if (blueNodeTable.checkBlueNode(name)) {
	        	SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
	        	return;
	        } else {
	        	//tracker lookup
	        	TrackerClient tr = new TrackerClient();
	        	args = tr.getPhysicalBn(name);
	        	authPort = Integer.parseInt(args[1]);
	        	
	        	phAddress = connectionSocket.getInetAddress();
	            phAddressStr = phAddress.getHostAddress(); 
	            
	            if (args[0].equals("OFFLINE")) {
	            	SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
	            	return;
	            } else if (!args[0].equals(phAddressStr)) {
	            	SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
	            	return;
	            }
	        }
			AppLogger.getInstance().consolePrint(pre + "BN "+name+" IS VALID AT ADDR "+phAddressStr+":"+authPort);
	        
	    	//create obj first in order to open its threads
	        BlueNode bn = null;
			try {
				bn = new BlueNode(name, bnPub, phAddressStr, authPort);
			} catch (Exception e) {
				e.printStackTrace();
				bn.killtasks();
				SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
				return;
			}
	        
			SocketUtilities.sendAESEncryptedStringData("ASSOSIATING "+bn.getServerSendPort()+" "+bn.getServerReceivePort(), socketWriter, sessionKey);
			AppLogger.getInstance().consolePrint(pre + "remote auth port "+bn.getRemoteAuthPort()+" upport "+bn.getServerSendPort()+" downport "+bn.getServerReceivePort());
	    	
	    	try {
				blueNodeTable.leaseBn(bn);
				AppLogger.getInstance().consolePrint(pre + "LEASED REMOTE BN "+name);
			} catch (Exception e) {
				e.printStackTrace();
				bn.killtasks();
			}        
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

    /**
     * A Bn has requested to tell him if we can receive his packets
     */
    static void Uping(BlueNode bn, DataOutputStream outputWriter, SecretKey sessionKey) {
    	bn.setUping(false);
    	try {
			SocketUtilities.sendAESEncryptedStringData("SET", outputWriter, sessionKey);
			try {
	            sleep(2000);
	        } catch (InterruptedException ex) {
	            ex.printStackTrace();
	        }
	    	
	        if (bn.getUPing()) {
	        	SocketUtilities.sendAESEncryptedStringData("OK", outputWriter, sessionKey);
	        } else {
	        	SocketUtilities.sendAESEncryptedStringData("FAILED", outputWriter, sessionKey);
	        }
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * A Bn has requested to get some packets. That's all!
     */
    static void Dping(BlueNode bn) {
        byte[] data = UnityPacket.buildDpingPacket();
        try {
        	for (int i=0; i<3; i++) {
        		bn.getSendQueue().offer(data);
        		sleep(200);
        	}			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    static void releaseBn(BlueNodeTable blueNodeTable, String BlueNodeName) {
		Lock lock = null;
    	try {
    		lock = blueNodeTable.aquireLock();
			blueNodeTable.releaseBn(lock, BlueNodeName);
		} catch (Exception e) {
			
		} finally {
    		lock.unlock();
		}
    }
    
    static void giveLRNs(LocalRedNodeTable localRedNodeTable, DataOutputStream socketWriter, SecretKey sessionKey) {
    	GlobalSocketFunctions.sendLocalRedNodes(localRedNodeTable, socketWriter, sessionKey);
    }
    
    public static void getLRNs(BlueNodeTable blueNodeTable, BlueNode bn, DataInputStream socketReader, SecretKey sessionKey) {
		GlobalSocketFunctions.getRemoteRedNodes(blueNodeTable, bn, socketReader, sessionKey);
	}

    static void exchangeRNs(LocalRedNodeTable localRedNodeTable, BlueNodeTable blueNodeTable, BlueNode bn, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) {
    	GlobalSocketFunctions.sendLocalRedNodes(localRedNodeTable, socketWriter, sessionKey);
    	GlobalSocketFunctions.getRemoteRedNodes(blueNodeTable, bn, socketReader, sessionKey);
    }
    
    static void getLocalRnHostnameByVaddress(LocalRedNodeTable localRedNodeTable, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) {
        try {
	    	if (localRedNodeTable.checkOnlineByVaddress(vaddress)) {
	        	SocketUtilities.sendAESEncryptedStringData(localRedNodeTable.getRedNodeInstanceByAddr(vaddress).getHostname(), socketWriter, sessionKey);
	        } else {
	        	SocketUtilities.sendAESEncryptedStringData("OFFLINE", socketWriter, sessionKey);
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    static void getLocalRnVaddressByHostname(LocalRedNodeTable localRedNodeTable, String hostname, DataOutputStream socketWriter, SecretKey sessionKey) {
    	try {
	    	if (localRedNodeTable.checkOnlineByHostname(hostname)) {
	    		SocketUtilities.sendAESEncryptedStringData(localRedNodeTable.getRedNodeInstanceByHn(hostname).getAddress().asString(), socketWriter, sessionKey);
	        } else {
	        	SocketUtilities.sendAESEncryptedStringData("OFFLINE", socketWriter, sessionKey);
	        }
    	} catch (Exception e) {
        	e.printStackTrace();
        }
    }

    static void getFeedReturnRoute(BlueNodeTable blueNodeTable,BlueNode bn, String hostname, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) {
        try {
			blueNodeTable.leaseRRn(bn, hostname, VirtualAddress.valueOf(vaddress));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public static void getRRNToBeReleasedByHn(BlueNode bn, String hostname, DataOutputStream socketWriter, SecretKey sessionKey) {
		Lock lock = null;
    	try {
    		lock = bn.getTable().aquireLock();
			bn.getTable().release(lock, hostname);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
    		lock.unlock();
		}
	}
	
	public static void getRRNToBeReleasedByVaddr(BlueNode bn, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) {
		Lock lock = null;
    	try {
    		lock = bn.getTable().aquireLock();
			bn.getTable().releaseByVirtualAddress(lock, vaddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void check(BlueNodeTable blueNodeTable, String blueNodeName, DataOutputStream socketWriter, SecretKey sessionKey) {
		//if associated reset idleTime and update timestamp as well
		Lock lock = null;
		try {
			lock = blueNodeTable.aquireLock();
			var o = blueNodeTable.getBlueNodeInstanceByName(lock, blueNodeName);
			if (o.isPresent()) {
				o.get().resetIdleTime();
				o.get().updateTimestamp();
			}
		} catch (InterruptedException e) {
			AppLogger.getInstance().consolePrint(e.getLocalizedMessage());
		} finally {
			lock.unlock();
		}

		try {
			SocketUtilities.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}	
