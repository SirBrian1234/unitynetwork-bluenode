package kostiskag.unitynetwork.bluenode.BlueNodeService;


import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * BLUENODE MUST HAVE ONLY ONE AUTHPORT
 */

/**
 *
 * @author kostis
 */
public class BlueNodeServer extends Thread{

    public String pre = "^AUTH SERVER ";    
    public static Boolean didTrigger = false;

    public BlueNodeServer() {
    }        
    
    @Override
    public void run() {
        lvl3BlueNode.ConsolePrint(pre+"started at thread "+Thread.currentThread().getName()+" on port "+lvl3BlueNode.authport);
        try {
            ServerSocket welcomeSocket = new ServerSocket(lvl3BlueNode.authport);            
            if (lvl3BlueNode.gui && didTrigger==false){
                MainWindow.jCheckBox8.setSelected(true);
                didTrigger = true;
            }            
            
            while (true) {    
                Socket connectionSocket = welcomeSocket.accept();                
                BlueNodeService service = new BlueNodeService(connectionSocket);
                service.start();
            }        
        } catch (java.net.BindException e){
            lvl3BlueNode.ConsolePrint(pre +"PORT ALREADY IN USE APPLICATION WILL DIE IN 3secs");             
            lvl3BlueNode.die();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
}
