package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import java.util.concurrent.atomic.AtomicInteger;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.common.service.SimpleCyclicService;

/**
 *
 * @author Konstantinos Kagiampakis
 */
final class TrackerTimeBuilder extends SimpleCyclicService {

    private static final String PRE = "^Tracker sonar ";
    private static boolean INSTANTIATED;

    private final Runnable bluenodeTerminate;

    public static TrackerTimeBuilder newInstance(int timeInSec, Runnable bluenodeTerminate) throws IllegalAccessException {
        //makes only one instance returns only one reference
        if (!INSTANTIATED) {
            INSTANTIATED = true;
            var timeBuilder = new TrackerTimeBuilder(timeInSec, bluenodeTerminate);
            timeBuilder.start();
            return timeBuilder;
        }
        return null;
    }

    // triggers
    private AtomicInteger trackerRespond = new AtomicInteger(0);

    private TrackerTimeBuilder(int timeInSec, Runnable bluenodeTerminate) throws IllegalAccessException {
        super(timeInSec);
        this.bluenodeTerminate = bluenodeTerminate;
    }

    @Override
    protected void preActions() {
        AppLogger.getInstance().consolePrint(PRE +"JUST STARTED");
    }

    @Override
    protected void postActions() {
        AppLogger.getInstance().consolePrint(PRE +"DIED");
    }

    @Override
    protected void interruptedMessage(InterruptedException e) {
        e.printStackTrace();
    }

    @Override
    protected void cyclicPayload() {
        int passedTime = trackerRespond.addAndGet(getTime());
        AppLogger.getInstance().consolePrint(PRE + " BUILDING TIME " + passedTime);

        if (passedTime > getTime() * 60) {
            AppLogger.getInstance().consolePrint(PRE + "GRAVE ERROR TRACKER DIED!!! REMOVING RNS, STARTING BN KILL");
            bluenodeTerminate.run();
        }
    }

    public int getTotalElapsedTime() {
        return trackerRespond.get();
    }

    public void resetClock() {
        trackerRespond.set(0);
    }
}
