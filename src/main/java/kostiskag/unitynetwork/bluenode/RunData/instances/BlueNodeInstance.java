package kostiskag.unitynetwork.bluenode.RunData.instances;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.QueueManager;
import kostiskag.unitynetwork.bluenode.Routing.Router;
import kostiskag.unitynetwork.bluenode.RunData.tables.RemoteRedNodeTable;
import kostiskag.unitynetwork.bluenode.blueThreads.BlueReceive;
import kostiskag.unitynetwork.bluenode.blueThreads.BlueNodeTimeBuilder;
import kostiskag.unitynetwork.bluenode.blueThreads.BlueSend;
import kostiskag.unitynetwork.bluenode.blueThreads.UploadManager;
import kostiskag.unitynetwork.bluenode.functions.GetTime;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeInstance {

    private final String pre;
    private final String name;
    private final String phAddressStr;
    private final InetAddress phAddress;
    private final int remoteAuthPort;
    private final boolean isServer;    
    //time
    private String timestamp;
    public AtomicInteger idleTime = new AtomicInteger(0);
    //threads, objects
    public RemoteRedNodeTable table;    
    private QueueManager sendQueue;
    private QueueManager receiveQueue;
    private BlueNodeTimeBuilder timeBuilder;
    private BlueReceive receive;
    private BlueSend send;
    private UploadManager uploadMan;
    private Router router;
    //triggers
    private int state = 0;    
    private AtomicBoolean uPing = new AtomicBoolean(false);
    private AtomicBoolean dPing = new AtomicBoolean(false);
    
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
    	this.pre = "^BlueNodeInstance "+name+" ";
    	this.state = 0;                  
        this.sendQueue = new QueueManager(20, App.bn.trackerMaxIdleTime);
        this.uploadMan = new UploadManager();
        this.table = new RemoteRedNodeTable(this);    
        this.timestamp = GetTime.getSmallTimestamp();
    }

    /**
     * This is the server constructor.
     * 
     */
    public BlueNodeInstance(String name, String phAddressStr, int authPort) throws Exception {
    	this.isServer = true;
    	this.name = name;
    	this.remoteAuthPort = authPort;
    	this.pre = "^BLUENODE "+name+" ";
    	this.phAddressStr = phAddressStr;
    	this.phAddress = TCPSocketFunctions.getAddress(phAddressStr);
        this.table = new RemoteRedNodeTable(this);
        this.timestamp = GetTime.getSmallTimestamp();
        
        //setting upload manager
        this.uploadMan = new UploadManager();
        //setting queues
        this.sendQueue = new QueueManager(20, App.bn.keepAliveSec);   
        this.receiveQueue = new QueueManager(20, App.bn.keepAliveSec);   
        this.router  = new Router(getName(), receiveQueue);
        //setting down as server
        this.receive = new BlueReceive(this);
        //a=setting up as server
        this.send = new BlueSend(this);

        //starting all threads
        this.receive.start();
        this.send.start();
        this.router.start();

        //hold the thread a bit to catch up the started threads
        Thread.sleep(200);

        //remember!, don't close the socket here, let the method return and it will be closed from the caller       
    }

    /**
     * This is the client constructor.
     * 
     */
    public BlueNodeInstance(String name, String phAddress, int authPort, int upPort, int downPort) throws Exception {
    	this.isServer = false;
    	this.name = name;
        this.pre = "^BLUENODE "+name+" ";
        this.phAddressStr = phAddress;
        this.phAddress = TCPSocketFunctions.getAddress(phAddressStr);
        this.remoteAuthPort = authPort;
        this.table = new RemoteRedNodeTable(this);
        this.timestamp = GetTime.getSmallTimestamp();
        
        //setting upload manager
        this.uploadMan = new UploadManager();
        //setting queues
        this.sendQueue = new QueueManager(20, App.bn.keepAliveSec);   
        this.receiveQueue = new QueueManager(20, App.bn.keepAliveSec); 
        this.router  = new Router(getName(), receiveQueue);
        //setting down as client
        this.send = new BlueSend(this, upPort);
        //setting up as client
        this.receive = new BlueReceive(this, downPort);
        //clients have also a timeBuilder
        this.timeBuilder = new BlueNodeTimeBuilder(this, App.bn.blueNodeTimeStepSec, App.bn.blueNodeMaxIdleTimeSec);
        
        //starting all threads
        this.send.start();
        this.receive.start();
        this.router.start();
        this.timeBuilder.start(); 
        
        //hold the thread a bit to catch up the started threads
        Thread.sleep(200);

        //remember!, don't close the socket here, let the method return and it will be closed from the caller
    }

    public String getName() {
		return name;
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

    public InetAddress getPhaddress() {
        return phAddress;
    }   

    public String getPhAddressStr() {
        return phAddressStr;
    }
    
    public int getRemoteAuthPort() {
		return remoteAuthPort;
	}

    public String getTime() {
        return timestamp;
    }
    
    public boolean getUPing() {
        return uPing.get();
    }
    
    public boolean getDPing() {
        return dPing.get();
    }
    
    public UploadManager getUploadMan() {
		return uploadMan;
	}
    
    public QueueManager getSendQueue() {
        return sendQueue;
    }
    
    public QueueManager getReceiveQueue() {
		return receiveQueue;
	}
    
    public int getServerReceivePort() {
        if (isServer){
            return receive.getServerPort();
        } else {
            return 0;
        }
    }

    public int getServerSendPort() {
        if (isServer){
            return send.getServerPort();
        } else {
            return 0;
        }        
    }    
    
    public int getPortToSend() {
    	return send.getPortToSend();
    }
    
    public int getPortToReceive() {
    	return receive.getPortToReceive();
    }
    
    public boolean isServer() {
        return isServer;
    }
    
    public String isTheRemoteAServer() {
        if (isServer) {
        	return "NO";
        } else {
        	return "YES";
        }
    }
    
    public void setUping(boolean uping) {
        this.uPing.set(uping);
    }
    
    public void setDping(boolean dping) {
        this.dPing.set(dping);
    }
    
    public void updateTime() {
        this.timestamp = GetTime.getSmallTimestamp();
    }
    
    public void resetIdleTime() {
		idleTime.set(0);
	}

    public void killtasks() { 
        //ka.kill();
        send.kill();
        receive.kill();
        router.kill();
        
        if (!isServer) {
            timeBuilder.Kill();
        }
        
        sendQueue.clear();
        state = -1;
    }
}
