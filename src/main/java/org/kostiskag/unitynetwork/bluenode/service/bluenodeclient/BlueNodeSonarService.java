package org.kostiskag.unitynetwork.bluenode.service.bluenodeclient;

import java.util.concurrent.atomic.AtomicBoolean;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
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
    private static BlueNodeSonarService BLUENODE_SONAR_SERVICE;

    public static BlueNodeSonarService newInstance(int timeInSec) throws IllegalAccessException {
        if (BLUENODE_SONAR_SERVICE == null) {
            BLUENODE_SONAR_SERVICE = new BlueNodeSonarService(timeInSec);
        }
        return BLUENODE_SONAR_SERVICE;
    }

    public static BlueNodeSonarService getInstance() {
        return BLUENODE_SONAR_SERVICE;
    }


    private BlueNodeSonarService(int timeInSec) throws IllegalAccessException {
        super(timeInSec);
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
        Bluenode.getInstance().blueNodeTable.rebuildTableViaAuthClient();
    }
}
