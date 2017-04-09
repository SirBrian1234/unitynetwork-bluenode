package kostiskag.unitynetwork.bluenode.BlueNodeService;

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

    private String pre = "^SERVER ";
    private Socket connectionSocket;
    private BufferedReader inFromClient;
    private PrintWriter outputWriter;

    BlueNodeService(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "STARTING AN AUTH AT " + Thread.currentThread().getName());
        try {
            String[] args;
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outputWriter = new PrintWriter(connectionSocket.getOutputStream(), true);
            outputWriter.println("BLUENODE " + App.bn.name + " ");

            String clientSentence = inFromClient.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            args = clientSentence.split("\\s+");

            if (args.length == 2 && args[0].equals("BLUENODE")) {
                BlueNodeService(args[1]);
            } else if (args.length == 2 && args[0].equals("REDNODE")) {
                RedNodeService(args[1]);
            } else if (args.length == 1 && args[0].equals("TRACKER")) {
                TrackingService();
            } else {
                outputWriter.println("WRONG_COMMAND");
                connectionSocket.close();
            }
            //do not put a socket.close down here because some objects save the socket instance
        } catch (IOException ex) {
            Logger.getLogger(BlueNodeService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void BlueNodeService(String hostname) {
        try {
            outputWriter.println("OK ");
            String clientSentence = inFromClient.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            String[] args = clientSentence.split("\\s+");

            if (args.length == 1 && args[0].equals("ASSOCIATE")) {
                BlueNodeFunctions.Associate(hostname,connectionSocket,inFromClient,outputWriter);
            } else if (args.length == 1 && args[0].equals("FULL_ASSOCIATE")) {
                BlueNodeFunctions.FullAssociate(hostname,connectionSocket,inFromClient,outputWriter);
            } else if (args.length == 2 && args[0].equals("GET_RED_HOSTNAME")) {
                BlueNodeFunctions.GetRnHostname(hostname, args[1], connectionSocket,inFromClient,outputWriter);
            } else if (args.length == 2 && args[0].equals("CHECK")) {
                BlueNodeFunctions.Check(hostname, args[1], connectionSocket,inFromClient,outputWriter);
            } else if (args.length == 1 && args[0].equals("RELEASE")) {
                BlueNodeFunctions.Release(hostname,connectionSocket,inFromClient,outputWriter);
            } else if (args.length == 1 && args[0].equals("UPING")) {
                BlueNodeFunctions.Uping(hostname,connectionSocket,inFromClient,outputWriter);
            } else if (args.length == 1 && args[0].equals("DPING")) {
                BlueNodeFunctions.Dping(hostname,connectionSocket,inFromClient,outputWriter);
            } else if (args.length == 1 && args[0].equals("GET_RED_NODES")) {
                BlueNodeFunctions.GetRNs(hostname,connectionSocket,inFromClient,outputWriter);
            } else if (args.length == 1 && args[0].equals("EXCHANGE_RED_NODES")) {
                BlueNodeFunctions.ExchangeRNs(hostname,connectionSocket,inFromClient,outputWriter);
            } else if (args.length == 2 && args[0].equals("FEED_RETURN_ROUTE")) {
                BlueNodeFunctions.FeedReturnRoute(hostname, args[1], connectionSocket,inFromClient,outputWriter);
            }
            connectionSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(BlueNodeService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void RedNodeService(String hostname) {
        try {
            outputWriter.println("OK");
            String clientSentence = inFromClient.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            String[] args = clientSentence.split("\\s+");

            if (args.length == 3 && args[0].equals("LEASE")) {
                RedNodeFunctions.Lease(connectionSocket, hostname, args[1], args[2]);
            }            
            //no need for connection socket close - statefull
        } catch (IOException ex) {
            Logger.getLogger(BlueNodeService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void TrackingService() {
        try {
            outputWriter.println("OK");
            String clientSentence = inFromClient.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            String[] args = clientSentence.split("\\s+");

            if (args.length == 1 && args[0].equals("CHECK")) {
                TrackingFunctions.check(outputWriter);
            } else if (args.length == 1 && args[0].equals("GETREDNODES")) {
                TrackingFunctions.getrns(outputWriter);
            }
            connectionSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(BlueNodeService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
