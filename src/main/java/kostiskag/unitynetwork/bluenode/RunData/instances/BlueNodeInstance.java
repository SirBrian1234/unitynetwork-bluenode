package kostiskag.unitynetwork.bluenode.RunData.instances;

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
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

/**
 *
 * @author kostis
 */
public class BlueNodeInstance {

    private final String pre;
    private final String name;
    private final boolean isServer;
    private int remoteAuthPort;
    public final RemoteRedNodeTable table;    
    private InetAddress phAddress;
    private String phAddressStr;      
    private boolean uPing = false;
    private int state = 0;
    //threads, objects
    private final BlueKeepAlive ka;
    private final QueueManager man;
    private BlueDownServiceServer down;
    private BlueUpServiceServer up;
    private BlueDownServiceClient downcl;
    private BlueUpServiceClient upcl;
    
    /**
     * This object constructor is mainly used for testing.
     * 
     * @throws Exception 
     */
    public BlueNodeInstance(String name) {
    	this.name = name;    
    	this.isServer = false;
    	this.remoteAuthPort = 7000;
    	this.pre = "^BLUENODE "+name+" ";
    	this.state = 0;                  
        this.ka = new BlueKeepAlive(this);
        this.man = new QueueManager(20);
        this.table = new RemoteRedNodeTable(this);        
    }

    /**
     * This is the server constructor.
     */
    public BlueNodeInstance(String name, boolean fullAssociation, Socket sessionSocket) throws Exception {
    	this.isServer = true;
    	this.name = name;
        this.pre = "^BLUENODE "+name+" ";
        this.table = new RemoteRedNodeTable(this);
        this.ka = new BlueKeepAlive(this);
        this.man = new QueueManager(20);        
        
        App.bn.ConsolePrint(pre + "STARTING A BLUE AUTH AT " + Thread.currentThread().getName());
        String[] args;
        try {
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(sessionSocket.getInputStream()));
            PrintWriter socketWriter = new PrintWriter(sessionSocket.getOutputStream(), true);
            String clientSentence = null;

            if (App.bn.blueNodesTable.checkBlueNode(name)) {
            	socketWriter.println("BLUE_NODE_ALLREADY_IN_LIST");
            	state = -1;            	
                return;
            }
            
            if (fullAssociation) {
            	GlobalSocketFunctions.sendLocalRedNodes(socketWriter);

                clientSentence = socketReader.readLine();
                App.bn.ConsolePrint(pre + clientSentence);
                args = clientSentence.split("\\s+");

                int count = Integer.parseInt(args[1]);
                for (int i = 0; i < count; i++) {
                    clientSentence = socketReader.readLine();
                    App.bn.ConsolePrint(pre + clientSentence);
                    args = clientSentence.split("\\s+");

                    if (!App.bn.blueNodesTable.checkRemoteRedNodeByVaddress(args[1])) {
                    	App.bn.ConsolePrint(pre + clientSentence);
                    	table.lease(args[0], args[1]);                        
                    } else {
                    	state = -1;
                        App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                        throw new Exception(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                    }
                }
                clientSentence = socketReader.readLine();
                App.bn.ConsolePrint(pre + clientSentence);
            }

            phAddress = sessionSocket.getInetAddress();
            phAddressStr = phAddress.getHostAddress();                        
            
            //setting down as server
            down = new BlueDownServiceServer(this);
            //a=setting up as server
            up = new BlueUpServiceServer(this);

            //starting all threads
            down.start();
            up.start();
            ka.start();

            Thread.sleep(100);

            socketWriter.println("ASSOSIATING "+App.bn.authPort+" "+up.getUpport()+" "+down.getDownport());
            
            clientSentence = socketReader.readLine();
            args = clientSentence.split("\\s+");
            remoteAuthPort = Integer.parseInt(args[0]);
            
            App.bn.ConsolePrint(pre + clientSentence);
            App.bn.ConsolePrint(pre + "remote auth port "+remoteAuthPort+" upport "+up.getUpport()+" downport "+down.getDownport());

            state = 1;
            //remember!, don't close the socket here, let the method return and it will be closed from the caller
        } catch (IOException ex) {
            ex.printStackTrace();
            state = -1;
        }
    }

