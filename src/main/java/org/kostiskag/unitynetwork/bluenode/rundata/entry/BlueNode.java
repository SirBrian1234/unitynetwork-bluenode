package org.kostiskag.unitynetwork.bluenode.rundata.entry;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.net.UnknownHostException;
import java.security.PublicKey;

import org.kostiskag.unitynetwork.common.routing.QueueManager;
import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.entry.NodeEntry;

import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;
import org.kostiskag.unitynetwork.bluenode.routing.Router;
import org.kostiskag.unitynetwork.bluenode.rundata.table.RemoteRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.bluethreads.BlueReceive;
import org.kostiskag.unitynetwork.bluenode.bluethreads.BlueNodeTimeBuilder;
import org.kostiskag.unitynetwork.bluenode.bluethreads.BlueSend;
import org.kostiskag.unitynetwork.bluenode.bluethreads.UploadManager;
import org.kostiskag.unitynetwork.bluenode.Bluenode.Timings;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNode extends NodeEntry<PhysicalAddress> {

    private final String pre;
    private final PublicKey pub;
    private final int remoteAuthPort;
    private final boolean isServer;    
    //threads, objects
    private final RemoteRedNodeTable table;
    private final QueueManager sendQueue;
    private final QueueManager receiveQueue;
    private final BlueNodeTimeBuilder timeBuilder;
    private final BlueReceive receive;
    private final BlueSend send;
    private final UploadManager uploadMan;
    private final Router router;
    //triggers
    public final AtomicInteger idleTime = new AtomicInteger(0);
    private final AtomicBoolean uPing = new AtomicBoolean(false);
    private final AtomicBoolean dPing = new AtomicBoolean(false);


    /**
     * This is the server constructor.
     * 
     */
    public BlueNode(String name, PublicKey pub, String phAddressStr, int authPort) throws UnknownHostException, IllegalAccessException, InterruptedException {
        this(true, name, pub, phAddressStr, authPort, 0, 0);
    }

    /**
     * This is the client constructor.
     * 
     */
    public BlueNode(String name, PublicKey pub, String phAddress, int authPort, int upPort, int downPort) throws UnknownHostException, IllegalAccessException, InterruptedException {
        this(false, name, pub, phAddress, authPort, upPort, downPort);
    }

    private BlueNode(boolean isServer, String name, PublicKey pub, String phAddress, int authPort, int upPort, int downPort) throws UnknownHostException, IllegalAccessException, InterruptedException{
        super(name, PhysicalAddress.valueOf(phAddress));
        this.isServer = isServer;
        this.pub = pub;
        this.pre = "^BLUENODE "+name+" ";
        this.remoteAuthPort = authPort;
        this.table = new RemoteRedNodeTable(this);

        //setting upload manager
        this.uploadMan = new UploadManager();
        //setting queues
        this.sendQueue = new QueueManager(20, Timings.KEEP_ALIVE_TIME.getWaitTimeInSec());
        this.receiveQueue = new QueueManager(20, Timings.KEEP_ALIVE_TIME.getWaitTimeInSec());
        this.router  = new Router(this, receiveQueue);

        if (isServer) {
            this.send = new BlueSend(this);
            this.receive = new BlueReceive(this);
            this.timeBuilder = null;
        } else {
            this.send = new BlueSend(this, upPort);
            this.receive = new BlueReceive(this, downPort);
            //starts on its own!
            this.timeBuilder = BlueNodeTimeBuilder.newInstance(this, Timings.BLUENODE_STEP_TIME.getWaitTimeInSec(), Timings.BLUENODE_MAX_IDLE_TIME.getWaitTimeInSec());
        }

        //starting all threads
        this.send.start();
        this.receive.start();
        this.router.start();

        //hold the thread a bit to catch up the started threads
        Thread.sleep(200);
        //remember!, don't close the socket here, let the method return and it will be closed from the caller
    }

    public PublicKey getPub() {
		return pub;
	}

    public RemoteRedNodeTable getTable() {
        return table;
    }

    public int getRemoteAuthPort() {
		return remoteAuthPort;
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
    
    public QueueManager<byte[]> getSendQueue() {
        return sendQueue;
    }
    
    public QueueManager<byte[]> getReceiveQueue() {
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
    
    public void resetIdleTime() {
		idleTime.set(0);
	}

    public void killtasks() { 
        //ka.kill();
        send.kill();
        receive.kill();
        router.kill();
        
        if (!isServer) {
            timeBuilder.kill();
        }
        
        sendQueue.clear();
    }


    /**
     * release performs all the inner BlueNode operations for a logical release.
     *
     * @throws IllegalAccessException
     * @throws InterruptedException
     */
    public void release() throws IllegalAccessException, InterruptedException {
        BlueNodeClient cl = new BlueNodeClient(this);
        cl.removeThisBlueNodesProjection();
        this.killtasks();
    }

    /**
     * Releases a RemoteRedNode only if it's found!
     *
     * @param hostname
     * @throws IllegalAccessException
     * @throws InterruptedException
     */
    public void releaseRRn(String hostname) throws IllegalAccessException, InterruptedException {
        //do not be confused, the lock for the inner RemoteRedNodeTable is different
        //from that of BlueNode Table
        Lock lock = null;
        try {
            lock = this.getTable().aquireLock();
            var o = this.getTable().getOptionalNodeEntry(lock, hostname);
            if (o.isPresent()) {
                BlueNodeClient cl = new BlueNodeClient(this);
                cl.removeRedNodeProjectionByHn(hostname);

                this.getTable().release(lock, o.get());
            } else {
                throw new IllegalAccessError("no RRN with hostname " + hostname);
            }
        } finally {
            lock.unlock();
        }
    }
}
