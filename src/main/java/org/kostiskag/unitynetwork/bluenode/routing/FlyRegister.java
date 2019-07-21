package org.kostiskag.unitynetwork.bluenode.routing;

import java.security.PublicKey;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.common.entry.NodeType;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.AppLogger.MessageType;
import org.kostiskag.unitynetwork.common.service.SimpleUnstoppedCyclicService;


/**
 *
 * @author Konstantinos Kagiampakis
 */


public final class FlyRegister extends SimpleUnstoppedCyclicService {

    private static final String PRE = "^FlyRegister ";
    private static final int maxQueueCapacity = 100;
    private static FlyRegister FLY_REGISTER;

    public static FlyRegister newInstance() {
        if (FLY_REGISTER == null) {
            FLY_REGISTER = new FlyRegister();
            FLY_REGISTER.start();
        }
        return FLY_REGISTER;
    }

    public static FlyRegister getInstance() {
        return FLY_REGISTER;
    }

    private final QueuePair queue = new QueuePair(maxQueueCapacity);

    private FlyRegister() { }

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

        String sourcevaddress = pair.sourcevaddress;
        String destvaddress = pair.destvaddress;

        AppLogger.getInstance().consolePrint(PRE + "Seeking to associate "+sourcevaddress+" with "+destvaddress);

        if (Bluenode.getInstance().blueNodeTable.checkRemoteRedNodeByVaddress(destvaddress)) {
            //check if it was associated one loop back
            AppLogger.getInstance().consolePrint(PRE + "Allready associated entry");
            return;
        } else {
            //make stuff
            TrackerClient tr = new TrackerClient();
            String BNHostname = tr.checkRnOnlineByVaddr(destvaddress);
            if (BNHostname != null) {
                if (Bluenode.getInstance().blueNodeTable.checkBlueNode(BNHostname)) {
                    //we might have him associated but we may not have his rrd
                    BlueNode bn;
                    try {
                        bn = Bluenode.getInstance().blueNodeTable.getBlueNodeInstanceByName(BNHostname);
                        BlueNodeClient cl = new BlueNodeClient(bn);
                        String remoteHostname = cl.getRedNodeHostnameByVaddress(destvaddress);
                        if (!remoteHostname.equals("OFFLINE")) {
                            Bluenode.getInstance().blueNodeTable.leaseRRn(bn, remoteHostname, destvaddress);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //if he is not associated at all, then associate
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
                    try {
                        cl.associateClient();
                    } catch (Exception e) {
                        AppLogger.getInstance().trafficPrint(PRE + "FAILED TO ASSOCIATE WITH BLUE NODE " + BNHostname, MessageType.ROUTING, NodeType.BLUENODE);
                        return;
                    }
                    AppLogger.getInstance().trafficPrint(PRE + "BLUE NODE " + BNHostname + " ASSOCIATED", MessageType.ROUTING, NodeType.BLUENODE);

                    //we were associated now it's time to feed return route
                    BlueNode bn;
                    try {
                        bn = Bluenode.getInstance().blueNodeTable.getBlueNodeInstanceByName(BNHostname);
                        cl = new BlueNodeClient(bn);
                        cl.feedReturnRoute(LocalRedNodeTable.getInstance().getRedNodeInstanceByAddr(sourcevaddress).getHostname(), sourcevaddress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //and then request the dest rn hotsname
                    try {
                        bn = Bluenode.getInstance().blueNodeTable.getBlueNodeInstanceByName(BNHostname);
                        cl = new BlueNodeClient(bn);
                        String remoteHostname = cl.getRedNodeHostnameByVaddress(destvaddress);
                        if (!remoteHostname.equals("OFFLINE")) {
                            Bluenode.getInstance().blueNodeTable.leaseRRn(bn, remoteHostname, destvaddress);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                AppLogger.getInstance().consolePrint(PRE + "NOT FOUND "+destvaddress+" ON NETWORK");
            }
        }
    }

    public void seekDest(String sourcevaddress, String destvaddress) {
        SourceDestPair pair = new SourceDestPair(sourcevaddress, destvaddress);        
        queue.offer(pair);        
    }
    
    public void kill() {
        super.kill();
        queue.exit();
    }
}
