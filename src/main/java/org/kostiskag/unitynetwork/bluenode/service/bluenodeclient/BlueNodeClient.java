package org.kostiskag.unitynetwork.bluenode.service.bluenodeclient;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.service.GlobalSocketFunctions;


/**
 * 
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeClient {

	public static String pre = "^BlueNodeClient ";
	private final String name;
	private final String phAddressStr;
	private final int authPort;
	private final BlueNode bn;
	private final PublicKey pub;
	private SecretKey sessionKey;
	private InetAddress phAddress;
	private Socket sessionSocket;
	private DataInputStream socketReader;
	private DataOutputStream socketWriter;
	private boolean connected = false;
	private static PrivateKey bluenodePrivate;

	public static void configureBlueNodeClient(PrivateKey bluenodePrivate) {
		if (bluenodePrivate == null) {
			throw new IllegalArgumentException("null data were given");
		}

		BlueNodeClient.bluenodePrivate = bluenodePrivate;
	}

	public BlueNodeClient(BlueNode bn) {
		this.bn = bn;
		this.name = bn.getName();
		this.phAddressStr = bn.getPhAddressStr();
		this.phAddress = bn.getPhaddress();
		this.authPort = bn.getRemoteAuthPort();
		this.pub = bn.getPub();
		initConnection();
	}

	public BlueNodeClient(String name, PublicKey pub, String phAddressStr, int authPort) {
		this.name = name;
		this.phAddressStr = phAddressStr;
		this.authPort = authPort;	
		this.pub = pub;
		this.bn = null;
		try {
			this.phAddress = SocketUtilities.getAddress(phAddressStr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		initConnection();
	}
	
	public boolean isConnected() {
		return connected;
	}

	private void initConnection() {
		try {
    		sessionSocket = SocketUtilities.absoluteConnect(phAddress, authPort);
			//socket.setSoTimeout(timeout);
			
			socketReader = SocketUtilities.makeDataReader(sessionSocket);
			socketWriter = SocketUtilities.makeDataWriter(sessionSocket);
			
			sessionKey = CryptoUtilities.generateAESSessionkey();
			if (sessionKey == null) {
				throw new Exception("Could not generate session key.");
			}

			String keyStr = CryptoUtilities.objectToBase64StringRepresentation(sessionKey);
			SocketUtilities.sendRSAEncryptedStringData(keyStr, socketWriter, pub);
			
			String[] args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
			System.out.println(args[0]);
			
			if(!args[0].equals("BLUENODE") || !args[1].equals(name)) {
				throw new Exception("Bluenode wrong name.");
			}
			System.out.println(args[0]+" "+args[1]);
			
			//this bn is to be authenticated by the target bn
			args = SocketUtilities.sendReceiveAESEncryptedStringData("BLUENODE "+ Bluenode.getInstance().getName(), socketReader, socketWriter, sessionKey);
			
			//decode question
			byte[] question = CryptoUtilities.base64StringTobytes(args[0]);
			
			//decrypt with private
			String answer = CryptoUtilities.decryptWithPrivate(question, BlueNodeClient.bluenodePrivate);
			
			//send back plain answer
			args = SocketUtilities.sendReceiveAESEncryptedStringData(answer, socketReader, socketWriter, sessionKey);
			
			if (args[0].equals("OK")) {
				connected = true;
			} 
			System.out.println("connected "+connected);
		} catch (Exception e) {
			e.printStackTrace();
		}        
	}

	private void closeConnection() {
		try {
			SocketUtilities.connectionClose(sessionSocket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		connected = false;
	}
	
	public boolean checkBlueNode() {
		if (connected) {
			String[] args;
			try {
				args = SocketUtilities.sendReceiveAESEncryptedStringData("CHECK", socketReader, socketWriter, sessionKey);
				closeConnection();
				System.out.println(args[0]);
				if (args[0].equals("OK")) {			
					return true;
				}		
			} catch (Exception e) {
				e.printStackTrace();
			}
			closeConnection();
		}
		return false;
	}
	
	public void associateClient() throws Exception {
		if (connected) {
			
			if (name.equals(Bluenode.getInstance().getName())) {
				closeConnection();
				AppLogger.getInstance().consolePrint(pre + "BNs are not allowed to create a u-turn association");
				throw new Exception(pre+"BNs are not allowed to create a u-turn association");
			} else if (Bluenode.getInstance().blueNodeTable.checkBlueNode(name)) {
				closeConnection();
				AppLogger.getInstance().consolePrint(pre+"BN is already an associated memeber.");
				throw new Exception(pre+"BN is already an associated memeber.");
			}
			
			//ports
			int downport = 0;
			int upport = 0;
			
			//lease
	        String[] args = SocketUtilities.sendReceiveAESEncryptedStringData("ASSOCIATE", socketReader, socketWriter, sessionKey);
	        if (args[0].equals("ERROR")) {
				AppLogger.getInstance().consolePrint(pre + "Connection error");
	            closeConnection();
	            throw new Exception(pre+"ERROR");  
	        } 
	        
	        downport = Integer.parseInt(args[1]);
            upport = Integer.parseInt(args[2]);
			AppLogger.getInstance().consolePrint(pre + " upport " + upport + " downport " + downport);
	        
	        //build the object
	    	BlueNode node;
			try {
				node = new BlueNode(name, pub, phAddressStr, authPort, upport, downport);
			} catch (Exception e) {
				e.printStackTrace();
				bn.killtasks();
				closeConnection();
				return;
			}       
			
			//lease to local bn table
			Bluenode.getInstance().blueNodeTable.leaseBn(node);
			closeConnection();
			AppLogger.getInstance().consolePrint(pre + "LEASED REMOTE BN "+name);
		}
    }		
		
	public boolean uPing() {
		if (bn != null) {
			if (connected) {
				byte[] data = UnityPacket.buildUpingPacket();
				try {
					SocketUtilities.sendReceiveAESEncryptedStringData("UPING", socketReader, socketWriter, sessionKey);
					//wait to get set
			        for (int i=0; i<3; i++) {
			        	bn.getSendQueue().offer(data);
			        	try {
							sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
			        }
			        
			        String[] args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
			        closeConnection();
			        
			        if (args[0].equals("OK")) {
			            return true;
			        } else {
			            return false;
			        }
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				closeConnection();
			}
		}
		return false;
	}
	
	/**
	 * I am telling YOU to send ME some packets to check if I can get them 
	 * 
	 * @return
	 */
	public boolean dPing() {
		if (bn != null) {
			if (connected) {
			    bn.setDping(false);
			    try {
					SocketUtilities.sendAESEncryptedStringData("DPING", socketWriter, sessionKey);
					closeConnection();
			        
			        try {
			            sleep(2000);
			        } catch (InterruptedException ex) {
			            ex.printStackTrace();
			        }
			        
			        if (bn.getDPing()) {
			            return true;
			        } else {
			            return false;
			        }
			    } catch (Exception e) {
					e.printStackTrace();
				}
		        closeConnection();
			}
		}		
		return false;
	}
	
	public void removeThisBlueNodesProjection() {
		if (bn != null) {
			if (connected) {
				try {
					SocketUtilities.sendAESEncryptedStringData("RELEASE", socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}        
		        closeConnection();
			}
		}		
	}
	
	public void getRemoteRedNodes() {
		if (bn != null) {
			if (connected) {
				try {
					SocketUtilities.sendAESEncryptedStringData("GET_RED_NODES", socketWriter, sessionKey);
					GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}   
				closeConnection();
			}
		}
	}
	
	public LinkedList<RemoteRedNode> getRemoteRedNodesObj() {
		return GlobalSocketFunctions.getRemoteRedNodesObj(bn, socketReader, sessionKey);
	}
	
	public void giveLocalRedNodes() {
		if (bn != null) {
			if (connected) {
				try {
					SocketUtilities.sendAESEncryptedStringData("GIVE_RED_NODES", socketWriter, sessionKey);
					GlobalSocketFunctions.sendLocalRedNodes(socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}   
				closeConnection();
			}
		}
	}
	
	public void exchangeRedNodes() {
		if (bn != null) {
			if (connected) {
				try {
					SocketUtilities.sendAESEncryptedStringData("EXCHANGE_RED_NODES", socketWriter, sessionKey);
					GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);
			        GlobalSocketFunctions.sendLocalRedNodes(socketWriter, sessionKey);	    
				} catch (Exception e) {
					e.printStackTrace();
				}
		        closeConnection();
			}
		}
	}
	
	public String getRedNodeVaddressByHostname(String hostname) {
		if (bn != null) {
			if (connected) {
				String[] args;
				try {
					args = SocketUtilities.sendReceiveAESEncryptedStringData("GET_RED_VADDRESS "+hostname, socketReader, socketWriter, sessionKey);
					closeConnection();
					if (args[0].equals("OFFLINE")) {
			            return null;
			        } else {
			            return args[0];
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}
				closeConnection();
		    }
		}
		return null;
	}

	public String getRedNodeHostnameByVaddress(String vaddress) {
		if (bn != null) {
			if (connected) {
				try {
					String[] args = SocketUtilities.sendReceiveAESEncryptedStringData("GET_RED_HOSTNAME "+vaddress, socketReader, socketWriter, sessionKey);
					closeConnection();
					if (args[0].equals("OFFLINE")) {
			            return null;
			        } else {
			            return args[0];
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}
				closeConnection();
		    }
		}		
		return null;
	}
	
	public void removeRedNodeProjectionByHn(String hostname) {
		if (bn != null) {
			if (connected) {
				try {
					SocketUtilities.sendAESEncryptedStringData("RELEASE_REMOTE_REDNODE_BY_HN "+hostname, socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}     
				closeConnection();
			}
		}		
	}
	
	public void removeRedNodeProjectionByVaddr(String vaddress) {
		if (bn != null) {
			if (connected) {
				try {
					SocketUtilities.sendAESEncryptedStringData("RELEASE_REMOTE_REDNODE_BY_VADDRESS "+vaddress, socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}     
				closeConnection();
			}
		}
	}
	
	public void feedReturnRoute(String hostname, String vaddress) {
		if (bn != null) {
			if (connected) {
				try {
					SocketUtilities.sendAESEncryptedStringData("LEASE_REMOTE_REDNODE "+hostname+" "+vaddress, socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}        
				closeConnection(); 
			}
		}
	}
}
