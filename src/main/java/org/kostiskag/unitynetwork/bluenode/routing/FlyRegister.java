package org.kostiskag.unitynetwork.bluenode.routing;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.concurrent.locks.Lock;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.service.SimpleUnstoppedCyclicService;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.AppLogger.MessageType;
import org.kostiskag.unitynetwork.bluenode.AppLogger;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public final class FlyRegister extends SimpleUnstoppedCyclicService {

    private static final String PRE = "^FlyRegister ";
    private static final int maxQueueCapacity = 100;
    private static FlyRegister FLY_REGISTER;

    private final LocalRedNodeTable localRedNodeTable;
    private final BlueNodeTable blueNodeTable;

    public static FlyRegister newInstance(LocalRedNodeTable localRedNodeTable, BlueNodeTable blueNodeTable) {
        if (FLY_REGISTER == null) {
            FLY_REGISTER = new FlyRegister(localRedNodeTable, blueNodeTable);
            FLY_REGISTER.start();
        }
        return FLY_REGISTER;
    }

    // i don't know if we can omit this as to move FlyReg with dependency injection to the
    // multiple constructors of the object which are using it would be very troublesome
    public static FlyRegister getInstance() {
        return FLY_REGISTER;
    }

    private final QueuePair queue = new QueuePair(maxQueueCapacity);

    private FlyRegister(LocalRedNodeTable localRedNodeTable, BlueNodeTable blueNodeTable) {
        this.localRedNodeTable = localRedNodeTable;
        this.blueNodeTable = blueNodeTable;
    }

    @Override
    protected void preActions() {
        AppLogger.getInstance().consolePrint(PRE +"STARTED");
    }

    @Override
    protected void postActions() {
        AppLogger.getInstance().consolePrint(PRE +"ENDED");
    }

    @Override
    protected void cyclicActions() {
        //waits...
        SourceDestPair pair =  queue.poll();
        //This obj makes the thread to wait when empty

        if (super.getKillValue() || pair == null) {
            return;
        }

        VirtualAddress sourceAddress = pair.sourceAddress;
        VirtualAddress destAddress = pair.destAddress;
        AppLogger.getInstance().consolePrint(PRE + "Seeking to associate "+sourceAddress.asString()+" with "+destAddress.asString());

        Lock lock = null;
        try {
            lock = blueNodeTable.aquireLock();

            if (this.blueNodeTable.getBlueNodeEntryByRemoteRedNode(lock, destAddress).isPresent()) {
                //check if it was associated one loop back
                throw new IllegalAccessException("Already associated entry");

            } else {
                //make stuff
                TrackerClient tr = new TrackerClient();
                String BNHostname = tr.checkRnOnlineByVaddr(destAddress.asString());
                if (BNHostname != null) {
                    var bno = this.blueNodeTable.getOptionalEntry(lock, BNHostname);
                    if (bno.isPresent()) {
                        //we might have him associated but we may not have his rrd
                        BlueNode bn = bno.get();
                        BlueNodeClient cl = new BlueNodeClient(bn);
                        String remoteHostname = cl.getRedNodeHostnameByVaddress(destAddress);
                        if (!remoteHostname.equals("OFFLINE")) {
                            this.blueNodeTable.leaseRemoteRedNode(lock, bn, remoteHostname, destAddress);
                        }
                    } else {
                        //if he is not associated at all, then associate with remote bn
                        //first collect its ip address and port
                        tr = new TrackerClient();
                        String[] args = tr.getPhysicalBn(BNHostname);
                        if (args[0].equals("OFFLINE")) {
                            AppLogger.getInstance().trafficPrint(PRE + "FAILED TO ASSOCIATE WITH BLUE NODE, OFFLINE " + BNHostname, MessageType.ROUTING, NodeType.BLUENODE);
                            return;
                        }
                        String address = args[0];
                        int port = Integer.parseInt(args[1]);

                        //then, collect its public key
                        tr = new TrackerClient();
                        PublicKey pub = tr.getBlueNodesPubKey(BNHostname);
                        if (pub == null) {
                            AppLogger.getInstance().trafficPrint(PRE + "FAILED TO ASSOCIATE WITH BLUE NODE, NO PUBLIC KEY " + BNHostname, MessageType.ROUTING, NodeType.BLUENODE);
                            return;
                        }

                        //use the above data to connect
                        BlueNodeClient cl = new BlueNodeClient(BNHostname, pub, address, port);
                        cl.associateClient(lock);
                        AppLogger.getInstance().trafficPrint(PRE + "BLUE NODE " + BNHostname + " ASSOCIATED", MessageType.ROUTING, NodeType.BLUENODE);

                        //we were associated now it's time to feed return route
                        var obn = this.blueNodeTable.getOptionalEntry(lock, BNHostname);
                        if (obn.isPresent()) {
                            //we provide the remote the rednode seeking to make a connection
                            cl = new BlueNodeClient(obn.get());
                            cl.feedReturnRoute(this.localRedNodeTable.getRedNodeInstanceByAddr(sourceAddress.asString()).getHostname(), sourceAddress.asString());

                            //and then request the destination rednodes hotsname
                            cl = new BlueNodeClient(obn.get());
                            String remoteHostname = cl.getRedNodeHostnameByVaddress(destAddress);
                            if (!remoteHostname.equals("OFFLINE")) {
                                var fetched = RemoteRedNode.newInstance(remoteHostname, destAddress, bno.get());
                                this.blueNodeTable.leaseRemoteRedNode(lock, obn.get(), fetched);
                            }
                        } else {
                            throw new IllegalAccessException("BlueNode was not associated although it should!");
                        }
                    }
                } else {
                    AppLogger.getInstance().consolePrint(PRE + "NOT FOUND "+destAddress.asString()+" ON NETWORK");
                }
            }
        } catch (IOException | GeneralSecurityException | InterruptedException | IllegalAccessException e) {
            AppLogger.getInstance().consolePrint(PRE + " "+e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void seekDest(VirtualAddress sourceAddress, VirtualAddress destAddress) {
        SourceDestPair pair = new SourceDestPair(sourceAddress, destAddress);
        queue.offer(pair);        
    }
    
    public void kill() {
        super.kill();
        queue.exit();
    }
}
