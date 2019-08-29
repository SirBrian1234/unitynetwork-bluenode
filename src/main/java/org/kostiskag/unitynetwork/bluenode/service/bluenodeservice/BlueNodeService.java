package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.serviceoperations.TrackerToBlueNode;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.rundata.table.AccountTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.ModeOfOperation;
import org.kostiskag.unitynetwork.bluenode.AppLogger;


/**
 *
 * @author Konstantinos Kagiampakis
 */
final class BlueNodeService extends Thread {

    private static final String PRE = "^BlueNodeService ";

    private final String prebn = "BlueNode ";
    private final String prern = "RedNode ";
    private final String pretr = "Tracker ";

    private final String localBluenodeName;
	private final AccountTable accountTable;
	private final LocalRedNodeTable rednodeTable;
	private final BlueNodeTable bluenodeTable;
	private final TrackerTimeBuilder trackerTimeBuilder;
	private final KeyPair bluenodeKeyPair;
	private final PublicKey trackerPublic;
	private final Socket sessionSocket;
    private final ModeOfOperation mode;
    private final Runnable blunodeTerminate;

    private DataInputStream socketReader;
    private DataOutputStream socketWriter;
    private SecretKey sessionKey;

	BlueNodeService(String localBluenodeName, LocalRedNodeTable rednodeTable, BlueNodeTable bluenodeTable, TrackerTimeBuilder trackerTimeBuilder, KeyPair bluenodeKeyPair, PublicKey trackerPublic, Socket sessionSocket, Runnable blunodeTerminate) {
		if (localBluenodeName == null || rednodeTable == null || bluenodeTable == null || bluenodeKeyPair == null || trackerPublic == null || sessionSocket == null || trackerTimeBuilder == null || blunodeTerminate == null) {
			throw new IllegalArgumentException(PRE + " invalid configuration data were given!");
		}

		this.accountTable = null;

		this.localBluenodeName = localBluenodeName;
		this.rednodeTable = rednodeTable;
		this.bluenodeTable = bluenodeTable;
		this.trackerTimeBuilder = trackerTimeBuilder;
		this.bluenodeKeyPair = bluenodeKeyPair;
		this.trackerPublic = trackerPublic;
		this.blunodeTerminate = blunodeTerminate;
        this.sessionSocket = sessionSocket;
        this.mode = ModeOfOperation.NETWORK;
    }

	//Local Plain mode
	BlueNodeService(String localBluenodeName, AccountTable accountTable, LocalRedNodeTable rednodeTable, Socket sessionSocket) {
		if (localBluenodeName == null || rednodeTable == null || sessionSocket == null) {
			throw new IllegalArgumentException(PRE + " invalid configuration data were given!");
		}

		this.bluenodeTable = null;
		this.bluenodeKeyPair = null;
		this.trackerPublic = null;
		this.trackerTimeBuilder = null;
		this.blunodeTerminate = null;

		this.localBluenodeName = localBluenodeName;
		this.accountTable = accountTable;
		this.rednodeTable = rednodeTable;
		this.sessionSocket = sessionSocket;
		this.mode = ModeOfOperation.LIST;
	}

