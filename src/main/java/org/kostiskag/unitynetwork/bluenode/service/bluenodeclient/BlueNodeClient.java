package org.kostiskag.unitynetwork.bluenode.service.bluenodeclient;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;
import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.service.CommonServiceFunctions;


/**
 * 
 * 
 * @author Konstantinos Kagiampakis
 */
public final class BlueNodeClient {

	public static String pre = "^BlueNodeClient ";

	private static PrivateKey bluenodePrivate;
	private static BlueNodeTable blueNodeTable;
	private static LocalRedNodeTable localRedNodeTable;
	private static String localBluenodeName;

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
	private boolean connected;


	public static void configureBlueNodeClient(String localBluenodeName, LocalRedNodeTable redNodeTable, BlueNodeTable blueNodeTable, PrivateKey bluenodePrivate) {
		if (localBluenodeName == null || blueNodeTable == null || redNodeTable == null || bluenodePrivate == null) {
			throw new IllegalArgumentException("null data were given");
		}

		BlueNodeClient.localBluenodeName = localBluenodeName;
		BlueNodeClient.blueNodeTable = blueNodeTable;
		BlueNodeClient.localRedNodeTable = localRedNodeTable;
		BlueNodeClient.bluenodePrivate = bluenodePrivate;
	}

	public BlueNodeClient(BlueNode bn) {
		this.bn = bn;
		this.name = bn.getHostname();
		this.phAddressStr = bn.getAddress().asString();
		this.phAddress = bn.getAddress().asInet();
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
			args = SocketUtilities.sendReceiveAESEncryptedStringData("BLUENODE "+ localBluenodeName, socketReader, socketWriter, sessionKey);
			
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


	public void associateClient(Lock lock) throws IllegalAccessException, IOException, InterruptedException, GeneralSecurityException {
		if (connected) {
			if (localBluenodeName.equals(name)) {
				closeConnection();
				AppLogger.getInstance().consolePrint(pre + "BNs are not allowed to create a u-turn association");
				throw new IllegalAccessException(pre + "BNs are not allowed to create a u-turn association");
			}

			if (BlueNodeClient.blueNodeTable.getOptionalEntry(lock, name).isPresent()) {
				closeConnection();
				throw new IllegalAccessException(pre + "BN is already an associated memeber.");
			}

			//lease
			String[] args = SocketUtilities.sendReceiveAESEncryptedStringData("ASSOCIATE", socketReader, socketWriter, sessionKey);
			if (args[0].equals("ERROR")) {
				closeConnection();
				throw new IOException(pre + "Connection error");
			}

			//ports
			int downport = Integer.parseInt(args[1]);
			int upport = Integer.parseInt(args[2]);
			AppLogger.getInstance().consolePrint(pre + " upport " + upport + " downport " + downport);

			//build the object
			BlueNode node;
			try {
				node = new BlueNode(name, pub, PhysicalAddress.valueOf(phAddressStr), authPort, upport, downport);
			} catch (UnknownHostException | IllegalAccessException | InterruptedException e) {
				AppLogger.getInstance().consolePrint(pre + e.getMessage());
				bn.killTasks();
				closeConnection();
				throw e;
			}

			//lease to local bn table
			BlueNodeClient.blueNodeTable.leaseBlueNode(lock, node);
			closeConnection();
			AppLogger.getInstance().consolePrint(pre + "LEASED REMOTE BN " + name);
		}
	}

	public void associateClient() throws IllegalAccessException, IOException, InterruptedException, GeneralSecurityException {
		Lock lock = null;
		try {
			lock = BlueNodeClient.blueNodeTable.aquireLock();
			this.associateClient(lock);
		} finally {
			lock.unlock();
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
					CommonServiceFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}   
				closeConnection();
			}
		}
	}
	
	public Collection<RemoteRedNode> getRemoteRedNodesObj() throws IOException, GeneralSecurityException, InterruptedException, IllegalAccessException {
		return CommonServiceFunctions.getRemoteRedNodeCollection(bn, socketReader, sessionKey);
	}
	
	public void giveLocalRedNodes() {
		if (bn != null) {
			if (connected) {
				try {
					SocketUtilities.sendAESEncryptedStringData("GIVE_RED_NODES", socketWriter, sessionKey);
					CommonServiceFunctions.sendLocalRedNodes(BlueNodeClient.localRedNodeTable, socketWriter, sessionKey);
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
					CommonServiceFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);
			        CommonServiceFunctions.sendLocalRedNodes(BlueNodeClient.localRedNodeTable, socketWriter, sessionKey);
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

	public String getRedNodeHostnameByVaddress(VirtualAddress address) {
		if (bn != null) {
			if (connected) {
				try {
					String[] args = SocketUtilities.sendReceiveAESEncryptedStringData("GET_RED_HOSTNAME "+address.asString(), socketReader, socketWriter, sessionKey);
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
