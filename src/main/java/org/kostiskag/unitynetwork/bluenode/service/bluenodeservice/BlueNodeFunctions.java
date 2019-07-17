package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.GlobalSocketFunctions;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeFunctions {

    private static String pre = "^Blue Node Functions";
    
    static void associate(String name, PublicKey bnPub, Socket connectionSocket, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) {        
    	try {
			AppLogger.getInstance().consolePrint(pre + "STARTING A BLUE AUTH AT " + Thread.currentThread().getName());
	    	InetAddress phAddress;
	    	String phAddressStr;
	    	int authPort = 0;
	    	String[] args;
	        
	        if (App.bn.name.equals(name)) {
	        	SocketUtilities.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
	        	return;
	        } else if (App.bn.blueNodeTable.checkBlueNode(name)) {
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
				App.bn.blueNodeTable.leaseBn(bn);
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
    
    static void releaseBn(String BlueNodeName) {
        try {
			App.bn.blueNodeTable.releaseBn(BlueNodeName);
		} catch (Exception e) {
			
		}        
    }
    
    static void giveLRNs(DataOutputStream socketWriter, SecretKey sessionKey) {
    	GlobalSocketFunctions.sendLocalRedNodes(socketWriter, sessionKey);
    }
    
    public static void getLRNs(BlueNode bn, DataInputStream socketReader, SecretKey sessionKey) {
		GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);
	}

    static void exchangeRNs(BlueNode bn, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) {
    	GlobalSocketFunctions.sendLocalRedNodes(socketWriter, sessionKey);
    	GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);           
    }
    
    static void getLocalRnHostnameByVaddress(String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) {
        try {
	    	if (App.bn.localRedNodesTable.checkOnlineByVaddress(vaddress)) {
	        	SocketUtilities.sendAESEncryptedStringData(App.bn.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).getHostname(), socketWriter, sessionKey);
	        } else {
	        	SocketUtilities.sendAESEncryptedStringData("OFFLINE", socketWriter, sessionKey);
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    static void getLocalRnVaddressByHostname(String hostname, DataOutputStream socketWriter, SecretKey sessionKey) {
    	try {
	    	if (App.bn.localRedNodesTable.checkOnlineByHostname(hostname)) {
	    		SocketUtilities.sendAESEncryptedStringData(App.bn.localRedNodesTable.getRedNodeInstanceByHn(hostname).getVaddress(), socketWriter, sessionKey);
	        } else {
	        	SocketUtilities.sendAESEncryptedStringData("OFFLINE", socketWriter, sessionKey);
	        }
    	} catch (Exception e) {
        	e.printStackTrace();
        }
    }

    static void getFeedReturnRoute(BlueNode bn, String hostname, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) {
        try {
			App.bn.blueNodeTable.leaseRRn(bn, hostname, vaddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public static void getRRNToBeReleasedByHn(BlueNode bn, String hostname, DataOutputStream socketWriter, SecretKey sessionKey) {
		try {
			bn.table.releaseByHostname(hostname);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void getRRNToBeReleasedByVaddr(BlueNode bn, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) {
		try {
			bn.table.releaseByVaddr(vaddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void check(String blueNodeName, DataOutputStream socketWriter, SecretKey sessionKey) {
		//if associated reset idleTime and update timestamp as well
		if (App.bn.blueNodeTable.checkBlueNode(blueNodeName)) {
			try {
				BlueNode bn = App.bn.blueNodeTable.getBlueNodeInstanceByName(blueNodeName);
				bn.resetIdleTime();
				bn.updateTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			SocketUtilities.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}	
