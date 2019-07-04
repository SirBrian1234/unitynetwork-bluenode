package org.kostiskag.unitynetwork.bluenode.socket.trackClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class TrackerClient {

	private final String pre = "^TrackerClient ";
	private final PublicKey trackerPublic;
	private final String name;
	private InetAddress addr;
	private int port;
	private Socket socket;
	private SecretKey sessionKey;
	private DataInputStream reader;
	private DataOutputStream writer;
	private String reason;
	private boolean connected = false;

	public TrackerClient() {
		this.name = App.bn.name;
		this.trackerPublic = App.bn.trackerPublicKey;
		
		if (trackerPublic == null) {
			System.err.println(pre+"no tracker public key was set.");
			return;
		}
		
		this.port = App.bn.trackerPort;
		try {
			this.addr = SocketUtilities.getAddress(App.bn.trackerAddress);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
			return;
		}
		
		try {
			this.socket = SocketUtilities.absoluteConnect(addr, port);
			
			this.reader = SocketUtilities.makeDataReader(socket);
			this.writer = SocketUtilities.makeDataWriter(socket);
			
			sessionKey = CryptoUtilities.generateAESSessionkey();
			if (sessionKey == null) {
				reason = "NO_SESSION_KEY";
				throw new Exception("NO_SESSION_KEY");
			}
			
			String keyStr = CryptoUtilities.objectToBase64StringRepresentation(sessionKey);
			SocketUtilities.sendRSAEncryptedStringData(keyStr, writer, App.bn.trackerPublicKey);
			
			String[] args = SocketUtilities.receiveAESEncryptedStringData(reader, sessionKey);
			System.out.println(args[0]);
			
			if(!args[0].equals("UnityTracker")) {
				reason = "WELLCOME_MSG_ERROR";
				throw new Exception("WELLCOME_MSG_ERROR");
			}
			
			args = SocketUtilities.sendReceiveAESEncryptedStringData("BLUENODE"+" "+name, reader, writer, sessionKey);
			
			if (args[0].equals("PUBLIC_NOT_SET")) {
				SocketUtilities.sendAESEncryptedStringData("EXIT", writer, sessionKey);
				reason = "KEY_NOT_SET";
				throw new Exception("This BN's public key is not set.");
			}
			
			//decode question
			byte[] question = CryptoUtilities.base64StringTobytes(args[0]);
			
			//decrypt with private
			String answer = CryptoUtilities.decryptWithPrivate(question, App.bn.bluenodeKeys.getPrivate());
			
			//send back plain answer
			args = SocketUtilities.sendReceiveAESEncryptedStringData(answer, reader, writer, sessionKey);
			
			if (args[0].equals("OK")) {
				connected = true;
			} else {
				throw new Exception("Bluenode could not be authenticated from tracker.");
			}
			
		} catch (Exception e2) {
			App.bn.ConsolePrint(pre+"Connection dropped for tracker at "+socket.getInetAddress().getHostAddress());
			e2.printStackTrace();
			closeCon();
			connected = false;
			return;
		}		
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	private void closeCon() {
		if (!socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean leaseBn(int authport) {
		if (connected) {
			String[] args = null;
			try {
				App.bn.ConsolePrint(pre+"LEASE"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("LEASE"+" "+authport, reader, writer, sessionKey);
				closeCon();
				if (args[0].equals("LEASED")) {
					App.bn.echoAddress = args[1];
					App.bn.window.setEchoIpAddress(args[1]);
					App.bn.ConsolePrint(pre + "ECHO ADDRESS IS " + App.bn.echoAddress);
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			closeCon();			
		}
		return false;
	}

	public boolean releaseBn() {
		if (connected) {
			String[] args;
			try {
				App.bn.ConsolePrint(pre+"RELEASE"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("RELEASE", reader, writer, sessionKey);
				closeCon();
				if (args[0].equals("RELEASED")) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			closeCon();
		}
		return false;
	}

	/**
	 * Lookups and retrieves a Blue Node's address based on its name.
	 * 
	 * @param BNHostname the target Blue Node's name
	 * @return returns a string array with two elements. The first is the IP address, the second is the auth port
	 */
	public String[] getPhysicalBn(String BNHostname) {
		if (connected) {
			String[] args;
			try {
				App.bn.ConsolePrint(pre+"GETPH"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("GETPH"+" "+BNHostname, reader, writer, sessionKey);
				closeCon();
				if (!args[0].equals("NOT_FOUND")) {
					return args;
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			closeCon();
		}
		return null;
	}

	/**
	 * Request permission from tracker to lease a Local Red Node over the calling Blue Node.
	 * 
	 * @param Hostname the local red node's hostname
	 * @param Username the hostname's user owner
	 * @param Password a hashed version of the user's password
	 * 
	 * @return On a successful authorize, returns a virtual address for the given hostname. 
	 * Otherwise returns an error message in a string format.
	 */
	public String leaseRn(String Hostname, String Username, String Password) {
		if (connected) {
	    	String[] args;
			try {
				App.bn.ConsolePrint(pre+"LEASE_RN"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("LEASE_RN"+" "+Hostname+" "+Username+" "+Password, reader, writer, sessionKey);
				closeCon();
		        if (args[0].equals("LEASED")) {
		            return args[1];
		        } else {
		            return args[0];
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
			closeCon();
	    }
		return null;
    }    

	/**
	 * Notifies the network with a Local red node's release.
	 * 
	 * @param hostname the local red node's name
	 */
    public void releaseRnByHostname(String hostname) {
    	if (connected) {
	        try {
	        	App.bn.ConsolePrint(pre+"RELEASE_RN"+" at "+socket.getInetAddress().getHostAddress());
				SocketUtilities.sendAESEncryptedStringData("RELEASE_RN"+" "+hostname, writer, sessionKey);
			} catch (Exception e) {
				e.printStackTrace();
			}       
	        closeCon();
    	}
    }
    
    /**
     * Retrieves the name of the BlueNode in which the given remote red node hostname
     * is connected.
     * 
     * @param hostanme the remote red node's hostname
     * @return returns the blue node's name
     */
    public String checkRnOnlineByHostname(String hostanme) {
    	if (connected) {
	    	String[] args;
			try {
				App.bn.ConsolePrint(pre+"CHECK_RN"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("CHECK_RN"+" "+hostanme, reader, writer, sessionKey);
				closeCon();
		        if (args[0].equals("OFFLINE")) {
		            return null;
		        } else {
		            return args[1];
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
			closeCon();
    	}
    	return null;
    }
    
    /**
     * Retrieves the name of the BlueNode in which the given remote red node with vaddress
     * is connected.
     * 
     * @param vaddress the remote red node's vaddress
     * @return returns the blue node's name
     */
    public String checkRnOnlineByVaddr(String vaddress) {            
    	if (connected) {
	        String[] args = null;
			try {
				App.bn.ConsolePrint(pre+"CHECK_RNA"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("CHECK_RNA"+" "+vaddress, reader, writer, sessionKey);
				closeCon();
				if (args[0].equals("OFFLINE")) {
		            return null;
		        } else {
		            return args[1];
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}        
	        closeCon();
	    }
    	return null;
    }
    
    public PublicKey getBlueNodesPubKey(String name) {            
    	if (connected) {
	        String[] args = null;
			try {
				App.bn.ConsolePrint(pre+"GETBNPUB"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("GETBNPUB"+" "+name, reader, writer, sessionKey);
				closeCon();
				if (args[0].equals("NONE")) {
		            return null;
		        } else {
		            return (PublicKey) CryptoUtilities.base64StringRepresentationToObject(args[0]);
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}        
	        closeCon();
	    }
    	return null;
    }
    
    public PublicKey getRedNodesPubKey(String hostname) {            
    	if (connected) {
	        String[] args = null;
			try {
				App.bn.ConsolePrint(pre+"GETRNPUB"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("GETRNPUB"+" "+hostname, reader, writer, sessionKey);
				closeCon();
				if (args[0].equals("NONE")) {
		            return null;
		        } else {
		            return (PublicKey) CryptoUtilities.base64StringRepresentationToObject(args[0]);
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}        
	        closeCon();
	    }
    	return null;
    }
    
    public String revokePubKey() {
		if (connected) {
			String[] args = null;
			try {
				App.bn.ConsolePrint(pre+"REVOKEPUB"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("REVOKEPUB", reader, writer, sessionKey);
				closeCon();
				return args[0];
			} catch (Exception e) {
				e.printStackTrace();
			}        
	        closeCon();
	        return null;
		}
		return reason;
	}
    
    /**
     * Collect's a tracker's public key needs a plain connection
     * It's a bit hardwired as after collection
     * it writes a file and updates bn's tracker public key
     * to use.
     */
	public static void getPubKey() {
		int port = App.bn.trackerPort;
		InetAddress addr;
		try {
			addr = SocketUtilities.getAddress(App.bn.trackerAddress);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return;
		}
		
		Socket socket = null;
		try {
			socket = SocketUtilities.absoluteConnect(addr, port);
			
			DataInputStream reader = SocketUtilities.makeDataReader(socket);
			DataOutputStream writer = SocketUtilities.makeDataWriter(socket);
			
			App.bn.ConsolePrint("Tracker "+"GETPUB"+" at "+socket.getInetAddress().getHostAddress());
			String[] args = SocketUtilities.sendReceivePlainStringData("GETPUB", reader, writer);
			     
			App.bn.trackerPublicKey = (PublicKey) CryptoUtilities.base64StringRepresentationToObject(args[0]);
	        CryptoUtilities.objectToFile(App.bn.trackerPublicKey, new File(App.trackerPublicKeyFileName));
	        
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Offer public key needs an rsa connection based on tracker's public
	 * but no authentication for the bluenode's part
	 *  
	 * @param ticket
	 * @return
	 */
	public static String offerPubKey(String ticket) {
		String pre = "^offerPubKey ";
		String name = App.bn.name;
		PublicKey trackerPublic = App.bn.trackerPublicKey;
		
		if (trackerPublic == null) {
			System.err.println(pre+"no tracker public key was set.");
			return null;
		}
		
		int port = App.bn.trackerPort;
		InetAddress addr;
		try {
			addr = SocketUtilities.getAddress(App.bn.trackerAddress);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
			return null;
		}
		
		Socket socket = null;
		try {
			socket = SocketUtilities.absoluteConnect(addr, port);
			
			DataInputStream reader = SocketUtilities.makeDataReader(socket);
			DataOutputStream writer = SocketUtilities.makeDataWriter(socket);
			
			SecretKey sessionKey = CryptoUtilities.generateAESSessionkey();
			if (sessionKey == null) {
				throw new Exception();
			}
			
			String keyStr = CryptoUtilities.objectToBase64StringRepresentation(sessionKey);
			SocketUtilities.sendRSAEncryptedStringData(keyStr, writer, App.bn.trackerPublicKey);
			
			String[] args = SocketUtilities.receiveAESEncryptedStringData(reader, sessionKey);
			System.out.println(args[0]);
			
			if(!args[0].equals("UnityTracker")) {
				throw new Exception();
			}
			
			args = SocketUtilities.sendReceiveAESEncryptedStringData("BLUENODE"+" "+name, reader, writer, sessionKey);
		
			if (!args[0].equals("PUBLIC_NOT_SET")) {
				SocketUtilities.sendAESEncryptedStringData("EXIT", writer, sessionKey);
				socket.close();
				return "KEY_IS_SET";
			}
			
			PublicKey pub = App.bn.bluenodeKeys.getPublic(); 
			App.bn.ConsolePrint(pre+"OFFERPUB"+" at "+socket.getInetAddress().getHostAddress());
	    	args = SocketUtilities.sendReceiveAESEncryptedStringData("OFFERPUB"+" "+ticket+" "+CryptoUtilities.objectToBase64StringRepresentation(pub), reader, writer, sessionKey);
			
	    	socket.close();
	    	return args[0];
	    	
			} catch (Exception e) {
				e.printStackTrace();
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}   
			}        
	        return "NOT_CONNECTED";
	}
}
