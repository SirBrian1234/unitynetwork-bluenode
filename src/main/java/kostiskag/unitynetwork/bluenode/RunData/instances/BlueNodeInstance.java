package kostiskag.unitynetwork.bluenode.RunData.instances;

import static kostiskag.unitynetwork.bluenode.socket.blueNodeClient.BlueNodeClientFunctions.isSameHostname;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.QueueManager;
import kostiskag.unitynetwork.bluenode.RunData.tables.RemoteRedNodeTable;
import kostiskag.unitynetwork.bluenode.blueThreads.BlueDownServiceClient;
import kostiskag.unitynetwork.bluenode.blueThreads.BlueDownServiceServer;
import kostiskag.unitynetwork.bluenode.blueThreads.BlueKeepAlive;
import kostiskag.unitynetwork.bluenode.blueThreads.BlueUpServiceClient;
import kostiskag.unitynetwork.bluenode.blueThreads.BlueUpServiceServer;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

/**
 *
 * @author kostis
 */
public class BlueNodeInstance extends Thread {

    private final String pre = "^BLUENODE ";
    //RedNodes
    public RemoteRedNodeTable table;
    //data
    private String name;
    private boolean isServer;
    private InetAddress phAddress;
    private String phAddressStr;      
    private boolean uPing = false;
    private int state = 0;
    //threads
    private BlueDownServiceServer down;
    private BlueUpServiceServer up;
    private BlueDownServiceClient downcl;
    private BlueUpServiceClient upcl;
    private BlueKeepAlive ka;
    private QueueManager man;
    
    //get status
    /* 
     * 1 means fully connected 
     * 0 means idle
     * -1 or lower, means error 
     * the caller of this object after construction must getStatus in order to save or discard the instance
     */
    public BlueNodeInstance() {
        state = 0;
        table = new RemoteRedNodeTable(this);
    }

