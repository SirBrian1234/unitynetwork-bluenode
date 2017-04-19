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
    private final String phAddressStr;
    private final InetAddress phAddress;
    private final boolean isServer;
    private int remoteAuthPort;
    //threads, objects
    public final RemoteRedNodeTable table;
    private final BlueKeepAlive ka;
    private final QueueManager man;
    private BlueDownServiceServer down;
    private BlueUpServiceServer up;
    private BlueDownServiceClient downcl;
    private BlueUpServiceClient upcl;
    //triggers
    private int state = 0;
    private boolean uPing = false;
    
    /**
     * This object constructor is mainly used for testing.
     * 
     * @throws Exception 
     */
    public BlueNodeInstance(String name) {
    	this.name = name;    
    	this.isServer = false;
    	this.phAddressStr = "1.1.1.1";
    	this.phAddress = TCPSocketFunctions.getAddress(phAddressStr);
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
    public BlueNodeInstance(String name, String phAddressStr) throws Exception {
    	this.isServer = true;
    	this.name = name;
    	this.pre = "^BLUENODE "+name+" ";
    	this.phAddressStr = phAddressStr;
    	this.phAddress = TCPSocketFunctions.getAddress(phAddressStr);
        this.table = new RemoteRedNodeTable(this);
        this.ka = new BlueKeepAlive(this);
        this.man = new QueueManager(20);                
        
        //setting down as server
        down = new BlueDownServiceServer(this);
        //a=setting up as server
        up = new BlueUpServiceServer(this);

        //starting all threads
        down.start();
        up.start();
        ka.start();

        //hold the thread a bit to catch up the started threads
        Thread.sleep(100);

        state = 1;
        //remember!, don't close the socket here, let the method return and it will be closed from the caller       
    }

    /**
     * This is the client constructor.
     * 
     * @throws Exception 
     */
    public BlueNodeInstance(String name, String phAddress, int authPort, int upPort, int downPort) throws Exception {
    	this.isServer = false;
    	this.name = name;
        this.pre = "^BLUENODE "+name+" ";
        this.phAddressStr = phAddress;
        this.phAddress = TCPSocketFunctions.getAddress(phAddressStr);
        this.table = new RemoteRedNodeTable(this);
        this.ka = new BlueKeepAlive(this);
        this.man = new QueueManager(20);        
        
        //setting down as client
        downcl = new BlueDownServiceClient(this, upPort);
        //setting up as client
        upcl = new BlueUpServiceClient(this, downPort);

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
    
    public void setRemoteAuthPort(int remoteAuthPort) {
		this.remoteAuthPort = remoteAuthPort;
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
