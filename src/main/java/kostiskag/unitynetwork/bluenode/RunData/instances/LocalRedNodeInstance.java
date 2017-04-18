package kostiskag.unitynetwork.bluenode.RunData.instances;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.QueueManager;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import kostiskag.unitynetwork.bluenode.functions.IpAddrFunctions;
import kostiskag.unitynetwork.bluenode.redThreads.RedDownService;
import kostiskag.unitynetwork.bluenode.redThreads.RedKeepAlive;
import kostiskag.unitynetwork.bluenode.redThreads.RedlUpService;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackingRedNodeFunctions;

/** 
 * RedAuthService runs every time for a single user only It is responsible for
 * user registering user's uplink port fishing and it stays alive as long as the
 * user is connected
 * 
 * @author kostis
 */
public class LocalRedNodeInstance {

	//to check if a RN is connected in another BN before auth
	private final String pre = "^AUTH ";
    //object data
	private String Hostname;
    private String Vaddress;
    private String phAddressStr;
	private int port;    
    private int state = 0;
    //socket objects
    private BufferedReader socketReader;
    private PrintWriter socketWriter;
    //thread objects
    private RedDownService down;
    private RedlUpService up;
    private RedKeepAlive ka;
    private QueueManager man;
    //loggers
    private boolean uping = false;
    private boolean didTrigger = false;

    public LocalRedNodeInstance() {
        state = 0;
    }
    
    public LocalRedNodeInstance(String Hostname, String Vaddress) {
        this.Hostname = Hostname;
        this.Vaddress = Vaddress;
        this.state = 0;
    }

    public LocalRedNodeInstance(BufferedReader socketReader, PrintWriter socketWriter, String hostname, String vAddress, String phAddress, int port) {
    	this.Hostname = hostname;
        this.Vaddress = vAddress;
        this.socketReader = socketReader;
    	this.socketWriter = socketWriter;
    	this.phAddressStr = phAddress;
    	this.port = port;

    	//notify the gui variables
    	if (App.bn.gui && didTrigger == false) {
            MainWindow.jCheckBox2.setSelected(true);
            didTrigger = true;
        }

        //set queue manager
        man = new QueueManager(10);

        //set downlink (allways by the aspect of bluenode)
        down = new RedDownService(Vaddress);

        //set uplink (allways by the aspect of bluenode)
        up = new RedlUpService(Vaddress);

        //set keep alive
        ka = new RedKeepAlive(Vaddress);
        
        //start the above
        down.start();
        up.start();
        ka.start();
        
        state = 1;
    }

    public String getHostname() {
        return Hostname;
    }

    public String getVaddress() {
        return Vaddress;
    }

    public String getPhAddress() {
        return phAddressStr;
    }

    public int getPort() {
		return port;
	}
    
    public int getStatus() {
        return state;
    }
    
    public QueueManager getQueueMan() {
        return man;
    }
    
    private boolean getUPing() {
        return uping;
    }

    public RedlUpService getUp() {
        return up;
    }

    public RedDownService getDown() {
        return down;
    }

    public boolean isUPinged() {
        return uping;
    }
    
    public void setUPing(boolean b) {
        this.uping = b;
    }

    /**
     * here we have the terminal loop a user may
     * send commands to the BN monitoring his status
     */
    public void initTerm() {
        if (state > 0) {
            while (true) {
                String clientSentence = null;
                try {
                    clientSentence = socketReader.readLine();
                } catch (Exception ex) {
                    break;
                }
                
                if (clientSentence == null) {
                    break;
                }
                
                if (clientSentence.startsWith("PING")) {
                    socketWriter.println("PING OK");
                } else if (clientSentence.startsWith("UPING")) {
                    boolean set = false;
                    for (int i = 0; i < 12; i++) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        if (getUPing()) {
                            socketWriter.println("UPING OK");
                            set = true;
                            break;
                        }
                    }
                    if (set == false) {
                        socketWriter.println("UPING FAILED");
                    }
                    setUPing(false);

                } else if (clientSentence.startsWith("DPING")) {
                    if (getQueueMan() != null) {

                        byte[] payload = "00001 [DPING PACKET]".getBytes();
                        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);

                        for (int i = 0; i < 2; i++) {
                            getQueueMan().offer(data);
                        }
                        socketWriter.println("PING ON THE WAY");
                    } else {
                        socketWriter.println("BLUE NODE ERROR");
                        App.bn.ConsolePrint(pre + "NO QUEUE FOUND FOR " + Vaddress + " HOST KILLED");
                        break;
                    }
                } else if (clientSentence.startsWith("DREFRESH")) {
                    App.bn.ConsolePrint(pre + " " + Vaddress + " UP REFRESH");
                    urefresh();
                } else if (clientSentence.startsWith("UREFRESH")) {
                    App.bn.ConsolePrint(pre + Vaddress + " DOWN REFRESH");
                    drefresh();
                } else if (clientSentence.startsWith("WHOAMI")) {
                    whoami();
                } else if (clientSentence.startsWith("EXIT")) {
                    break;
                } else {
                    //not recognized command
                    socketWriter.println("NRC");
                }
            }    
            
            //remember you can't kill the socket here
        	//you have to let initTerm return and the socket closes by itself
        	
            //killing user tasks
            down.kill();
            up.kill();
            ka.kill();
            
            //setting state
            state = -1;       
        }
    }
    
    private void whoami() {
        socketWriter.println(Hostname + "/" + Vaddress + " ~ " + phAddressStr + ":" + up.getUpport() + ":" + down.getDownport());
    }

    private void urefresh() {
        up.kill();

        up = new RedlUpService(Vaddress);
        up.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        socketWriter.println("DOWNLINK REFRESH " + up.getUpport());
    }

    private void drefresh() {
        down.kill();
        down = new RedDownService(Vaddress);
        down.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        socketWriter.println("UPLINK REFRESH " + down.getDownport());
    }

    public void exit() {
    	socketWriter.println("BYE");
    }
}
