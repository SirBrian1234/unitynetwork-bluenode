package org.kostiskag.unitynetwork.bluenode.service.trackclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.serviceoperations.BlueNodeToTracker;
import org.kostiskag.unitynetwork.common.state.PublicKeyState;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.AppLogger;


/**
 * 
 * @author Konstantinos Kagiampakis
 */
public final class TrackerClient {

	private static final String PRE = "^TrackerClient ";
	private static String bluenodeName;
	private static KeyPair bluenodeKeys;
	private static PublicKey trackerPublic;
	private static PhysicalAddress trackerAddress;
	private static int trackerPort;

	private Socket socket;
	private SecretKey sessionKey;
	private DataInputStream reader;
	private DataOutputStream writer;
	private String reason;
	private boolean connected = false;

	public static void configureTracker(String bluenodeName, KeyPair bluenodeKeys, PublicKey trackerPublic, PhysicalAddress trackerAddress, int trackerPort) {
		if (bluenodeName == null || bluenodeKeys==null || trackerPublic == null || trackerAddress == null || (trackerPort <=0 && trackerPort > NumericConstraints.MAX_ALLOWED_PORT_NUM.size())) {
			throw new IllegalArgumentException(PRE + " invalid configuration data were given!");
		}

		TrackerClient.bluenodeName = bluenodeName;
		TrackerClient.bluenodeKeys = bluenodeKeys;
		TrackerClient.trackerPublic = trackerPublic;
		TrackerClient.trackerAddress = trackerAddress;
		TrackerClient.trackerPort = trackerPort;
	}

