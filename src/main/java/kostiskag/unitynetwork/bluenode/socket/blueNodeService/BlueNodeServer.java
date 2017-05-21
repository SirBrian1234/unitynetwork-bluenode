package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.gui.MainWindow;

/**
 * A proper bluenode server should have one port to listen for every request.
 *
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeServer extends Thread{

	public final String pre = "^AUTH SERVER ";    
	public final int authPort;
	public Boolean didTrigger = false;
    
    public BlueNodeServer(int authPort) {
    	this.authPort = authPort;
    }        
    
    @Override
    public void run() {
        App.bn.ConsolePrint(pre+"started at thread "+Thread.currentThread().getName()+" on port "+authPort);
        try {
            ServerSocket serverSocket = new ServerSocket(authPort);            
            if (!didTrigger && App.bn.gui){
                MainWindow.jCheckBox8.setSelected(true);
                didTrigger = true;
            }            
            
            while (true) {    
                Socket connectionSocket = serverSocket.accept();                
                BlueNodeService service = new BlueNodeService(connectionSocket);
                service.start();
            }        
        } catch (java.net.BindException e){
            App.bn.ConsolePrint(pre +"PORT ALREADY IN USE APPLICATION WILL DIE IN 3secs");             
            App.bn.die();
        } catch (IOException e) {
            e.printStackTrace();
            App.bn.die();
        }        
    }
}
