package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import java.util.concurrent.atomic.AtomicInteger;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.common.service.SimpleCyclicService;
import org.kostiskag.unitynetwork.common.service.TimeBuilder;

/**
 *
 * @author Konstantinos Kagiampakis
 */
final class TrackerTimeBuilder extends TimeBuilder {

    private static final String PRE = "^Tracker sonar ";
    private static boolean INSTANTIATED;

    private final Runnable bluenodeTerminate;

    public static TrackerTimeBuilder newInstance(int buildStepSec, int maxWaitTimeSec, Runnable bluenodeTerminate) throws IllegalAccessException {
        //makes only one instance returns only one reference
        if (!INSTANTIATED) {
            INSTANTIATED = true;
            var timeBuilder = new TrackerTimeBuilder(buildStepSec, maxWaitTimeSec, bluenodeTerminate);
            timeBuilder.start();
            return timeBuilder;
        }
        return null;
    }

    private TrackerTimeBuilder(int buildStepSec, int maxWaitTimeSec, Runnable bluenodeTerminate) throws IllegalAccessException {
        super(buildStepSec, maxWaitTimeSec);
        this.bluenodeTerminate = bluenodeTerminate;
    }

    @Override
    protected void preActions() {
        AppLogger.getInstance().consolePrint(PRE +"JUST STARTED");
    }

    @Override
    protected void postActions() {
        AppLogger.getInstance().consolePrint(PRE + "GRAVE ERROR TRACKER DIED!!! REMOVING RNS, STARTING BN KILL");
        bluenodeTerminate.run();
    }

    @Override
    protected void interruptedMessage(InterruptedException e) {
        AppLogger.getInstance().consolePrint(PRE + "interrupted "+e.getLocalizedMessage());
    }

}
