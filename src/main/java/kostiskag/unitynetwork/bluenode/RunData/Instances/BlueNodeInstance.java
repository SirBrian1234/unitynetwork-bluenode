package kostiskag.unitynetwork.bluenode.RunData.Instances;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.BlueThreads.BlueDownServiceClient;
import kostiskag.unitynetwork.bluenode.BlueThreads.BlueDownServiceServer;
import kostiskag.unitynetwork.bluenode.BlueThreads.BlueKeepAlive;
import kostiskag.unitynetwork.bluenode.BlueThreads.BlueUpServiceClient;
import kostiskag.unitynetwork.bluenode.BlueThreads.BlueUpServiceServer;
import kostiskag.unitynetwork.bluenode.Functions.TCPSocketFunctions;
import kostiskag.unitynetwork.bluenode.Routing.QueueManager;
import static kostiskag.unitynetwork.bluenode.BlueNodeClient.BlueNodeClientFunctions.isSameHostname;

/**
 *
 * @author kostis
 */
public class BlueNodeInstance extends Thread {

    private String pre = "^BLUE AUTH ";
    //data
    private String Hostname = null;
    private String PhaddressStr = null;
    private InetAddress Phaddress = null;    
    private boolean uping = false;
    private boolean isServer = true;
    private int state = 0;
    //threads
    private BlueDownServiceServer down;
    private BlueUpServiceServer up;
    private BlueDownServiceClient downcl;
    private BlueUpServiceClient upcl;
    private BlueKeepAlive ka;
    private QueueManager man;
    
    //get status
    /* 0 means idle
     * 1 means fully connected 
     *-1 means error 
     * the caller of this object after construction must getStatus in order to save or discard the instance
     */

    public BlueNodeInstance() {
        state = 0;
    }

