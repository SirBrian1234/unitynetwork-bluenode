package org.kostiskag.unitynetwork.bluenode.service.trackclient;

import java.util.concurrent.atomic.AtomicInteger;

import org.kostiskag.unitynetwork.common.service.SimpleCyclicService;
import org.kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class TrackerTimeBuilder extends SimpleCyclicService {

    private static final String PRE = "^Tracker sonar ";
    private static TrackerTimeBuilder TRACKER_TIME_BUILDER;

    public static TrackerTimeBuilder newInstance(int timeInSec) throws IllegalAccessException {
        if (TRACKER_TIME_BUILDER == null) {
            TRACKER_TIME_BUILDER = new TrackerTimeBuilder(timeInSec);
        }
        return TRACKER_TIME_BUILDER;
    }

    public static TrackerTimeBuilder getInstance() {
        return TRACKER_TIME_BUILDER;
    }

    // triggers
    private AtomicInteger trackerRespond = new AtomicInteger(0);

    private TrackerTimeBuilder(int timeInSec) throws IllegalAccessException {
        super(timeInSec);
    }

    @Override
    protected void preActions() {
        App.bn.ConsolePrint(PRE +"JUST STARTED");
    }

    @Override
    protected void postActions() {
        App.bn.ConsolePrint(PRE +"DIED");
    }

    @Override
    protected void interruptedMessage(InterruptedException e) {
        e.printStackTrace();
    }

    @Override
    protected void cyclicPayload() {
        int passedTime = trackerRespond.addAndGet(getTime());
        App.bn.ConsolePrint(PRE + " BUILDING TIME " + passedTime);

        if (passedTime > getTime() * 60) {
            App.bn.ConsolePrint(PRE + "GRAVE ERROR TRACKER DIED!!! REMOVING RNS, STARTING BN KILL");
            App.bn.localRedNodesTable.exitAll();
            App.bn.die();
        }
    }

    public int getTotalElapsedTime() {
        return trackerRespond.get();
    }

    public void resetClock() {
        trackerRespond.set(0);
    }
}
