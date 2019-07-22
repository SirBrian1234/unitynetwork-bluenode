package org.kostiskag.unitynetwork.bluenode.service.bluenodeclient;

import java.util.concurrent.atomic.AtomicBoolean;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.common.service.SimpleCyclicService;

/**
 * Works like the java garbage collector but for killed bluenodes and remote redonodes. The sonar
 * connects to the associated bluenodes where the calling bluenode is a server
 * and requests to get their status and remote rednodes back
 * When a dead bn is found, it, and its rns are removed from this bluenode
 * 
 * @author Konstantinos Kagiampakis
 */
public final class BlueNodeSonarService extends SimpleCyclicService {

    private final String pre = "^BlueNodeSonarService ";
    private static boolean INSTANTIATED;
    private final BlueNodeTable table;

    public static BlueNodeSonarService newInstance(BlueNodeTable table, int timeInSec) throws IllegalAccessException {
        if (!INSTANTIATED) {
            INSTANTIATED = true;
            var s = new BlueNodeSonarService(table, timeInSec);
            s.start();
            return s;
        }
        return null;
    }

    private BlueNodeSonarService(BlueNodeTable table, int timeInSec) throws IllegalAccessException {
        super(timeInSec);
        this.table = table;
    }

    @Override
    protected void preActions() {
        AppLogger.getInstance().consolePrint(pre+"started in thread "+Thread.currentThread()+" with time period "+getTime()+" milli sec");
    }

    @Override
    protected void postActions() {

    }

    @Override
    protected void interruptedMessage(InterruptedException e) {

    }

    @Override
    protected void cyclicPayload() {
        AppLogger.getInstance().consolePrint(pre+"Updating BN Tables via ping");
        this.table.rebuildTableViaAuthClient();
    }
}
