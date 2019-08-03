package org.kostiskag.unitynetwork.bluenode.bluethreads;

import org.kostiskag.unitynetwork.common.service.TimeBuilder;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.AppLogger;

import java.util.concurrent.locks.Lock;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeTimeBuilder extends TimeBuilder {

    private final String pre;
    private final BlueNode bn;

    public static BlueNodeTimeBuilder newInstance(BlueNode bn, int buildStepSec, int maxWaitTimeSec) throws IllegalAccessException {
        var b = new BlueNodeTimeBuilder(bn, buildStepSec, maxWaitTimeSec);
        b.start();
        return b;
    }

    private BlueNodeTimeBuilder(BlueNode bn, int buildStepSec, int maxWaitTimeSec) throws IllegalAccessException{
    	super(buildStepSec, maxWaitTimeSec);
        this.bn = bn;
    	this.pre = "^BlueNodeTimeBuilder "+bn.getHostname()+" ";
    }

    @Override
    protected void preActions() {
        AppLogger.getInstance().consolePrint(pre+"JUST STARTED");
    }

    @Override
    protected void postActions() {
        AppLogger.getInstance().consolePrint(pre+"BlueNode is not responding releasing from the local bn table");
        Lock lock = null;
        try {
            Bluenode.getInstance().blueNodeTable.aquireLock();
            Bluenode.getInstance().blueNodeTable.releaseBn(lock, bn);
        } catch (InterruptedException | IllegalAccessException e) {
            AppLogger.getInstance().consolePrint(pre+"BlueNodeTable release exception "+e.getLocalizedMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void interruptedMessage(InterruptedException e) {
        AppLogger.getInstance().consolePrint(pre+" exception "+e.getLocalizedMessage());
    }
}