    /**
     * This is the client constructor.
     * 
     * @throws Exception 
     */
    public BlueNodeInstance(String phAddress, int authPort, String name, boolean fullAssociation) throws Exception {
    	this.isServer = true;
    	this.name = name;
        this.pre = "^BLUENODE "+name+" ";
        this.table = new RemoteRedNodeTable(this);
        this.ka = new BlueKeepAlive(this);
        this.man = new QueueManager(20);        
        
        App.bn.ConsolePrint(pre + "Assosiating a New Blue Node from address " + phAddress + ":" + authPort);
        this.phAddressStr = phAddress;
        try {
            this.phAddress = InetAddress.getByName(phAddressStr);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            return;
        }
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
        
        if (name.equals(args[1])) {
            TCPSocketFunctions.sendFinalData("EXIT", outputWriter);            
            state = -1;
            return;
        }                 
        
        TCPSocketFunctions.sendData("BLUENODE " + App.bn.name, outputWriter, inputReader);
        
        if (fullAssociation) {                        
            args = TCPSocketFunctions.sendData("FULL_ASSOCIATE", outputWriter, inputReader);    
            if (!args[0].equals("BLUE_NODE_ALLREADY_IN_LIST")) {
                
                int count = Integer.parseInt(args[1]);
                for (int i = 0; i < count; i++) {
                    args = TCPSocketFunctions.readData(inputReader);
                    if (App.bn.blueNodesTable.checkRemoteRedNodeByVaddress(args[1])) {
                        table.lease(args[0], args[1]);
                    } else {
                    	state = -1;
                    	App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
                    	throw new Exception(pre + "ALLREADY REGISTERED REMOTE RED NODE");                        
                    }
                }
                TCPSocketFunctions.readData(inputReader);
                GlobalSocketFunctions.sendLocalRedNodes(outputWriter);
                
                args = TCPSocketFunctions.readData(inputReader);
                remoteAuthPort = Integer.parseInt(args[1]);
                downport = Integer.parseInt(args[2]);
                upport = Integer.parseInt(args[3]);
                TCPSocketFunctions.sendFinalData(App.bn.authPort+" ", outputWriter);
                App.bn.ConsolePrint(pre + "upport " + upport + " downport " + downport);
            } else {
                state = -1;
                App.bn.ConsolePrint(pre + "BLUE_NODE_ALLREADY_IN_LIST");
                throw new Exception("BLUE_NODE_ALLREADY_IN_LIST");
            }
        } else {
            args = TCPSocketFunctions.sendData("ASSOCIATE", outputWriter, inputReader);
            if (!args[0].equals("BLUE_NODE_ALLREADY_IN_LIST")) {
                remoteAuthPort = Integer.parseInt(args[1]);
            	downport = Integer.parseInt(args[2]);
                upport = Integer.parseInt(args[3]);
                TCPSocketFunctions.sendFinalData(App.bn.authPort+" ", outputWriter);
                App.bn.ConsolePrint(pre + "remote authport "+remoteAuthPort+" upport " + upport + " downport " + downport);
            } else {            	
                state = -1;
                App.bn.ConsolePrint(pre + "BLUE_NODE_ALLREADY_IN_LIST");
                throw new Exception("BLUE_NODE_ALLREADY_IN_LIST");                
            }
        }

        //setting down as client
        downcl = new BlueDownServiceClient(this, upport);
        //setting up as client
        upcl = new BlueUpServiceClient(this, downport);

        //starting all threads
        downcl.start();
        upcl.start();
        ka.start();

        state = 1;
        //remember!, don't close the socket here, let the method return and it will be closed from the caller
    }

    public String getName() {
		return name;
	}

    public InetAddress getPhaddress() {
        return phAddress;
    }   

    public String getPhAddressStr() {
        return phAddressStr;
    }

    /**
     * get status 
     * 1 means fully connected 
     * 0 means idle
     * -1 or lower, means not connected 
     * the caller of this object must getStatus in order to save or discard the instance
     */
    public int getStatus() {
        return state;
    }

    public boolean isServer() {
        return isServer;
    }        

    public QueueManager getQueueMan() {
        return man;
    }
    
    public int getRemoteAuthPort() {
		return remoteAuthPort;
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
    
    public boolean getUPing() {
        return uPing;
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

    public void setUping(boolean uping) {
        this.uPing = uping;
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
        man.clear();
        state = -1;
    }
}