    //server constructor
    public BlueNodeInstance(String name, boolean FullAssociation, Socket sessionSocket) {
        App.bn.ConsolePrint(pre + "STARTING A BLUE AUTH AT " + Thread.currentThread().getName());
        this.name = name;
        table = new RemoteRedNodeTable(this);
        isServer = true;
        String[] args;

        try {
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(sessionSocket.getInputStream()));
            PrintWriter socketWriter = new PrintWriter(sessionSocket.getOutputStream(), true);
            String clientSentence = null;

            if (App.bn.blueNodesTable.getBlueNodeInstanceByName(name) != null) {
                App.bn.blueNodesTable.releaseBn(name);
                try {
                    sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


            if (FullAssociation) {
            	LinkedList<String> fetched = App.bn.localRedNodesTable.buildAddrHostStringList();
                int size = fetched.size();
                socketWriter.println("SENDING_LOCAL_RED_NODES " + size);
                TCPSocketFunctions.sendFinalData("SENDING_LOCAL_RED_NODES " + size, socketWriter);
            	Iterator<String> it = fetched.listIterator();
                while(it.hasNext()){
                	String toSend = it.next();
                	socketWriter.println(toSend);
                }        
                socketWriter.println(" ");

                clientSentence = socketReader.readLine();
                App.bn.ConsolePrint(pre + clientSentence);
                args = clientSentence.split("\\s+");

                int count = Integer.parseInt(args[1]);
                for (int i = 0; i < count; i++) {
                    clientSentence = socketReader.readLine();
                    App.bn.ConsolePrint(pre + clientSentence);
                    args = clientSentence.split("\\s+");

                    if (App.bn.remoteRedNodesTable.getByVaddress(args[0]) == null) {
                        App.bn.remoteRedNodesTable.lease(args[0], args[1], name);
                    } else {
                        App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                    }
                }
                clientSentence = socketReader.readLine();
                App.bn.ConsolePrint(pre + clientSentence);
            }

            phAddress = sessionSocket.getInetAddress();
            phAddressStr = phAddress.getHostAddress();                        

            //keep alive
            ka = new BlueKeepAlive(name);
            //starting managers
            man = new QueueManager(20);
            //starting down
            down = new BlueDownServiceServer(name);
            //starting up
            up = new BlueUpServiceServer(name);

            down.start();
            up.start();
            ka.start();

            try {
                sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
            }

            socketWriter.println("ASSOSIATING "+up.getUpport()+" "+ down.getDownport());
            clientSentence = socketReader.readLine();
            App.bn.ConsolePrint(pre + clientSentence);
            App.bn.ConsolePrint(pre + "upport " + up.getUpport() + " downport " + down.getDownport());

            state = 1;
        } catch (IOException ex) {
            Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //client constructor
    public BlueNodeInstance(String phAddress, int authPort, String authName, boolean exclusive, boolean fullAssociation) {
        App.bn.ConsolePrint(pre + "Assosiating a New Blue Node with address " + phAddress + ":" + authPort);
        this.phAddressStr = phAddress;
        try {
            this.phAddress = InetAddress.getByName(phAddressStr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(BlueNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        isServer = false;              
        int upport;
        int downport;

        InetAddress IPaddress = TCPSocketFunctions.getAddress(phAddress);
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
            if (RemoteHostname.equals(authName)) {
                TCPSocketFunctions.sendData("BLUENODE " +App.bn.name, outputWriter, inputReader);
            } else {
                TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
                TCPSocketFunctions.connectionClose(socket);
                App.bn.ConsolePrint(pre + "TARGET BLUE NODE NAME ERROR");
                state = -1;
                return;
            }
        } else {
            TCPSocketFunctions.sendData("BLUENODE " + App.bn.name, outputWriter, inputReader);
        }
        name = RemoteHostname;

        if (fullAssociation) {                        
            args = TCPSocketFunctions.sendData("FULL_ASSOCIATE", outputWriter, inputReader);    
            System.out.println(args[0]+args[1]);
            if (!args[0].equals("BLUE_NODE_ALLREADY_IN_LIST")) {
                
                int count = Integer.parseInt(args[1]);
                for (int i = 0; i < count; i++) {
                    args = TCPSocketFunctions.readData(inputReader);
                    if (App.bn.remoteRedNodesTable.getByVaddress(args[0]) == null) {
                        App.bn.remoteRedNodesTable.lease(args[0], args[1], RemoteHostname);
                    } else {
                        App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                    }
                }
                TCPSocketFunctions.readData(inputReader);
                 
                LinkedList<String> fetched = App.bn.localRedNodesTable.buildAddrHostStringList();
                int size = fetched.size();
                outputWriter.println("SENDING_LOCAL_RED_NODES " + size);
                Iterator<String> it = fetched.listIterator();
                while(it.hasNext()){
                	String toSend = it.next();
                	outputWriter.println(toSend);
                }        
                outputWriter.println(" ");
                
                args = TCPSocketFunctions.readData(inputReader);
                downport = Integer.parseInt(args[1]);
                upport = Integer.parseInt(args[2]);
                TCPSocketFunctions.sendFinalData("OK", outputWriter);
                App.bn.ConsolePrint(pre + "upport " + upport + " downport " + downport);
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
                App.bn.ConsolePrint(pre + "upport " + upport + " downport " + downport);
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
        downcl = new BlueDownServiceClient(RemoteHostname, upport, phAddress);
        //starting up
        upcl = new BlueUpServiceClient(RemoteHostname, downport, phAddress);

        downcl.start();
        upcl.start();
        ka.start();

        TCPSocketFunctions.connectionClose(socket);
        state = 1;
    }

    public String getHostname() {
        return name;
    }

    public InetAddress getPhaddress() {
        return phAddress;
    }   

    public String getPhAddressStr() {
        return phAddressStr;
    }

    public int getStatus() {
        return state;
    }

    public boolean isServer() {
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
        return uPing;
    }

    public void setUping(boolean uping) {
        this.uPing = uping;
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
