package kostiskag.unitynetwork.bluenode.RunData.instances;

import java.io.BufferedReader;
import java.io.PrintWriter;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.gui.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.QueueManager;
import kostiskag.unitynetwork.bluenode.Routing.Router;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.redThreads.RedReceive;
import kostiskag.unitynetwork.bluenode.redThreads.RedlSend;

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
    private RedReceive receive;
    private RedlSend send;    
    private QueueManager sendQueue;
    private QueueManager receiveQueue;
    private Router router;
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
    	if (!didTrigger) {
    		if (App.bn.gui) {
    			MainWindow.jCheckBox2.setSelected(true);
    		}
            didTrigger = true;
        }

        //set queues
        sendQueue = new QueueManager(10, App.bn.keepAliveSec);
        receiveQueue = new QueueManager(10, App.bn.keepAliveSec);
        router = new Router(getHostname(), receiveQueue);

        //set downlink (allways by the aspect of bluenode)
        receive = new RedReceive(this);

        //set uplink (allways by the aspect of bluenode)
        send = new RedlSend(this);
        
        //start the above
        receive.start();
        send.start();        
        router.start();
        
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
    
    public QueueManager getSendQueue() {
        return sendQueue;
    }
    
    public QueueManager getReceiveQueue() {
        return receiveQueue;
    }
    
    private boolean getUPing() {
        return uping;
    }

    public RedlSend getSend() {
        return send;
    }

    public RedReceive getReceive() {
        return receive;
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
                	setUPing(false);
                	boolean set = false;
                	socketWriter.println("SET");
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
                    if (!set) {
                        socketWriter.println("UPING FAILED");
                    }
                    setUPing(false);

                } else if (clientSentence.startsWith("DPING")) {                    
                        byte[] data = UnityPacket.buildDpingPacket();
                        for (int i = 0; i < 10; i++) {
                            getSendQueue().offer(data);
                            try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
                        }                                                                  
                } else if (clientSentence.startsWith("DREFRESH")) {
                    App.bn.ConsolePrint(pre + " " + Vaddress + " UP REFRESH");
                    drefresh();
                } else if (clientSentence.startsWith("UREFRESH")) {
                    App.bn.ConsolePrint(pre + Vaddress + " DOWN REFRESH");
                    urefresh();
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
            receive.kill();
            send.kill();
            router.kill();
            
            //setting state
            state = -1;       
        }
    }
    
    private void whoami() {
        socketWriter.println(Hostname + "/" + Vaddress + " ~ " + phAddressStr + ":" + send.getServerPort() + ":" + receive.getServerPort());
    }

    private void drefresh() {
        send.kill();
        send = new RedlSend(this);
        send.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        socketWriter.println("DREFRESH "+send.getServerPort());
    }

    private void urefresh() {
        receive.kill();
        receive = new RedReceive(this);
        receive.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        socketWriter.println("UREFRESH "+receive.getServerPort());
    }

    public void exit() {
    	socketWriter.println("BYE");
    }
}
