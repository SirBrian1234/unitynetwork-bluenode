package org.kostiskag.unitynetwork.bluenode.rundata.entry;

import java.util.concurrent.atomic.AtomicBoolean;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.entry.NodeEntry;
import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.routing.QueueManager;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.routing.Router;
import org.kostiskag.unitynetwork.bluenode.redthreads.RedReceive;
import org.kostiskag.unitynetwork.bluenode.redthreads.RedlSend;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.Bluenode.Timings;
import org.kostiskag.unitynetwork.bluenode.AppLogger;


/** 
 * RedAuthService runs every time for a single user only It is responsible for
 * user registering user's uplink port fishing and it stays alive as long as the
 * user is connected
 * 
 * @author Konstantinos Kagiampakis
 */
public class LocalRedNode extends NodeEntry<VirtualAddress> {

    private static final String pre = "^AUTH ";
    private static boolean didTrigger;

    private static final int MAX_UPING_OFFER_ATTEMPTS = 12;
    private static final long UPING_TIME_PERIOD = 500;
    private static final int MAX_DPING_OFFER_ATTEMPTS = 10;
    private static final long DPING_TIME_PERIOD = 200;
    private static final long DREFRESH_WAIT_TIME = 1000;
    private static final long UREFRESH_WAIT_TIME = 1000;

    //object data
    private final SecretKey sessionKey;
    private final PhysicalAddress phAddress;
    private final int port;
    //socket objects
    private final DataInputStream socketReader;
    private final DataOutputStream socketWriter;
    //thread objects
    private final QueueManager<byte[]> sendQueue;
    private final QueueManager<byte[]> receiveQueue;
    private final Router router;
    private RedReceive receive;
    private RedlSend send;
    //loggers
    private AtomicBoolean uping = new AtomicBoolean(false);
    private boolean connected;

    public LocalRedNode(
            String hostname,
            String vAddress,
            String phAddress,
            int port,
            DataInputStream socketReader,
            DataOutputStream socketWriter,
            SecretKey sessionKey) throws UnknownHostException, IllegalAccessException{

        super(hostname, VirtualAddress.valueOf(vAddress));
        this.phAddress = PhysicalAddress.valueOf(phAddress);

        this.sessionKey = sessionKey;
        this.socketReader = socketReader;
        this.socketWriter = socketWriter;
    	this.port = port;

    	//notify the gui variables
    	if (!didTrigger) {
            didTrigger = true;
            if (AppLogger.getInstance().isGui()) {
                MainWindow.getInstance().setOneUserAsConnected();
            }
        }

        //set queues
        this.sendQueue = new QueueManager(10, Timings.KEEP_ALIVE_TIME.getWaitTimeInSec());
        this.receiveQueue = new QueueManager(10, Timings.KEEP_ALIVE_TIME.getWaitTimeInSec());
        this.router = new Router(this, receiveQueue);

        //set downlink (always by the aspect of bluenode)
        this.receive = new RedReceive(this);

        //set uplink (always by the aspect of bluenode)
        this.send = new RedlSend(this);
        
        //start the above
        receive.start();
        send.start();        
        router.start();
    }

    public PhysicalAddress getPhAddress() {
        return phAddress;
    }

    public int getPort() {
		return port;
	}

    public QueueManager<byte[]> getSendQueue() {
        return sendQueue;
    }
    
    public QueueManager<byte[]> getReceiveQueue() {
        return receiveQueue;
    }
    
    private boolean getUPing() {
        return uping.get();
    }

    public RedlSend getSend() {
        return send;
    }

    public RedReceive getReceive() {
        return receive;
    }

    public boolean isUPinged() {
        return uping.get();
    }
    
    public void setUPing(boolean b) {
        this.uping.set(b);
    }

    /**
     * here we have the terminal loop a user may
     * send commands to the BN monitoring his status
     */
    public void initTerm() {
        connected = true;
        while (true) {
            String[] args = null;
            try {
                args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
            } catch (GeneralSecurityException | IOException ex) {
                break;
            }
            if (args == null || args.length != 1) {
                continue;
            }

            if (args[0].equals("PING")) {
                ping();
            } else if (args[0].equals("UPING")) {
                uPing();
            } else if (args[0].equals("DPING")) {
                dPing();
            } else if (args[0].equals("DREFRESH")) {
                dRefresh();
            } else if (args[0].equals("UREFRESH")) {
                uRefresh();
            } else if (args[0].equals("WHOAMI")) {
                whoami();
            } else if (args[0].equals("EXIT")) {
                break;
            } else {
                nrc();
            }
        }

        //remember you can't kill the socket here
        //you have to let initTerm return and the socket closes by itself

        //killing user tasks
        receive.kill();
        send.kill();
        router.kill();

        connected = false;
    }

    private void ping() {
        response("PING OK");
    }

    private void whoami() {
        response(getHostname() + "/" + getAddress() + " ~ " + phAddress.asString() + ":" + send.getServerPort() + ":" + receive.getServerPort());
    }

    private void nrc() {
        //not recognized command
        response("NRC");
    }

    public void exit() {
        response("BYE");
    }

    private void uPing() {
        setUPing(false);
        boolean set = false;
        response("SET");
        for (int i = 0; i < MAX_UPING_OFFER_ATTEMPTS; i++) {
            waitTimePeriod(UPING_TIME_PERIOD);
            if (getUPing()) {
                response("UPING OK");
                set = true;
                break;
            }
        }
        if (!set) {
            response("UPING FAILED");
        }
        setUPing(false);
    }

    private void dPing() {
        byte[] data = UnityPacket.buildDpingPacket();
        for (int i = 0; i < MAX_DPING_OFFER_ATTEMPTS; i++) {
            getSendQueue().offer(data);
            waitTimePeriod(DPING_TIME_PERIOD);
        }
    }

    private void dRefresh() {
        AppLogger.getInstance().consolePrint(pre + " " + getAddress().asString() + " UP REFRESH");
        send.kill();
        send = new RedlSend(this);
        send.start();
        waitTimePeriod(DREFRESH_WAIT_TIME);
        response("DREFRESH "+send.getServerPort());
    }

    private void uRefresh() {
        AppLogger.getInstance().consolePrint(pre + getAddress().asString() + " DOWN REFRESH");
        receive.kill();
        receive = new RedReceive(this);
        receive.start();
        waitTimePeriod(UREFRESH_WAIT_TIME);
        response("UREFRESH "+receive.getServerPort());
    }

    private void response(String response) {
        try {
            SocketUtilities.sendAESEncryptedStringData(response, socketWriter, sessionKey);
        } catch (GeneralSecurityException | IOException e) {
            AppLogger.getInstance().consolePrint(pre+" socket error "+e.getLocalizedMessage());
        }
    }

    private void waitTimePeriod(long timeInMilliSec) {
        try {
            Thread.sleep(timeInMilliSec);
        } catch (InterruptedException e) {
            AppLogger.getInstance().consolePrint(pre+" interrupted time socket error "+e.getLocalizedMessage());
        }
    }
}
