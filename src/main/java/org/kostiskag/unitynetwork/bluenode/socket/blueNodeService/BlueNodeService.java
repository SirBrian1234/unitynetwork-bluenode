package org.kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import org.kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeService extends Thread {

    private final String pre = "^BlueNodeService ";
    private final String prebn = "BlueNode ";
    private final String prern = "RedNode ";
    private final String pretr = "Tracker ";
    private final Socket sessionSocket;
    private DataInputStream socketReader;
    private DataOutputStream socketWriter;
    private SecretKey sessionKey;

    BlueNodeService(Socket sessionSocket) throws IOException {
        this.sessionSocket = sessionSocket;
    }
    
    private void close() {
    	try {
			sessionSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre +"STARTING AN AUTH AT "+Thread.currentThread().getName());
        try {
        	socketReader = SocketUtilities.makeDataReader(sessionSocket);
			socketWriter = SocketUtilities.makeDataWriter(sessionSocket);

			byte[] received = SocketUtilities.receiveData(socketReader);
			String receivedStr = new String(received, "utf-8");
			String[] args = receivedStr.split("\\s+");
			
			if (!App.bn.network && args[0].equals("GETPUB")) {
				// if this bluenode is standalone it is allowed to distribute its public
				SocketUtilities.sendPlainStringData(CryptoUtilities.objectToBase64StringRepresentation(App.bn.bluenodeKeys.getPublic()), socketWriter);
			} else {
				//client uses server's public key collected from the network to send a session key
				String decrypted = CryptoUtilities.decryptWithPrivate(received, App.bn.bluenodeKeys.getPrivate());
				sessionKey = (SecretKey) CryptoUtilities.base64StringRepresentationToObject(decrypted);
				args = SocketUtilities.sendReceiveAESEncryptedStringData("BLUENODE "+App.bn.name, socketReader, socketWriter, sessionKey);
	
				if (args.length == 2 && args[0].equals("REDNODE")) {
	                redNodeService(args[1]);
	            } else if (App.bn.network) {
	            	if (args.length == 2 && args[0].equals("BLUENODE")) {
	                    blueNodeService(args[1]);
	                } else if (args.length == 1 && args[0].equals("TRACKER")) {
	                    trackingService();
	                } else {
	                	SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey);                
	                }
	            } else {
	            	SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey);                
	            }
			}
            
        } catch (Exception e) {
			e.printStackTrace();
		}
    	close();
	}

    private void redNodeService(String hostname) {
    	String[] args;
    	try {
	    	if (App.bn.network) {
	       		//when bluenode is in network mode, it offers an auth question based on rn's pub key and then is verified
	       		//with usrer credentials
	       		
	       		//first collect rn's public from tracker
				TrackerClient tr = new TrackerClient();
				PublicKey rnPub = tr.getRedNodesPubKey(hostname);
				
		    	// generate a random question
		    	String question = CryptoUtilities.generateQuestion();
		
		    	// encrypt question with target's public
		    	byte[] questionb = CryptoUtilities.encryptWithPublic(question, rnPub);
		
		    	// encode it to base 64
		    	String encq = CryptoUtilities.bytesToBase64String(questionb);
		
		    	// send it, wait for response
		    	args = SocketUtilities.sendReceiveAESEncryptedStringData(encq, socketReader, socketWriter, sessionKey);
		    	
		    	if (args[0].equals(question)) {
					// now this is a proper RSA authentication
					SocketUtilities.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
				} else {
					SocketUtilities.sendAESEncryptedStringData("NOT_ALLOWED", socketWriter, sessionKey);
					throw new Exception("RSA auth for Tracker in "+sessionSocket.getInetAddress().getHostAddress()+" has failed.");
				}
	    	} else {
	    		//there is AES connection started from bn's public and the red node is later verified with user credentials
	    		SocketUtilities.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
			}
			
			args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
			//options
	        if (args.length == 3 && args[0].equals("LEASE")) {
	        	App.bn.ConsolePrint(pre+prern+"LEASE"+" from "+sessionSocket.getInetAddress().getHostAddress());
	            RedNodeFunctions.lease(hostname, args[1], args[2], sessionSocket, socketReader, socketWriter, sessionKey);
	        } else {
	        	App.bn.ConsolePrint(pre+prern+"WRONG_COMMAND "+args[0]+" from "+sessionSocket.getInetAddress().getHostAddress());
	        	SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey); 
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void blueNodeService(String blueNodeName) {
    	try {
    		//collect bn's public either from tracker of from table
    		PublicKey bnPub;
    		boolean associated = false;
    		if (App.bn.blueNodesTable.checkBlueNode(blueNodeName)) {
    			associated = true;
    			bnPub = App.bn.blueNodesTable.getBlueNodeInstanceByName(blueNodeName).getPub();
    		} else {
    			TrackerClient tr = new TrackerClient();
    			bnPub = tr.getBlueNodesPubKey(blueNodeName);
    		}
    		
    		if (bnPub == null) {
    			throw new Exception("BlueNode's key could not be retrieved from online table or from tracker.");
    		}
    		
        	// generate a random question
	    	String question = CryptoUtilities.generateQuestion();
	
	    	// encrypt question with target's public
	    	byte[] questionb = CryptoUtilities.encryptWithPublic(question, bnPub);
	
	    	// encode it to base 64
	    	String encq = CryptoUtilities.bytesToBase64String(questionb);
	
	    	// send it, wait for response
	    	String args[] = SocketUtilities.sendReceiveAESEncryptedStringData(encq, socketReader, socketWriter, sessionKey);
	    	
	    	if (args[0].equals(question)) {
				// now this is a proper RSA authentication
				SocketUtilities.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
			} else {
				SocketUtilities.sendAESEncryptedStringData("NOT_ALLOWED", socketWriter, sessionKey);
				throw new Exception("RSA auth for Tracker in "+sessionSocket.getInetAddress().getHostAddress()+" has failed.");
			}
    	
    		args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
    		//options
            if (args.length == 1 && args[0].equals("CHECK")) {
            	App.bn.ConsolePrint(pre+prebn+"CHECK"+" from bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
            	BlueNodeFunctions.check(blueNodeName,socketWriter, sessionKey);
            } else if (args.length == 1 && args[0].equals("ASSOCIATE")) {
            	App.bn.ConsolePrint(pre+prebn+"ASSOCIATE"+" from bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
                BlueNodeFunctions.associate(blueNodeName, bnPub, sessionSocket,socketReader,socketWriter, sessionKey);
            } else if (associated) {            	
            	//these options are only for leased bns
            	BlueNodeInstance bn = App.bn.blueNodesTable.getBlueNodeInstanceByName(blueNodeName);
				if (args.length == 1 && args[0].equals("UPING")) {
					App.bn.ConsolePrint(pre+prebn+"UPING"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.Uping(bn, socketWriter, sessionKey);
	            } else if (args.length == 1 && args[0].equals("DPING")) {
	            	App.bn.ConsolePrint(pre+prebn+"DPING"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.Dping(bn);
	            } else if (args.length == 1 && args[0].equals("RELEASE")) {
	            	App.bn.ConsolePrint(pre+prebn+"RELEASE"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.releaseBn(blueNodeName);
	            } else if (args.length == 1 && args[0].equals("GET_RED_NODES")) {
	            	App.bn.ConsolePrint(pre+prebn+"GET_RED_NODES"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.giveLRNs(socketWriter, sessionKey);
	            } else if (args.length == 1 && args[0].equals("GIVE_RED_NODES")) {
	            	App.bn.ConsolePrint(pre+prebn+"GIVE_RED_NODES"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getLRNs(bn, socketReader, sessionKey);
	            } else if (args.length == 1 && args[0].equals("EXCHANGE_RED_NODES")) {
	            	App.bn.ConsolePrint(pre+prebn+"EXCHANGE_RED_NODES"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.exchangeRNs(bn, socketReader, socketWriter, sessionKey);
	            } else if (args.length == 2 && args[0].equals("GET_RED_HOSTNAME")) {
	            	App.bn.ConsolePrint(pre+prebn+"GET_RED_HOSTNAME"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getLocalRnHostnameByVaddress(args[1], socketWriter, sessionKey);
	            } else if (args.length == 2 && args[0].equals("GET_RED_VADDRESS")) {
	            	App.bn.ConsolePrint(pre+prebn+"GET_RED_VADDRESS"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getLocalRnVaddressByHostname(args[1], socketWriter, sessionKey);
	            } else if (args.length == 2 && args[0].equals("RELEASE_REMOTE_REDNODE_BY_HN")) {
	            	App.bn.ConsolePrint(pre+prebn+"RELEASE_REMOTE_REDNODE_BY_HN"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getRRNToBeReleasedByHn(bn, args[1], socketWriter, sessionKey);
	            } else if (args.length == 2 && args[0].equals("RELEASE_REMOTE_REDNODE_BY_VADDRESS")) {
	            	App.bn.ConsolePrint(pre+prebn+"RELEASE_REMOTE_REDNODE_BY_VADDRESS"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getRRNToBeReleasedByVaddr(bn, args[1], socketWriter, sessionKey);
	            } else if (args.length == 3 && args[0].equals("LEASE_REMOTE_REDNODE")) {
	            	App.bn.ConsolePrint(pre+prebn+"LEASE_REMOTE_REDNODE"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getFeedReturnRoute(bn, args[1], args[2], socketWriter, sessionKey);
	            } else {
	            	App.bn.ConsolePrint(pre+prebn+"WRONG_COMMAND "+args[0]+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	            	SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey); 
	            }
            } else {            
            	App.bn.ConsolePrint(pre+prebn+"WRONG_COMMAND "+args[0]+" from bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
            	SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey);           
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }

    private void trackingService() {
    	try {
	    	// generate a random question
	    	String question = CryptoUtilities.generateQuestion();
	
	    	// encrypt question with target's public
	    	byte[] questionb = CryptoUtilities.encryptWithPublic(question, App.bn.trackerPublicKey);
	
	    	// encode it to base 64
	    	String encq = CryptoUtilities.bytesToBase64String(questionb);
	
	    	// send it, wait for response
	    	String args[] = SocketUtilities.sendReceiveAESEncryptedStringData(encq, socketReader, socketWriter, sessionKey);
	    	

	    	if (args[0].equals(question)) {
				// now this is a proper RSA authentication
				SocketUtilities.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
			} else {
				SocketUtilities.sendAESEncryptedStringData("NOT_ALLOWED", socketWriter, sessionKey);
				throw new Exception("RSA auth for Tracker in "+sessionSocket.getInetAddress().getHostAddress()+" has failed.");
			}
    	
    		args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
    		//options
            if (args.length == 1 && args[0].equals("CHECK")) {
            	App.bn.ConsolePrint(pre+pretr+"CHECK"+" from "+sessionSocket.getInetAddress().getHostAddress());
                TrackingFunctions.check(socketWriter, sessionKey);
            } else if (args.length == 1 && args[0].equals("GETREDNODES")) {
            	App.bn.ConsolePrint(pre+pretr+"GETREDNODES"+" from "+sessionSocket.getInetAddress().getHostAddress());
                TrackingFunctions.getrns(socketWriter, sessionKey);
            } else if (args.length == 1 && args[0].equals("KILLSIG")) {
            	App.bn.ConsolePrint(pre+pretr+"KILLSIG"+" from "+sessionSocket.getInetAddress().getHostAddress());
                TrackingFunctions.killsig(socketWriter, sessionKey);
            } else {
            	App.bn.ConsolePrint(pre+pretr+"WRONG_COMMAND: "+args[0]+" from "+sessionSocket.getInetAddress().getHostAddress());
            	SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey);  
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
}
