package org.kostiskag.unitynetwork.bluenode;

import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.common.entry.NodeType;


public final class AppLogger {

    public static AppLogger APP_LOGGER;

    public enum MessageType {
        KEEP_ALIVE,
        PINGS,
        ACKS,
        ROUTING;
    }

    private final boolean gui;
    private final MainWindow mainWindow;
    private final boolean log;
    private final boolean printTraffic;

    public static AppLogger newInstance(boolean log, boolean printTraffic) {
        return AppLogger.newInstance(false, null, log, printTraffic);
    }

    public static AppLogger newInstance(boolean gui, MainWindow mainWindow, boolean log, boolean printTraffic) {
        if (APP_LOGGER == null) {
            APP_LOGGER = new AppLogger(gui, mainWindow, log, printTraffic);
        }
        return APP_LOGGER;
    }

    public static AppLogger getInstance() {
        return APP_LOGGER;
    }

    private AppLogger(boolean gui, MainWindow mainWindow, boolean log, boolean printTraffic) {
        if (gui && mainWindow != null) {
            this.gui = true;
            this.mainWindow = mainWindow;
        } else {
            this.gui = false;
            this.mainWindow = null;
        }
        this.log = log;
        this.printTraffic = printTraffic;
    }

    public synchronized void consolePrint(String message) {
        System.out.println(message);
        if (gui) {
            mainWindow.consolePrint(message);
        }
        if (log) {
            App.writeToLogFile(message);
        }
    }

    /**
     *  Prints a message to traffic console.
     *
     *  @param messageType ~ 0 keep alive, 1 pings, 2 acks, 3 routing
     *  @param hostType ~ 0 reds, 1 blues
     */
    public synchronized void trafficPrint(String message, AppLogger.MessageType messageType, NodeType hostType) {
        if (gui) {
            MainWindow.getInstance().trafficPrint(message, messageType, hostType);
        } else if (printTraffic) {
            System.out.println(hostType.name()+" "+messageType.name()+": "+message);
        }
    }

    public boolean isLog() {
        return log;
    }

    public boolean isGui() {
        return gui;
    }
}
