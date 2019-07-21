package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import org.kostiskag.unitynetwork.common.service.SimpleUnstoppedCyclicService;

import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;


/**
 * A proper bluenode server should have one port to listen for every request.
 *
 * @author Konstantinos Kagiampakis
 */
public final class BlueNodeServer extends SimpleUnstoppedCyclicService {

	private static final String PRE = "^AUTH SERVER ";
	private static BlueNodeServer BLUENODE_SERVER;

	private final int authPort;
    private ServerSocket serverSocket;
    private boolean didTrigger;

    public static BlueNodeServer newInstance(int authPort) {
        if (BLUENODE_SERVER == null) {
            BLUENODE_SERVER = new BlueNodeServer(authPort);
            BLUENODE_SERVER.start();
        }
        return BLUENODE_SERVER;
    }

    public static BlueNodeServer getInstance() {
        return BLUENODE_SERVER;
    }

    private BlueNodeServer(int authPort) {
    	this.authPort = authPort;
        try {
            this.serverSocket = new ServerSocket(authPort);
        } catch (BindException e){
            AppLogger.getInstance().consolePrint(PRE +"PORT ALREADY IN USE " + e.getLocalizedMessage());
            kill();
            Bluenode.getInstance().terminate();
        } catch (IOException e) {
            AppLogger.getInstance().consolePrint(PRE +"SERVER CONNECTION ERROR " + e.getLocalizedMessage());
            kill();
            Bluenode.getInstance().terminate();
        }
    }

    @Override
    protected void preActions() {
        AppLogger.getInstance().consolePrint(PRE +"started at thread "+Thread.currentThread().getName()+" on port "+authPort);
        if (!didTrigger && Bluenode.getInstance().isGui()) {
            MainWindow.getInstance().setAuthServiceAsEnabled();
            didTrigger = true;
        }
    }

    @Override
    protected void cyclicActions() {
        try {
            Socket connectionSocket = serverSocket.accept();
            BlueNodeService service = new BlueNodeService(connectionSocket);
            service.start();
        } catch (IOException e) {
            AppLogger.getInstance().consolePrint(PRE +"SERVER CONNECTION ERROR");
            kill();
            Bluenode.getInstance().terminate();
        }
    }

    @Override
    protected void postActions() {
        AppLogger.getInstance().consolePrint(PRE +"SERVICE ENDED");
    }
}