	public TrackerClient() {
		//sanitize
		if (bluenodeName == null || bluenodeKeys == null || trackerPublic == null || trackerAddress == null || (trackerPort <=0 && trackerPort > NumericConstraints.MAX_ALLOWED_PORT_NUM.size())) {
			throw new IllegalArgumentException(PRE + " invalid configuration data were given!");
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
			SocketUtilities.sendRSAEncryptedStringData(keyStr, writer, TrackerClient.trackerPublic);
			
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
			String answer = CryptoUtilities.decryptWithPrivate(question, TrackerClient.bluenodeKeys.getPrivate());
			
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
				AppLogger.getInstance().consolePrint(PRE +BlueNodeToTracker.LEASE.value()+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData(BlueNodeToTracker.LEASE.value()+" "+authport, reader, writer, sessionKey);
				closeCon();
				if (args[0].equals(BlueNodeToTracker.LEASE_SUCCESS_RESPONSE.value())) {
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
				AppLogger.getInstance().consolePrint(PRE +BlueNodeToTracker.RELEASE.value()+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData(BlueNodeToTracker.RELEASE.value(), reader, writer, sessionKey);
				closeCon();
				if (args[0].equals(BlueNodeToTracker.RELEASE_SUCCESS_RESPONSE.value())) {
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
				AppLogger.getInstance().consolePrint(PRE +BlueNodeToTracker.GETPH.value()+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData(BlueNodeToTracker.GETPH.value()+" "+BNHostname, reader, writer, sessionKey);
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
				AppLogger.getInstance().consolePrint(PRE +BlueNodeToTracker.LEASE_RN.value()+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData(BlueNodeToTracker.LEASE_RN.value()+" "+Hostname+" "+Username+" "+Password, reader, writer, sessionKey);
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
				AppLogger.getInstance().consolePrint(PRE +BlueNodeToTracker.RELEASE_RN.value()+" at "+socket.getInetAddress().getHostAddress());
				SocketUtilities.sendAESEncryptedStringData(BlueNodeToTracker.RELEASE_RN.value()+" "+hostname, writer, sessionKey);
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
				AppLogger.getInstance().consolePrint(PRE +BlueNodeToTracker.CHECK_RN.value()+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData(BlueNodeToTracker.CHECK_RN.value()+" "+hostanme, reader, writer, sessionKey);
				closeCon();
		        if (args[0].equals(BlueNodeToTracker.CHECK_RN_FAIL_RESPONSE.value())) {
		            return null;
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
				AppLogger.getInstance().consolePrint(PRE +BlueNodeToTracker.CHECK_RNA.value()+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData(BlueNodeToTracker.CHECK_RNA.value()+" "+vaddress, reader, writer, sessionKey);
				closeCon();
				if (args[0].equals(BlueNodeToTracker.CHECK_RNA_FAIL_RESPONSE.value())) {
		            return null;
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
    
    public PublicKey getBlueNodesPubKey(String name) {            
    	if (connected) {
	        String[] args = null;
			try {
				AppLogger.getInstance().consolePrint(PRE +BlueNodeToTracker.GETBNPUB.value()+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData(BlueNodeToTracker.GETBNPUB.value()+" "+name, reader, writer, sessionKey);
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
				AppLogger.getInstance().consolePrint(PRE +BlueNodeToTracker.GETRNPUB.value()+" at "+socket.getInetAddress().getHostAddress());
				args = SocketUtilities.sendReceiveAESEncryptedStringData(BlueNodeToTracker.GETRNPUB.value()+" "+hostname, reader, writer, sessionKey);
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
     * it is used from Bluenode in order to store the public key
     * to use.
     */
	public static PublicKey getTrackersPublicKey(InetAddress trackerAddress, int trackerPort) {
		//sanitize
		if (trackerAddress == null || trackerPort <= 0 || trackerPort > NumericConstraints.MAX_ALLOWED_PORT_NUM.size() ) {
			throw  new IllegalArgumentException("malformed input data were given");
		}

		try (Socket socket = SocketUtilities.absoluteConnect(trackerAddress, trackerPort);
			 DataInputStream reader = SocketUtilities.makeDataReader(socket);
			 DataOutputStream writer = SocketUtilities.makeDataWriter(socket)) {

			AppLogger.getInstance().consolePrint("Tracker "+"GETPUB"+" at "+socket.getInetAddress().getHostAddress());
			String[] args = SocketUtilities.sendReceivePlainStringData("GETPUB", reader, writer);
			     
			return CryptoUtilities.base64StringRepresentationToObject(args[0]);
		} catch (IOException | GeneralSecurityException e) {
			AppLogger.getInstance().consolePrint(PRE + "failed to connect to tracker to fetch public ket\n"+e.getLocalizedMessage());
		}
		return null;
	}
	
	/**
	 * Offer public key needs an rsa connection based on tracker's public
	 * but no authentication for the bluenode's part
	 *  
	 * @param ticket
	 * @return
	 */
	public static PublicKeyState offerPubKey(String bluenodeName, String ticket, PublicKey trackerPublic, InetAddress trackerAddress, int trackerPort) {
		//sanitize
		if (bluenodeName == null || bluenodeName.isEmpty() || ticket == null || ticket.isEmpty() || trackerPublic == null || trackerAddress == null || trackerPort <= 0 || trackerPort > NumericConstraints.MAX_ALLOWED_PORT_NUM.size()) {
			throw  new IllegalArgumentException("malformed input data were given");
		}

		String pre = "^offerPubKey ";
		try(Socket socket = SocketUtilities.absoluteConnect(trackerAddress, trackerPort);
			DataInputStream reader = SocketUtilities.makeDataReader(socket);
			DataOutputStream writer = SocketUtilities.makeDataWriter(socket)) {

			SecretKey sessionKey = CryptoUtilities.generateAESSessionkey();
			if (sessionKey == null) {
				throw new GeneralSecurityException();
			}
			
			String keyStr = CryptoUtilities.objectToBase64StringRepresentation(sessionKey);
			SocketUtilities.sendRSAEncryptedStringData(keyStr, writer, trackerPublic);
			
			String[] args = SocketUtilities.receiveAESEncryptedStringData(reader, sessionKey);
			System.out.println(args[0]);
			
			if(!args[0].equals("UnityTracker")) {
				throw new IOException("Wrong header greeting.");
			}
			
			args = SocketUtilities.sendReceiveAESEncryptedStringData("BLUENODE"+" "+bluenodeName, reader, writer, sessionKey);
		
			if (!args[0].equals("PUBLIC_NOT_SET")) {
				SocketUtilities.sendAESEncryptedStringData("EXIT", writer, sessionKey);
				return PublicKeyState.KEY_IS_SET;
			}
			
			PublicKey pub = TrackerClient.bluenodeKeys.getPublic();
			AppLogger.getInstance().consolePrint(pre+"OFFERPUB"+" at "+socket.getInetAddress().getHostAddress());
	    	args = SocketUtilities.sendReceiveAESEncryptedStringData("OFFERPUB"+" "+ticket+" "+CryptoUtilities.objectToBase64StringRepresentation(pub), reader, writer, sessionKey);

	    	return PublicKeyState.valueOf(args[0]);
	    	
		} catch (GeneralSecurityException | IOException e) {
			AppLogger.getInstance().consolePrint(pre+"OFFERPUB"+" failed with system error " + e.getLocalizedMessage());
			return PublicKeyState.SYSTEM_ERROR;
		}
	}
}