    //Local Plain mode
	BlueNodeService(String localBluenodeName, LocalRedNodeTable rednodeTable, Socket sessionSocket) {
		if (localBluenodeName == null || rednodeTable == null || sessionSocket == null) {
			throw new IllegalArgumentException(PRE + " invalid configuration data were given!");
		}

		this.accountTable = null;
		this.bluenodeTable = null;
		this.bluenodeKeyPair = null;
		this.trackerPublic = null;
		this.trackerTimeBuilder = null;
		this.blunodeTerminate = null;

		this.localBluenodeName = localBluenodeName;
		this.rednodeTable = rednodeTable;
		this.sessionSocket = sessionSocket;
		this.mode = ModeOfOperation.PLAIN;
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
		AppLogger.getInstance().consolePrint(PRE +"STARTING AN AUTH AT "+Thread.currentThread().getName());
        try {
        	socketReader = SocketUtilities.makeDataReader(sessionSocket);
			socketWriter = SocketUtilities.makeDataWriter(sessionSocket);

			byte[] received = SocketUtilities.receiveData(socketReader);
			String receivedStr = new String(received, "utf-8");
			String[] args = receivedStr.split("\\s+");
			
			if (mode != ModeOfOperation.NETWORK && args[0].equals("GETPUB")) {
				// if this bluenode is standalone it is allowed to distribute its public
				SocketUtilities.sendPlainStringData(CryptoUtilities.objectToBase64StringRepresentation(bluenodeKeyPair.getPublic()), socketWriter);
			} else {
				//client uses server's public key collected from the network to send a session key
				String decrypted = CryptoUtilities.decryptWithPrivate(received, bluenodeKeyPair.getPrivate());
				sessionKey = CryptoUtilities.base64StringRepresentationToObject(decrypted);
				args = SocketUtilities.sendReceiveAESEncryptedStringData("BLUENODE "+localBluenodeName, socketReader, socketWriter, sessionKey);
	
				if (args.length == 2 && args[0].equals("REDNODE")) {
	                redNodeService(args[1]);
	            } else if (mode == ModeOfOperation.NETWORK) {
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
	    	if (mode == ModeOfOperation.NETWORK) {
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
				AppLogger.getInstance().consolePrint(PRE +prern+"LEASE"+" from "+sessionSocket.getInetAddress().getHostAddress());
	            RedNodeFunctions.lease(mode, accountTable, rednodeTable, bluenodeTable, hostname, args[1], args[2], sessionSocket, socketReader, socketWriter, sessionKey);
	        } else {
				AppLogger.getInstance().consolePrint(PRE +prern+"WRONG_COMMAND "+args[0]+" from "+sessionSocket.getInetAddress().getHostAddress());
	        	SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey); 
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void blueNodeService(String blueNodeName) {
    	Lock lock = null;
		try {
			lock = bluenodeTable.aquireLock(); //we are going to need a lock for all the process of this service
			BlueNode bn = null;
			PublicKey bnPub = null;
			boolean associated = false;

			//collect bn's public either from tracker of from table
			var bno = bluenodeTable.getOptionalEntry(lock, blueNodeName);
			if (bno.isPresent()) {
				associated = true;
				bn = bno.get();
				bnPub = bn.getPub();
			} else {
				TrackerClient tr = new TrackerClient();
				bnPub = tr.getBlueNodesPubKey(blueNodeName);
			}

    		if (bnPub == null) {
    			throw new GeneralSecurityException("BlueNode's key could not be retrieved from online table or from tracker.");
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
				throw new IOException("RSA auth for Tracker in "+sessionSocket.getInetAddress().getHostAddress()+" has failed.");
			}
    	
    		args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
    		//options
            if (args.length == 1 && args[0].equals("CHECK")) {
				AppLogger.getInstance().consolePrint(PRE +prebn+"CHECK"+" from bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
            	BlueNodeFunctions.check(lock, bluenodeTable, blueNodeName,socketWriter, sessionKey);
            } else if (args.length == 1 && args[0].equals("ASSOCIATE")) {
				AppLogger.getInstance().consolePrint(PRE +prebn+"ASSOCIATE"+" from bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
                BlueNodeFunctions.associate(lock, bluenodeTable, localBluenodeName, blueNodeName, bnPub, sessionSocket,socketReader,socketWriter, sessionKey);
            } else if (associated) {            	
            	//these options are only for leased bns
            	if (args.length == 1 && args[0].equals("UPING")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"UPING"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.uPing(bn, socketWriter, sessionKey);
	            } else if (args.length == 1 && args[0].equals("DPING")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"DPING"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.dPing(bn);
	            } else if (args.length == 1 && args[0].equals("RELEASE")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"RELEASE"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.releaseBn(lock, bluenodeTable, blueNodeName);
	            } else if (args.length == 1 && args[0].equals("GET_RED_NODES")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"GET_RED_NODES"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.giveLRNs(rednodeTable, socketWriter, sessionKey);
	            } else if (args.length == 1 && args[0].equals("GIVE_RED_NODES")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"GIVE_RED_NODES"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getLRNs(bn, socketReader, sessionKey);
	            } else if (args.length == 1 && args[0].equals("EXCHANGE_RED_NODES")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"EXCHANGE_RED_NODES"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.exchangeRNs(rednodeTable, bn, socketReader, socketWriter, sessionKey);
	            } else if (args.length == 2 && args[0].equals("GET_RED_HOSTNAME")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"GET_RED_HOSTNAME"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getLocalRnHostnameByVaddress(rednodeTable, args[1], socketWriter, sessionKey);
	            } else if (args.length == 2 && args[0].equals("GET_RED_VADDRESS")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"GET_RED_VADDRESS"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getLocalRnVaddressByHostname(rednodeTable, args[1], socketWriter, sessionKey);
	            } else if (args.length == 2 && args[0].equals("RELEASE_REMOTE_REDNODE_BY_HN")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"RELEASE_REMOTE_REDNODE_BY_HN"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getRRNToBeReleasedByHn(bn, args[1], socketWriter, sessionKey);
	            } else if (args.length == 2 && args[0].equals("RELEASE_REMOTE_REDNODE_BY_VADDRESS")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"RELEASE_REMOTE_REDNODE_BY_VADDRESS"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getRRNToBeReleasedByVaddr(bn, args[1], socketWriter, sessionKey);
	            } else if (args.length == 3 && args[0].equals("LEASE_REMOTE_REDNODE")) {
					AppLogger.getInstance().consolePrint(PRE +prebn+"LEASE_REMOTE_REDNODE"+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	                BlueNodeFunctions.getFeedReturnRoute(bn, args[1], args[2], socketWriter, sessionKey);
	            } else {
					AppLogger.getInstance().consolePrint(PRE +prebn+"WRONG_COMMAND "+args[0]+" from associated bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
	            	SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey); 
	            }
            } else {
				AppLogger.getInstance().consolePrint(PRE +prebn+"WRONG_COMMAND "+args[0]+" from bn "+blueNodeName+" at "+sessionSocket.getInetAddress().getHostAddress());
            	SocketUtilities.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey);           
            }
        } catch (GeneralSecurityException | IOException | InterruptedException | IllegalAccessException e) {
        	AppLogger.getInstance().consolePrint(e.getMessage());
        }  finally {
			lock.unlock();
		}
	}

    private void trackingService() {
    	try {
	    	// generate a random question
	    	String question = CryptoUtilities.generateQuestion();
	
	    	// encrypt question with target's public
	    	byte[] questionb = CryptoUtilities.encryptWithPublic(question, trackerPublic);
	
	    	// encode it to base 64
	    	String encq = CryptoUtilities.bytesToBase64String(questionb);
	
	    	// send it, wait for response
	    	String args[] = SocketUtilities.sendReceiveAESEncryptedStringData(encq, socketReader, socketWriter, sessionKey);
	    	

	    	if (args[0].equals(question)) {
				// now this is a proper RSA authentication
				SocketUtilities.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
			} else {
				SocketUtilities.sendAESEncryptedStringData("NOT_ALLOWED", socketWriter, sessionKey);
				throw new IOException("RSA auth for Tracker in "+sessionSocket.getInetAddress().getHostAddress()+" has failed.");
			}
    	
    		args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
    		//options
            if (args.length == 1 && args[0].equals(TrackerToBlueNode.CHECK_IF_ALIVE.value())) {
				AppLogger.getInstance().consolePrint(PRE +pretr+TrackerToBlueNode.CHECK_IF_ALIVE.value()+" from "+sessionSocket.getInetAddress().getHostAddress());
                TrackingFunctions.check(trackerTimeBuilder, socketWriter, sessionKey);
            } else if (args.length == 1 && args[0].equals(TrackerToBlueNode.GET_ALL_LEASED_REDNODES.value())) {
				AppLogger.getInstance().consolePrint(PRE +pretr+TrackerToBlueNode.GET_ALL_LEASED_REDNODES.value()+" from "+sessionSocket.getInetAddress().getHostAddress());
                TrackingFunctions.getRns(rednodeTable, socketWriter, sessionKey);
            } else if (args.length == 1 && args[0].equals(TrackerToBlueNode.KILLING_SIGNAL.value())) {
				AppLogger.getInstance().consolePrint(PRE +pretr+TrackerToBlueNode.KILLING_SIGNAL.value()+" from "+sessionSocket.getInetAddress().getHostAddress());
                TrackingFunctions.killSig(blunodeTerminate);
            } else {
				AppLogger.getInstance().consolePrint(PRE +pretr+TrackerToBlueNode.WRONG_OPTION.value()+": "+args[0]+" from "+sessionSocket.getInetAddress().getHostAddress());
				SocketUtilities.sendAESEncryptedStringData(TrackerToBlueNode.WRONG_OPTION.value(), socketWriter, sessionKey);
			}
        } catch (GeneralSecurityException | IOException ex) {
        	ex.printStackTrace();
        }
    }
}