    //server constructor
    public BlueNodeInstance(String hostname, boolean FullAssociation, Socket connectionSocket) {
        App.ConsolePrint(pre + "STARTING A BLUE AUTH AT " + Thread.currentThread().getName());
        this.Hostname = hostname;
        isServer = true;
        String[] args;

        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            PrintWriter outputWriter = new PrintWriter(connectionSocket.getOutputStream(), true);
            String clientSentence = null;

            if (App.BlueNodesTable.getBlueNodeInstanceByHn(hostname) != null) {
                App.BlueNodesTable.removeSingle(hostname);
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


            if (FullAssociation) {
                int size = App.localRedNodesTable.getSize();
                outputWriter.println("SENDING_LOCAL_RED_NODES " + size);
                for (int i = 0; i < size; i++) {
                    String vaddress = App.localRedNodesTable.getRedNodeInstance(i).getVaddress();
                    hostname = App.localRedNodesTable.getRedNodeInstance(i).getHostname();
                    outputWriter.println(vaddress+" "+hostname);
                }
                outputWriter.println(" ");

                clientSentence = inFromClient.readLine();
                App.ConsolePrint(pre + clientSentence);
                args = clientSentence.split("\\s+");

                int count = Integer.parseInt(args[1]);
                for (int i = 0; i < count; i++) {
                    clientSentence = inFromClient.readLine();
                    App.ConsolePrint(pre + clientSentence);
                    args = clientSentence.split("\\s+");

                    if (App.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                        App.remoteRedNodesTable.lease(args[0], args[1], hostname);
                    } else {
                        App.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                    }
                }
                clientSentence = inFromClient.readLine();
                App.ConsolePrint(pre + clientSentence);
            }

            Phaddress = connectionSocket.getInetAddress();
            PhaddressStr = Phaddress.getHostAddress();                        

            //keep alive
            ka = new BlueKeepAlive(hostname);
            //starting managers
            man = new QueueManager(20);
            //starting down
            down = new BlueDownServiceServer(hostname);
            //starting up
            up = new BlueUpServiceServer(hostname);

            down.start();
            up.start();
            ka.start();

            try {
                sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
            }

            outputWriter.println("ASSOSIATING "+up.getUpport()+" "+ down.getDownport());
            clientSentence = inFromClient.readLine();
            App.ConsolePrint(pre + clientSentence);
            App.ConsolePrint(pre + "upport " + up.getUpport() + " downport " + down.getDownport());

            state = 1;
        } catch (IOException ex) {
            Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //client constructor
    public BlueNodeInstance(String PhAddress, int authPort, String AuthHostname, boolean exclusive, boolean FullAssociation) {
        App.ConsolePrint(pre + "Assosiating a New Blue Node with address " + PhAddress + ":" + authPort);
        this.PhaddressStr = PhAddress;
        try {
            this.Phaddress = InetAddress.getByName(PhaddressStr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        isServer = false;              
        int upport;
        int downport;

        InetAddress IPaddress = TCPSocketFunctions.getAddress(PhAddress);
        if (IPaddress == null) {
            state = -1;
            return;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, authPort);
        if (socket == null) {
            state = -1;
            return;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);                     
        String RemoteHostname = null;  
        RemoteHostname = args[1];
        
        if (isSameHostname(RemoteHostname)) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);            
            TCPSocketFunctions.connectionClose(socket);
            state = -1;
            return;
        }                 
        
        if (exclusive) {
            if (RemoteHostname.equals(AuthHostname)) {
                TCPSocketFunctions.sendData("BLUENODE " +App.Hostname, outputWriter, inputReader);
            } else {
                TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
                TCPSocketFunctions.connectionClose(socket);
                App.ConsolePrint(pre + "TARGET BLUE NODE NAME ERROR");
                state = -1;
                return;
            }
        } else {
            TCPSocketFunctions.sendData("BLUENODE " + App.Hostname, outputWriter, inputReader);
        }
        Hostname = RemoteHostname;

        if (FullAssociation) {                        
            args = TCPSocketFunctions.sendData("FULL_ASSOCIATE", outputWriter, inputReader);    
            System.out.println(args[0]+args[1]);
            if (!args[0].equals("BLUE_NODE_ALLREADY_IN_LIST")) {
                
                int count = Integer.parseInt(args[1]);
                for (int i = 0; i < count; i++) {
                    args = TCPSocketFunctions.readData(inputReader);
                    if (App.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                        App.remoteRedNodesTable.lease(args[0], args[1], RemoteHostname);
                    } else {
                        App.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                    }
                }
                TCPSocketFunctions.readData(inputReader);
                                
                int size = App.localRedNodesTable.getSize();
                outputWriter.println("SENDING_LOCAL_RED_NODES " + size);
                for (int i = 0; i < size; i++) {
                    String vaddress = App.localRedNodesTable.getRedNodeInstance(i).getVaddress();
                    String hostname = App.localRedNodesTable.getRedNodeInstance(i).getHostname();
                    outputWriter.println(vaddress + " " + hostname);
                }
                outputWriter.println();

                args = TCPSocketFunctions.readData(inputReader);
                downport = Integer.parseInt(args[1]);
                upport = Integer.parseInt(args[2]);
                TCPSocketFunctions.sendFinalData("OK", outputWriter);
                App.ConsolePrint(pre + "upport " + upport + " downport " + downport);
            } else {
                state = -1;
                return;
            }
        } else {
            args = TCPSocketFunctions.sendData("ASSOCIATE", outputWriter, inputReader);
            if (!args[0].equals("BLUE_NODE_ALLREADY_IN_LIST")) {
                downport = Integer.parseInt(args[1]);
                upport = Integer.parseInt(args[2]);
                TCPSocketFunctions.sendFinalData("OK ", outputWriter);
                App.ConsolePrint(pre + "upport " + upport + " downport " + downport);
            } else {
                state = -1;
                return;
            }
        }

        //starting keep alive
        ka = new BlueKeepAlive(RemoteHostname);
        //starting man
        man = new QueueManager(20);
        //starting down
        downcl = new BlueDownServiceClient(RemoteHostname, upport, PhAddress);
        //starting up
        upcl = new BlueUpServiceClient(RemoteHostname, downport, PhAddress);

        downcl.start();
        upcl.start();
        ka.start();

        TCPSocketFunctions.connectionClose(socket);
        state = 1;
    }

    public String getHostname() {
        return Hostname;
    }

    public InetAddress getRealPhaddress() {
        return Phaddress;
    }   

    public String getPhaddress() {
        return PhaddressStr;
    }

    public int getStatus() {
        return state;
    }

    public boolean isIsServer() {
        return isServer;
    }        

    public QueueManager getQueueMan() {
        return man;
    }

    public void killtasks() {
        ka.kill();
        if (isServer) {
            up.kill();
            down.kill();
        } else {
            upcl.kill();
            downcl.kill();
        }
    }

    public boolean getUPing() {
        return uping;
    }

    public void setUping(boolean uping) {
        this.uping = uping;
    }

    public String getDownStr() {
        if (isServer) {
            return down.toString();
        } else {
            return downcl.toString();
        }
    }

    public String getUpStr() {
        if (isServer) {
            return up.toString();
        } else {
            return upcl.toString();
        }
    }        

    public int getDownport() {
        if (isServer){
            return down.getDownport();
        } else {
            return downcl.getDownport();
        }
    }

    public int getUpport() {
        if (isServer){
            return up.getUpport();
        } else {
            return upcl.getUpport();
        }        
    }    
   
}
