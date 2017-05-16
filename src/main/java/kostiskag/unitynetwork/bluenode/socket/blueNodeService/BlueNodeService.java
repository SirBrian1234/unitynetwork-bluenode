package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.crypto.SecretKey;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.functions.CryptoMethods;
import kostiskag.unitynetwork.bluenode.socket.SocketFunctions;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeService extends Thread {

    private final String pre = "^SERVER ";
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
        	socketReader = SocketFunctions.makeDataReader(sessionSocket);
			socketWriter = SocketFunctions.makeDataWriter(sessionSocket);

			String[] args = SocketFunctions.receiveRSAEncryptedStringData(socketReader, App.bn.bluenodeKeys.getPrivate());

			sessionKey = (SecretKey) CryptoMethods.base64StringRepresentationToObject(args[0]);
			args = SocketFunctions.sendReceiveAESEncryptedStringData("BLUENODE "+App.bn.name, socketReader, socketWriter, sessionKey);

			App.bn.ConsolePrint(pre +args[0]);
            if (args.length == 2 && args[0].equals("BLUENODE")) {
                blueNodeService(args[1]);
            } else if (args.length == 2 && args[0].equals("REDNODE")) {
                redNodeService(args[1]);
            } else if (args.length == 1 && args[0].equals("TRACKER")) {
                trackingService();
            } else {
            	SocketFunctions.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey);                
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
    	close();
	}

    private void blueNodeService(String blueNodeName) {
    	/*
        try {
            socketWriter.println("OK ");
            String clientSentence = socketReader.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            String[] args = clientSentence.split("\\s+");

            if (args.length == 1 && args[0].equals("CHECK")) {
            	BlueNodeFunctions.check(blueNodeName,socketWriter);
            } else if (args.length == 1 && args[0].equals("ASSOCIATE")) {
                BlueNodeFunctions.associate(blueNodeName,sessionSocket,socketReader,socketWriter);
            } else if (App.bn.blueNodesTable.checkBlueNode(blueNodeName)) {            	
            	//these options are only for leased bns
            	BlueNodeInstance bn;
				try {
					bn = App.bn.blueNodesTable.getBlueNodeInstanceByName(blueNodeName);
					if (args.length == 1 && args[0].equals("UPING")) {
		                BlueNodeFunctions.Uping(bn, socketWriter);
		            } else if (args.length == 1 && args[0].equals("DPING")) {
		                BlueNodeFunctions.Dping(bn,socketWriter);
		            } else if (args.length == 1 && args[0].equals("RELEASE")) {
		                BlueNodeFunctions.releaseBn(blueNodeName,socketWriter);
		            } else if (args.length == 1 && args[0].equals("GET_RED_NODES")) {
		                BlueNodeFunctions.giveLRNs(socketWriter);
		            } else if (args.length == 1 && args[0].equals("GIVE_RED_NODES")) {
		                BlueNodeFunctions.getLRNs(bn, socketReader, socketWriter);
		            } else if (args.length == 1 && args[0].equals("EXCHANGE_RED_NODES")) {
		                BlueNodeFunctions.exchangeRNs(bn, socketReader, socketWriter);
		            } else if (args.length == 2 && args[0].equals("GET_RED_HOSTNAME")) {
		                BlueNodeFunctions.getLocalRnHostnameByVaddress(args[1], socketWriter);
		            } else if (args.length == 2 && args[0].equals("GET_RED_VADDRESS")) {
		                BlueNodeFunctions.getLocalRnVaddressByHostname(args[1], socketWriter);
		            } else if (args.length == 2 && args[0].equals("RELEASE_REMOTE_REDNODE_BY_HN")) {
		                BlueNodeFunctions.getRRNToBeReleasedByHn(bn, args[1], socketWriter);
		            } else if (args.length == 2 && args[0].equals("RELEASE_REMOTE_REDNODE_BY_VADDRESS")) {
		                BlueNodeFunctions.getRRNToBeReleasedByVaddr(bn, args[1], socketWriter);
		            } else if (args.length == 3 && args[0].equals("LEASE_REMOTE_REDNODE")) {
		                BlueNodeFunctions.getFeedReturnRoute(bn, args[1], args[2], socketWriter);
		            } else {
		            	socketWriter.println("WRONG_COMMAND");
		            }
				} catch (Exception e) {
					e.printStackTrace();
					socketWriter.println("WRONG_COMMAND");					
				}	            
            } else {            
            	socketWriter.println("WRONG_COMMAND");                
            }
            sessionSocket.close();
        } catch (IOException ex) {
        	ex.printStackTrace();
            try {
				sessionSocket.close();
			} catch (IOException e) {
				
			}
        }
        */
    }

    private void redNodeService(String hostname) {
    	/*
        try {
        	socketWriter.println("OK");
            String clientSentence = socketReader.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            String[] args = clientSentence.split("\\s+");

            if (args.length == 3 && args[0].equals("LEASE")) {
                RedNodeFunctions.lease(sessionSocket, socketReader, socketWriter, hostname, args[1], args[2]);
            } else {
            	socketWriter.println("WRONG_COMMAND"); 
            }
            sessionSocket.close();
        } catch (IOException ex) {
        	ex.printStackTrace();
            try {
				sessionSocket.close();
			} catch (IOException e) {
				
			}
        }
        */
    }

    private void trackingService() {
    	try {
	    	// generate a random question
	    	String question = CryptoMethods.generateQuestion();
	
	    	// encrypt question with target's public
	    	byte[] questionb = CryptoMethods.encryptWithPublic(question, App.bn.trackerPublicKey);
	
	    	// encode it to base 64
	    	String encq = CryptoMethods.bytesToBase64String(questionb);
	
	    	// send it, wait for response
	    	String args[] = SocketFunctions.sendReceiveAESEncryptedStringData(encq, socketReader, socketWriter, sessionKey);
	    	
	    	System.out.println("received " + args[0]);
			if (args[0].equals(question)) {
				// now this is a proper RSA authentication
				SocketFunctions.sendAESEncryptedStringData("OK", socketWriter, sessionKey);
			} else {
				SocketFunctions.sendAESEncryptedStringData("NOT_ALLOWED", socketWriter, sessionKey);
				throw new Exception("RSA auth for Tracker in "+sessionSocket.getInetAddress().getHostAddress()+" has failed.");
			}
    	
    		args = SocketFunctions.receiveAESEncryptedStringData(socketReader, sessionKey);
    		App.bn.ConsolePrint(pre +args[0]);
			//options
            if (args.length == 1 && args[0].equals("CHECK")) {
                TrackingFunctions.check(socketWriter, sessionKey);
            } else if (args.length == 1 && args[0].equals("GETREDNODES")) {
                TrackingFunctions.getrns(socketWriter, sessionKey);
            } else if (args.length == 1 && args[0].equals("KILLSIG")) {
                TrackingFunctions.killsig(socketWriter, sessionKey);
            } else {
            	SocketFunctions.sendAESEncryptedStringData("WRONG_COMMAND", socketWriter, sessionKey);  
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
}
