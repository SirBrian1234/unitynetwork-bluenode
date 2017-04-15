package kostiskag.unitynetwork.bluenode.blueNodeService;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;

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
                BlueNodeService(args[1]);
            } else if (args.length == 2 && args[0].equals("REDNODE")) {
                RedNodeService(args[1]);
            } else if (args.length == 1 && args[0].equals("TRACKER")) {
                TrackingService();
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

    private void BlueNodeService(String hostname) {
        try {
            socketWriter.println("OK ");
            String clientSentence = socketReader.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            String[] args = clientSentence.split("\\s+");

            if (args.length == 1 && args[0].equals("ASSOCIATE")) {
                BlueNodeFunctions.Associate(hostname,sessionSocket,socketReader,socketWriter);
            } else if (args.length == 1 && args[0].equals("FULL_ASSOCIATE")) {
                BlueNodeFunctions.FullAssociate(hostname,sessionSocket,socketReader,socketWriter);
            } else if (args.length == 2 && args[0].equals("GET_RED_HOSTNAME")) {
                BlueNodeFunctions.GetRnHostname(hostname, args[1], sessionSocket,socketReader,socketWriter);
            } else if (args.length == 2 && args[0].equals("CHECK")) {
                BlueNodeFunctions.Check(hostname, args[1], sessionSocket,socketReader,socketWriter);
            } else if (args.length == 1 && args[0].equals("RELEASE")) {
                BlueNodeFunctions.Release(hostname,sessionSocket,socketReader,socketWriter);
            } else if (args.length == 1 && args[0].equals("UPING")) {
                BlueNodeFunctions.Uping(hostname,sessionSocket,socketReader,socketWriter);
            } else if (args.length == 1 && args[0].equals("DPING")) {
                BlueNodeFunctions.Dping(hostname,sessionSocket,socketReader,socketWriter);
            } else if (args.length == 1 && args[0].equals("GET_RED_NODES")) {
                BlueNodeFunctions.GetRNs(hostname,sessionSocket,socketReader,socketWriter);
            } else if (args.length == 1 && args[0].equals("EXCHANGE_RED_NODES")) {
                BlueNodeFunctions.ExchangeRNs(hostname,sessionSocket,socketReader,socketWriter);
            } else if (args.length == 2 && args[0].equals("FEED_RETURN_ROUTE")) {
                BlueNodeFunctions.FeedReturnRoute(hostname, args[1], sessionSocket,socketReader,socketWriter);
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

    private void RedNodeService(String hostname) {
        try {
        	socketWriter.println("OK");
            String clientSentence = socketReader.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            String[] args = clientSentence.split("\\s+");

            if (args.length == 3 && args[0].equals("LEASE")) {
                RedNodeFunctions.Lease(sessionSocket, socketReader, socketWriter, hostname, args[1], args[2]);
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

    private void TrackingService() {
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
