package kostiskag.unitynetwork.bluenode.BlueNodeService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;

/**
 *
 * @author kostis
 */
public class BlueNodeServer extends Thread{

	// a bluenode server should have only one port to listen
	public String pre = "^AUTH SERVER ";    
    public static Boolean didTrigger = false;

    public BlueNodeServer() {
    }        
    
    @Override
    public void run() {
        App.ConsolePrint(pre+"started at thread "+Thread.currentThread().getName()+" on port "+App.authport);
        try {
            ServerSocket welcomeSocket = new ServerSocket(App.authport);            
            if (App.gui && didTrigger==false){
                MainWindow.jCheckBox8.setSelected(true);
                didTrigger = true;
            }            
            
            while (true) {    
                Socket connectionSocket = welcomeSocket.accept();                
                BlueNodeService service = new BlueNodeService(connectionSocket);
                service.start();
            }        
        } catch (java.net.BindException e){
            App.ConsolePrint(pre +"PORT ALREADY IN USE APPLICATION WILL DIE IN 3secs");             
            App.die();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
}
