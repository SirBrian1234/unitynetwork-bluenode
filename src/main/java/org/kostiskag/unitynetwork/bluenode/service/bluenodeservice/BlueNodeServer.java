package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.PublicKey;

import org.kostiskag.unitynetwork.bluenode.ModeOfOperation;
import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.service.SimpleUnstoppedCyclicService;

import org.kostiskag.unitynetwork.bluenode.rundata.table.AccountTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.AppLogger;


/**
 * A proper bluenode server should have one port to listen for every request.
 *
 * @author Konstantinos Kagiampakis
 */
public final class BlueNodeServer extends SimpleUnstoppedCyclicService {

	private static final String PRE = "^AUTH SERVER ";
	private static boolean INSTANTIATED;

	private final ModeOfOperation operationMode;
	private final String localBluenodeName;
    //to be used in rednodeservice
    private final AccountTable accountTable;
    private final LocalRedNodeTable rednodeTable;
    //to be used in bluenodeservice
    private final BlueNodeTable bluenodeTable;
    private final KeyPair bluenodeKeyPair;
    private final PublicKey trackerPublic;
    //to be used in tracking service
    private final TrackerTimeBuilder trackerTimeBuilder;
    //service
    private final int authPort;
    private final ServerSocket serverSocket;
    //terminate bluenode
    private final Runnable terminateBluenode;

    private boolean didTrigger;

    public static BlueNodeServer newInstance(String localBluenodeName, LocalRedNodeTable rednodeTable, int authPort, Runnable terminateBluenode) throws IOException, IllegalAccessException {
        return BlueNodeServer.newInstance(ModeOfOperation.PLAIN, localBluenodeName, null, rednodeTable, null, null, null, 0, authPort, terminateBluenode);
    }

    public static BlueNodeServer newInstance(String localBluenodeName, AccountTable accountTable, LocalRedNodeTable rednodeTable, int authPort, Runnable terminateBluenode) throws IOException, IllegalAccessException {
        return BlueNodeServer.newInstance(ModeOfOperation.LIST, localBluenodeName, accountTable, rednodeTable, null, null, null, 0, authPort, terminateBluenode);
    }

    public static BlueNodeServer newInstance(String localBluenodeName, LocalRedNodeTable rednodeTable, BlueNodeTable bluenodeTable, KeyPair bluenodeKeyPair, PublicKey trackerPublic, int authPort, int trackerTimeBuilderTIme, Runnable terminateBluenode) throws IOException, IllegalAccessException {
        return BlueNodeServer.newInstance(ModeOfOperation.NETWORK, localBluenodeName, null, rednodeTable, bluenodeTable, bluenodeKeyPair, trackerPublic, trackerTimeBuilderTIme, authPort, terminateBluenode);
    }

    private static BlueNodeServer newInstance(ModeOfOperation mode,
                                              String localBluenodeName,
                                              AccountTable accountTable,
                                              LocalRedNodeTable rednodeTable,
                                              BlueNodeTable bluenodeTable,
                                              KeyPair bluenodeKeyPair,
                                              PublicKey trackerPublic,
                                              int trackerTimeBuilderTIme,
                                              int authPort,
                                              Runnable terminateBluenode) throws IOException, IllegalAccessException {
        if (!INSTANTIATED) {
            INSTANTIATED = true;
            var server = new BlueNodeServer(mode, localBluenodeName, accountTable, rednodeTable, bluenodeTable, bluenodeKeyPair, trackerPublic, trackerTimeBuilderTIme, authPort, terminateBluenode);
            server.start();
            return server;
        }
        return null;
    }

    private BlueNodeServer(ModeOfOperation mode,
                           String localBluenodeName,
                           AccountTable accountTable,
                           LocalRedNodeTable rednodeTable,
                           BlueNodeTable bluenodeTable,
                           KeyPair bluenodeKeyPair,
                           PublicKey trackerPublic,
                           int trackerTimeBuilderTIme,
                           int authPort,
                           Runnable terminateBluenode)  throws IOException, IllegalAccessException{

        //sanitize depending on mode of operation, throw illegal argument exc
        var msg = PRE+"malformed arguments were given";
        if (mode == null || localBluenodeName == null || authPort <=0 || authPort > NumericConstraints.MAX_ALLOWED_PORT_NUM.size() || terminateBluenode == null) {
            throw new IllegalArgumentException(msg);
        }
        if (mode == ModeOfOperation.PLAIN) {
            if (rednodeTable == null || accountTable != null || bluenodeTable != null || bluenodeKeyPair != null || trackerPublic != null || trackerTimeBuilderTIme != 0) {
                throw new IllegalArgumentException(msg);
            }
        } else if (mode == ModeOfOperation.LIST) {
            if ( accountTable == null || rednodeTable == null || bluenodeTable != null || bluenodeKeyPair != null || trackerPublic != null || trackerTimeBuilderTIme != 0) {
                throw new IllegalArgumentException(msg);
            }
        } else {
            if ( rednodeTable == null || bluenodeTable == null || bluenodeKeyPair == null || trackerPublic == null || trackerTimeBuilderTIme == 0 || accountTable != null) {
                throw new IllegalArgumentException(msg);
            }
        }

        //initialize server socket
        ServerSocket serverSocket = new ServerSocket(authPort);

        //initialize TrackerTimeBuilder
        if (mode == ModeOfOperation.NETWORK) {
            /*
             * Time builder periodically checks the tracker to determine if it's alive!
             *
             */
            this.trackerTimeBuilder = TrackerTimeBuilder.newInstance(trackerTimeBuilderTIme, terminateBluenode);
        } else {
            this.trackerTimeBuilder = null;
        }

        this.operationMode = mode;
        this.localBluenodeName = localBluenodeName;
        this.accountTable = accountTable;
        this.rednodeTable = rednodeTable;
        this.bluenodeTable = bluenodeTable;
        this.bluenodeKeyPair = bluenodeKeyPair;
        this.trackerPublic = trackerPublic;
        this.authPort = authPort;
        this.serverSocket = serverSocket;
        this.terminateBluenode = terminateBluenode;
    }

    @Override
    protected void preActions() {
        AppLogger.getInstance().consolePrint(PRE +"started at thread "+Thread.currentThread().getName()+" on port "+authPort);
        if (!didTrigger && AppLogger.getInstance().isGui()) {
            MainWindow.getInstance().setAuthServiceAsEnabled();
            didTrigger = true;
        }
    }

    @Override
    protected void cyclicActions() {
        try {
            Socket connectionSocket = serverSocket.accept();
            BlueNodeService service = null;
            if (operationMode == ModeOfOperation.NETWORK) {
                service = new BlueNodeService(localBluenodeName, rednodeTable, bluenodeTable, trackerTimeBuilder, bluenodeKeyPair, trackerPublic, connectionSocket, terminateBluenode);
            } else if (operationMode == ModeOfOperation.LIST) {
                service = new BlueNodeService(localBluenodeName, accountTable, rednodeTable, connectionSocket);
            } else {
                service = new BlueNodeService(localBluenodeName, rednodeTable, connectionSocket);
            }
            service.start();
        } catch (IOException e) {
            AppLogger.getInstance().consolePrint(PRE +"SERVER CONNECTION ERROR");
            kill();
            terminateBluenode.run();
        }
    }

    @Override
    protected void postActions() {
        AppLogger.getInstance().consolePrint(PRE +"SERVICE ENDED");
    }

    @Override
    public void kill() {
        super.kill();
        if (operationMode == ModeOfOperation.NETWORK) {
            trackerTimeBuilder.kill();
        }
    }
}
