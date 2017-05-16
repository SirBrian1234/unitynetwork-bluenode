package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.SocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeFunctions {

    private static String pre = "^Blue Node Functions";
    
    static void associate(String name, PublicKey bnPub, Socket connectionSocket, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) {        
    	try {
	    	App.bn.ConsolePrint(pre + "STARTING A BLUE AUTH AT " + Thread.currentThread().getName());
	    	InetAddress phAddress;
	    	String phAddressStr;
	    	int authPort = 0;
	    	String[] args;
	        
	        if (App.bn.name.equals(name)) {
	        	SocketFunctions.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
	        	return;
	        } else if (App.bn.blueNodesTable.checkBlueNode(name)) {
	        	SocketFunctions.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
	        	return;
	        } else {
	        	//tracker lookup
	        	TrackerClient tr = new TrackerClient();
	        	args = tr.getPhysicalBn(name);
	        	authPort = Integer.parseInt(args[1]);
	        	
	        	phAddress = connectionSocket.getInetAddress();
	            phAddressStr = phAddress.getHostAddress(); 
	            
	            if (args[0].equals("OFFLINE")) {
	            	SocketFunctions.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
	            	return;
	            } else if (!args[0].equals(phAddressStr)) {
	            	SocketFunctions.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
	            	return;
	            }
	        }
	        App.bn.ConsolePrint(pre + "BN "+name+" IS VALID AT ADDR "+phAddressStr+":"+authPort);
	        
	    	//create obj first in order to open its threads
	        BlueNodeInstance bn = null;
			try {
				bn = new BlueNodeInstance(name, bnPub, phAddressStr, authPort);			
			} catch (Exception e) {
				e.printStackTrace();
				bn.killtasks();
				SocketFunctions.sendAESEncryptedStringData("ERROR", socketWriter, sessionKey);
				return;
			}
	        
			SocketFunctions.sendAESEncryptedStringData("ASSOSIATING "+bn.getServerSendPort()+" "+bn.getServerReceivePort(), socketWriter, sessionKey);        
			App.bn.ConsolePrint(pre + "remote auth port "+bn.getRemoteAuthPort()+" upport "+bn.getServerSendPort()+" downport "+bn.getServerReceivePort());
	    	
	    	try {
				App.bn.blueNodesTable.leaseBn(bn);
				App.bn.ConsolePrint(pre + "LEASED REMOTE BN "+name);
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
    static void Uping(BlueNodeInstance bn, DataOutputStream outputWriter, SecretKey sessionKey) {
    	bn.setUping(false);
    	try {
			SocketFunctions.sendAESEncryptedStringData("SET", outputWriter, sessionKey);
			try {
	            sleep(2000);
	        } catch (InterruptedException ex) {
	            ex.printStackTrace();
	        }
	    	
	        if (bn.getUPing()) {
	        	SocketFunctions.sendAESEncryptedStringData("OK", outputWriter, sessionKey);
	        } else {
	        	SocketFunctions.sendAESEncryptedStringData("FAILED", outputWriter, sessionKey);
	        }
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * A Bn has requested to get some packets. That's all!
     */
    static void Dping(BlueNodeInstance bn) {
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
			App.bn.blueNodesTable.releaseBn(BlueNodeName);
		} catch (Exception e) {
			
		}        
    }
    
    static void giveLRNs(DataOutputStream socketWriter, SecretKey sessionKey) {
    	GlobalSocketFunctions.sendLocalRedNodes(socketWriter, sessionKey);
    }
    
    public static void getLRNs(BlueNodeInstance bn, DataInputStream socketReader, SecretKey sessionKey) {
		GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);
	}

    static void exchangeRNs(BlueNodeInstance bn, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) {
    	GlobalSocketFunctions.sendLocalRedNodes(socketWriter, sessionKey);
    	GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);           
    }
    
    static void getLocalRnHostnameByVaddress(String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) {
        try {
	    	if (App.bn.localRedNodesTable.checkOnlineByVaddress(vaddress)) {
	        	SocketFunctions.sendAESEncryptedStringData(App.bn.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).getHostname(), socketWriter, sessionKey);
	        } else {
	        	SocketFunctions.sendAESEncryptedStringData("OFFLINE", socketWriter, sessionKey);
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    static void getLocalRnVaddressByHostname(String hostname, DataOutputStream socketWriter, SecretKey sessionKey) {
    	try {
	    	if (App.bn.localRedNodesTable.checkOnlineByHostname(hostname)) {
	    		SocketFunctions.sendAESEncryptedStringData(App.bn.localRedNodesTable.getRedNodeInstanceByHn(hostname).getVaddress(), socketWriter, sessionKey);
	        } else {
	        	SocketFunctions.sendAESEncryptedStringData("OFFLINE", socketWriter, sessionKey);
	        }
    	} catch (Exception e) {
        	e.printStackTrace();
        }
    }

    static void getFeedReturnRoute(BlueNodeInstance bn, String hostname, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) {        
        try {
			App.bn.blueNodesTable.leaseRRn(bn, hostname, vaddress);
			SocketFunctions.sendAESEncryptedStringData("OK", socketWriter, sessionKey);   
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public static void getRRNToBeReleasedByHn(BlueNodeInstance bn, String hostname, DataOutputStream socketWriter, SecretKey sessionKey) {
		try {
			bn.table.releaseByHostname(hostname);
			SocketFunctions.sendAESEncryptedStringData("OK", socketWriter, sessionKey);   
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void getRRNToBeReleasedByVaddr(BlueNodeInstance bn, String vaddress, DataOutputStream socketWriter, SecretKey sessionKey) {
		try {
			bn.table.releaseByVaddr(vaddress);
			SocketFunctions.sendAESEncryptedStringData("OK", socketWriter, sessionKey);   
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void check(String blueNodeName, DataOutputStream socketWriter, SecretKey sessionKey) {
		//if associated reset idleTime and update timestamp as well
		if (App.bn.blueNodesTable.checkBlueNode(blueNodeName)) {
			try {
				BlueNodeInstance bn = App.bn.blueNodesTable.getBlueNodeInstanceByName(blueNodeName);
				bn.resetIdleTime();
				bn.updateTime();
				SocketFunctions.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}	
