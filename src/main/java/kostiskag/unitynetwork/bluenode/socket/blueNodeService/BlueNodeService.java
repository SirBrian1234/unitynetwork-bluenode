package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

/**
 *
 * @author kostis
 */
public class BlueNodeService extends Thread {

    private final String pre = "^SERVER ";
    private final Socket sessionSocket;
    private final BufferedReader socketReader;
    private final PrintWriter socketWriter;

    BlueNodeService(Socket sessionSocket) throws IOException {
        this.sessionSocket = sessionSocket;
        socketReader = new BufferedReader(new InputStreamReader(sessionSocket.getInputStream()));
        socketWriter = new PrintWriter(sessionSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre +"STARTING AN AUTH AT "+Thread.currentThread().getName());
        try {
            String[] args;
            
            socketWriter.println("BLUENODE "+App.bn.name+" ");

            String clientSentence = socketReader.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            args = clientSentence.split("\\s+");

            if (args.length == 2 && args[0].equals("BLUENODE")) {
                blueNodeService(args[1]);
            } else if (args.length == 2 && args[0].equals("REDNODE")) {
                redNodeService(args[1]);
            } else if (args.length == 1 && args[0].equals("TRACKER")) {
                trackingService();
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
    }

    private void blueNodeService(String blueNodeName) {
        try {
            socketWriter.println("OK ");
            String clientSentence = socketReader.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            String[] args = clientSentence.split("\\s+");

            if (args.length == 1 && args[0].equals("CHECK")) {
            	socketWriter.println("OK");
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
		            } else if (args.length == 1 && args[0].equals("EXCHANGE_RED_NODES")) {
		                BlueNodeFunctions.exchangeRNs(bn, socketReader, socketWriter);
		            } else if (args.length == 2 && args[0].equals("GET_RED_HOSTNAME")) {
		                BlueNodeFunctions.getLocalRnHostnameByVaddress(args[1], socketWriter);
		            } else if (args.length == 2 && args[0].equals("GET_RED_VADDRESS")) {
		                BlueNodeFunctions.getLocalRnVaddressByHostname(args[1], socketWriter);
		            } else if (args.length == 2 && args[0].equals("RELEASE_REMOTE_REDNODE_BY_HN")) {
		                BlueNodeFunctions.getRRNToBeReleasedByHn(bn, args[0], socketWriter);
		            } else if (args.length == 2 && args[0].equals("RELEASE_REMOTE_REDNODE_BY_VADDRESS")) {
		                BlueNodeFunctions.getRRNToBeReleasedByVaddr(bn, args[0], socketWriter);
		            } else if (args.length == 3 && args[0].equals("LEASE_REMOTE_REDNODE")) {
		                BlueNodeFunctions.getFeedReturnRoute(bn, args[0], args[1], socketWriter);
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
    }

    private void redNodeService(String hostname) {
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
    }

    private void trackingService() {
        try {
            socketWriter.println("OK");
            String clientSentence = socketReader.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            String[] args = clientSentence.split("\\s+");

            if (args.length == 1 && args[0].equals("CHECK")) {
                TrackingFunctions.check(socketWriter);
            } else if (args.length == 1 && args[0].equals("GETREDNODES")) {
                TrackingFunctions.getrns(socketWriter);
            } else if (args.length == 1 && args[0].equals("KILLSIG")) {
                TrackingFunctions.killsig(socketWriter);
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
    }
}
