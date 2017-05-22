package kostiskag.unitynetwork.bluenode.socket.blueNodeClient;

import static java.lang.Thread.sleep;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.LinkedList;

import javax.crypto.SecretKey;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.RunData.instances.RemoteRedNodeInstance;
import kostiskag.unitynetwork.bluenode.functions.CryptoMethods;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.SocketFunctions;

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
	private final BlueNodeInstance bn;
	private final PublicKey pub;
	private SecretKey sessionKey;
	private InetAddress phAddress;
	private Socket sessionSocket;
	private DataInputStream socketReader;
	private DataOutputStream socketWriter;
	private boolean connected = false;

	public BlueNodeClient(BlueNodeInstance bn) {
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
			this.phAddress = SocketFunctions.getAddress(phAddressStr);
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
    		sessionSocket = SocketFunctions.absoluteConnect(phAddress, authPort);
			//socket.setSoTimeout(timeout);
			
			socketReader = SocketFunctions.makeDataReader(sessionSocket);
			socketWriter = SocketFunctions.makeDataWriter(sessionSocket);
			
			sessionKey = CryptoMethods.generateAESSessionkey();
			if (sessionKey == null) {
				throw new Exception("Could not generate session key.");
			}

			String keyStr = CryptoMethods.objectToBase64StringRepresentation(sessionKey);
			SocketFunctions.sendRSAEncryptedStringData(keyStr, socketWriter, pub);
			
			String[] args = SocketFunctions.receiveAESEncryptedStringData(socketReader, sessionKey);
			System.out.println(args[0]);
			
			if(!args[0].equals("BLUENODE") || !args[1].equals(name)) {
				throw new Exception("Bluenode wrong name.");
			}
			System.out.println(args[0]+" "+args[1]);
			
			//this bn is to be authenticated by the target bn
			args = SocketFunctions.sendReceiveAESEncryptedStringData("BLUENODE "+App.bn.name, socketReader, socketWriter, sessionKey);
			
			//decode question
			byte[] question = CryptoMethods.base64StringTobytes(args[0]);
			
			//decrypt with private
			String answer = CryptoMethods.decryptWithPrivate(question, App.bn.bluenodeKeys.getPrivate());
			
			//send back plain answer
			args = SocketFunctions.sendReceiveAESEncryptedStringData(answer, socketReader, socketWriter, sessionKey);
			
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
			SocketFunctions.connectionClose(sessionSocket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		connected = false;
	}
	
	public boolean checkBlueNode() {
		if (connected) {
			String[] args;
			try {
				args = SocketFunctions.sendReceiveAESEncryptedStringData("CHECK", socketReader, socketWriter, sessionKey);
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
			
			if (name.equals(App.bn.name)) {
				closeConnection();
				App.bn.ConsolePrint(pre + "BNs are not allowed to create a u-turn association");
				throw new Exception(pre+"BNs are not allowed to create a u-turn association");
			} else if (App.bn.blueNodesTable.checkBlueNode(name)) {
				closeConnection();
				App.bn.ConsolePrint(pre+"BN is already an associated memeber.");
				throw new Exception(pre+"BN is already an associated memeber.");
			}
			
			//ports
			int downport = 0;
			int upport = 0;
			
			//lease
	        String[] args = SocketFunctions.sendReceiveAESEncryptedStringData("ASSOCIATE", socketReader, socketWriter, sessionKey);
	        if (args[0].equals("ERROR")) {
	        	App.bn.ConsolePrint(pre + "Connection error");
	            closeConnection();
	            throw new Exception(pre+"ERROR");  
	        } 
	        
	        downport = Integer.parseInt(args[1]);
            upport = Integer.parseInt(args[2]);
            App.bn.ConsolePrint(pre + " upport " + upport + " downport " + downport);
	        
	        //build the object
	    	BlueNodeInstance node;
			try {
				node = new BlueNodeInstance(name, pub, phAddressStr, authPort, upport, downport);	        
			} catch (Exception e) {
				e.printStackTrace();
				bn.killtasks();
				closeConnection();
				return;
			}       
			
			//lease to local bn table
			App.bn.blueNodesTable.leaseBn(node);
			closeConnection();
			App.bn.ConsolePrint(pre + "LEASED REMOTE BN "+name);
		}
    }		
		
	public boolean uPing() {
		if (bn != null) {
			if (connected) {
				byte[] data = UnityPacket.buildUpingPacket();
				try {
					SocketFunctions.sendReceiveAESEncryptedStringData("UPING", socketReader, socketWriter, sessionKey);
					//wait to get set
			        for (int i=0; i<3; i++) {
			        	bn.getSendQueue().offer(data);
			        	try {
							sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
			        }
			        
			        String[] args = SocketFunctions.receiveAESEncryptedStringData(socketReader, sessionKey);	
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
					SocketFunctions.sendAESEncryptedStringData("DPING", socketWriter, sessionKey);
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
					SocketFunctions.sendAESEncryptedStringData("RELEASE", socketWriter, sessionKey);
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
					SocketFunctions.sendAESEncryptedStringData("GET_RED_NODES", socketWriter, sessionKey);
					GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}   
				closeConnection();
			}
		}
	}
	
	/**
	 * The difference of this method from getRemoteRedNodes is that the former requests and
	 * then internally leases the returned results although this method requests but returns the 
	 * results as a built object.
	 * 
	 * @return a linked list with all the retrieved RemoteRedNodeInstance
	 */
	public LinkedList<RemoteRedNodeInstance> getRemoteRedNodesObj() {
		LinkedList<RemoteRedNodeInstance> fetched = new LinkedList<RemoteRedNodeInstance>();
		if (bn != null) {
			if (connected) {
				try {
					String[] args = SocketFunctions.sendReceiveAESEncryptedStringData("GET_RED_NODES", socketReader, socketWriter, sessionKey);
					int count = Integer.parseInt(args[1]);
			        for (int i = 0; i < count; i++) {
			            args = SocketFunctions.receiveAESEncryptedStringData(socketReader, sessionKey);
			            RemoteRedNodeInstance r =  new RemoteRedNodeInstance(args[0], args[1], bn);                    
			            fetched.add(r); 
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}        
		        closeConnection();
			}
		}
		return fetched;
	}
	
	public void giveLocalRedNodes() {
		if (bn != null) {
			if (connected) {
				try {
					SocketFunctions.sendAESEncryptedStringData("GIVE_RED_NODES", socketWriter, sessionKey);
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
					SocketFunctions.sendAESEncryptedStringData("EXCHANGE_RED_NODES", socketWriter, sessionKey);
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
					args = SocketFunctions.sendReceiveAESEncryptedStringData("GET_RED_VADDRESS "+hostname, socketReader, socketWriter, sessionKey);
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
					String[] args = SocketFunctions.sendReceiveAESEncryptedStringData("GET_RED_HOSTNAME "+vaddress, socketReader, socketWriter, sessionKey);
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
					SocketFunctions.sendAESEncryptedStringData("RELEASE_REMOTE_REDNODE_BY_HN "+hostname, socketWriter, sessionKey);
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
					SocketFunctions.sendAESEncryptedStringData("RELEASE_REMOTE_REDNODE_BY_VADDRESS "+vaddress, socketWriter, sessionKey);
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
					SocketFunctions.sendAESEncryptedStringData("LEASE_REMOTE_REDNODE "+hostname+" "+vaddress, socketWriter, sessionKey);
				} catch (Exception e) {
					e.printStackTrace();
				}        
				closeConnection(); 
			}
		}
	}
}
