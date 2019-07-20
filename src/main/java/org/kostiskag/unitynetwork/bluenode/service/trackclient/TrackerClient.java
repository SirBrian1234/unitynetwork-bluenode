package org.kostiskag.unitynetwork.bluenode.service.trackclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;


/**
 * 
 * @author Konstantinos Kagiampakis
 */
public class TrackerClient {

	private static final String PRE = "^TrackerClient ";
	private static String bluenodeName;
	private static PublicKey trackerPublic;
	private static PhysicalAddress trackerAddress;
	private static int trackerPort;

	private Socket socket;
	private SecretKey sessionKey;
	private DataInputStream reader;
	private DataOutputStream writer;
	private String reason;
	private boolean connected = false;

	public static void configureTracker(String bluenodeName, PublicKey trackerPublic, PhysicalAddress trackerAddress, int trackerPort) {
		if (bluenodeName == null || trackerPublic == null || trackerAddress == null || (trackerPort <=0 && trackerPort > NumericConstraints.MAX_ALLOWED_PORT_NUM.size())) {
			throw new IllegalArgumentException(PRE + " invalid arguments were given!");
		}

		TrackerClient.bluenodeName = bluenodeName;
		TrackerClient.trackerPublic = trackerPublic;
		TrackerClient.trackerAddress = trackerAddress;
		TrackerClient.trackerPort = trackerPort;
	}

	public TrackerClient() {
		//sanitize
		if (bluenodeName == null || trackerPublic == null || trackerAddress == null || (trackerPort <=0 && trackerPort > NumericConstraints.MAX_ALLOWED_PORT_NUM.size())) {
			throw new IllegalArgumentException(PRE + " invalid arguments were given!");
		}

		try {
			this.socket = SocketUtilities.absoluteConnect(trackerAddress.asInet(), trackerPort);
			
			this.reader = SocketUtilities.makeDataReader(socket);
			this.writer = SocketUtilities.makeDataWriter(socket);
			
			sessionKey = CryptoUtilities.generateAESSessionkey();
			if (sessionKey == null) {
				reason = "NO_SESSION_KEY";
				throw new Exception("NO_SESSION_KEY");
			}
			
			String keyStr = CryptoUtilities.objectToBase64StringRepresentation(sessionKey);
			SocketUtilities.sendRSAEncryptedStringData(keyStr, writer, Bluenode.getInstance().trackerPublicKey);
			
			String[] args = SocketUtilities.receiveAESEncryptedStringData(reader, sessionKey);
			System.out.println(args[0]);
			
			if(!args[0].equals("UnityTracker")) {
				reason = "WELLCOME_MSG_ERROR";
				throw new Exception("WELLCOME_MSG_ERROR");
			}
			
			args = SocketUtilities.sendReceiveAESEncryptedStringData("BLUENODE"+" "+bluenodeName, reader, writer, sessionKey);
			
			if (args[0].equals("PUBLIC_NOT_SET")) {
				SocketUtilities.sendAESEncryptedStringData("EXIT", writer, sessionKey);
				reason = "KEY_NOT_SET";
				throw new Exception("This BN's public key is not set.");
			}
			
			//decode question
			byte[] question = CryptoUtilities.base64StringTobytes(args[0]);
			
			//decrypt with private
			String answer = CryptoUtilities.decryptWithPrivate(question, Bluenode.getInstance().bluenodeKeys.getPrivate());
			
			//send back plain answer
			args = SocketUtilities.sendReceiveAESEncryptedStringData(answer, reader, writer, sessionKey);
			
			if (args[0].equals("OK")) {
				connected = true;
			} else {
				throw new Exception("Bluenode could not be authenticated from tracker.");
			}
			
		} catch (Exception e2) {
			AppLogger.getInstance().consolePrint(PRE +"Connection dropped for tracker at "+socket.getInetAddress().getHostAddress());
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
				AppLogger.getInstance().consolePrint(PRE +"LEASE"+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData("LEASE"+" "+authport, reader, writer, sessionKey);
				closeCon();
				if (args[0].equals("LEASED")) {
					MainWindow.getInstance().setEchoIpAddress(args[1]);
					AppLogger.getInstance().consolePrint(PRE + "ECHO ADDRESS IS " + args[1]);
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
				AppLogger.getInstance().consolePrint(PRE +"RELEASE"+" at "+socket.getInetAddress().getHostAddress());
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
				AppLogger.getInstance().consolePrint(PRE +"GETPH"+" at "+socket.getInetAddress().getHostAddress());
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
				AppLogger.getInstance().consolePrint(PRE +"LEASE_RN"+" at "+socket.getInetAddress().getHostAddress());
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
				AppLogger.getInstance().consolePrint(PRE +"RELEASE_RN"+" at "+socket.getInetAddress().getHostAddress());
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
				AppLogger.getInstance().consolePrint(PRE +"CHECK_RN"+" at "+socket.getInetAddress().getHostAddress());
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
				AppLogger.getInstance().consolePrint(PRE +"CHECK_RNA"+" at "+socket.getInetAddress().getHostAddress());
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
				AppLogger.getInstance().consolePrint(PRE +"GETBNPUB"+" at "+socket.getInetAddress().getHostAddress());
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
				AppLogger.getInstance().consolePrint(PRE +"GETRNPUB"+" at "+socket.getInetAddress().getHostAddress());
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
				AppLogger.getInstance().consolePrint(PRE +"REVOKEPUB"+" at "+socket.getInetAddress().getHostAddress());
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
		int port = Bluenode.getInstance().trackerPort;
		InetAddress addr = TrackerClient.trackerAddress.asInet();

		Socket socket = null;
		try {
			socket = SocketUtilities.absoluteConnect(addr, port);
			
			DataInputStream reader = SocketUtilities.makeDataReader(socket);
			DataOutputStream writer = SocketUtilities.makeDataWriter(socket);

			AppLogger.getInstance().consolePrint("Tracker "+"GETPUB"+" at "+socket.getInetAddress().getHostAddress());
			String[] args = SocketUtilities.sendReceivePlainStringData("GETPUB", reader, writer);
			     
			PublicKey trackerPublic = CryptoUtilities.base64StringRepresentationToObject(args[0]);
			Bluenode.getInstance().updateTrackerPublicKey(trackerPublic);
	        
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
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
		String name = Bluenode.getInstance().name;
		PublicKey trackerPublic = Bluenode.getInstance().trackerPublicKey;
		
		if (trackerPublic == null) {
			System.err.println(pre+"no tracker public key was set.");
			return null;
		}
		
		int port = Bluenode.getInstance().trackerPort;
		InetAddress addr = TrackerClient.trackerAddress.asInet();

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
			SocketUtilities.sendRSAEncryptedStringData(keyStr, writer, Bluenode.getInstance().trackerPublicKey);
			
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
			
			PublicKey pub = Bluenode.getInstance().bluenodeKeys.getPublic();
			AppLogger.getInstance().consolePrint(pre+"OFFERPUB"+" at "+socket.getInetAddress().getHostAddress());
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
