package org.kostiskag.unitynetwork.bluenode.service.bluenodeservice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;

/**
 * A proper bluenode server should have one port to listen for every request.
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeServer extends Thread {

	public final String pre = "^AUTH SERVER ";    
	public final int authPort;
	public Boolean didTrigger = false;
    
    public BlueNodeServer(int authPort) {
    	this.authPort = authPort;
    }        
    
    @Override
    public void run() {
        AppLogger.getInstance().consolePrint(pre+"started at thread "+Thread.currentThread().getName()+" on port "+authPort);
        try {
            ServerSocket serverSocket = new ServerSocket(authPort);            
            if (!didTrigger && Bluenode.getInstance().gui){
                MainWindow.getInstance().setAuthServiceAsEnabled();
                didTrigger = true;
            }            
            
            while (true) {    
                Socket connectionSocket = serverSocket.accept();                
                BlueNodeService service = new BlueNodeService(connectionSocket);
                service.start();
            }        
        } catch (java.net.BindException e){
            AppLogger.getInstance().consolePrint(pre +"PORT ALREADY IN USE APPLICATION WILL DIE IN 3secs");
            Bluenode.getInstance().die();
        } catch (IOException e) {
            e.printStackTrace();
            Bluenode.getInstance().die();
        }        
    }
}
