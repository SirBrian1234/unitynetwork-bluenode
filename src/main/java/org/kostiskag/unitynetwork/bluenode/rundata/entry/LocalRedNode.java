package org.kostiskag.unitynetwork.bluenode.rundata.entry;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.crypto.SecretKey;

import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;
import org.kostiskag.unitynetwork.common.utilities.SocketUtilities;

import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.Bluenode.Timings;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.routing.QueueManager;
import org.kostiskag.unitynetwork.bluenode.routing.Router;
import org.kostiskag.unitynetwork.bluenode.redthreads.RedReceive;
import org.kostiskag.unitynetwork.bluenode.redthreads.RedlSend;


/** 
 * RedAuthService runs every time for a single user only It is responsible for
 * user registering user's uplink port fishing and it stays alive as long as the
 * user is connected
 * 
 * @author Konstantinos Kagiampakis
 */
public class LocalRedNode {

	//to check if a RN is connected in another BN before auth
	private final String pre = "^AUTH ";
    //object data
	private String Hostname;
    private String Vaddress;
    private SecretKey sessionKey;
    private String phAddressStr;
	private int port;    
    private int state = 0;
    //socket objects
    private DataInputStream socketReader;
    private DataOutputStream socketWriter;
    //thread objects
    private RedReceive receive;
    private RedlSend send;
    private QueueManager sendQueue;
    private QueueManager receiveQueue;
    private Router router;
    //loggers
    private boolean uping = false;
    private boolean didTrigger = false;

    public LocalRedNode() {
        state = 0;
    }
    
    /**
     * This is a test constructor
     */
    public LocalRedNode(String Hostname, String Vaddress) {
        this.Hostname = Hostname;
        this.Vaddress = Vaddress;
        this.state = 0;
    }

    public LocalRedNode(String hostname, String vAddress, String phAddress, int port, DataInputStream socketReader, DataOutputStream socketWriter, SecretKey sessionKey) {
    	this.Hostname = hostname;
        this.Vaddress = vAddress;
        this.sessionKey = sessionKey;
        
        this.socketReader = socketReader;
    	this.socketWriter = socketWriter;
    	this.phAddressStr = phAddress;
    	this.port = port;

    	//notify the gui variables
    	if (!didTrigger) {
    		if (Bluenode.getInstance().isGui()) {
    			MainWindow.getInstance().setOneUserAsConnected();
    		}
            didTrigger = true;
        }

        //set queues
        sendQueue = new QueueManager(10, Timings.KEEP_ALIVE_TIME.getWaitTimeInSec());
        receiveQueue = new QueueManager(10, Timings.KEEP_ALIVE_TIME.getWaitTimeInSec());
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
                String[] args = null;
                try {
                    args = SocketUtilities.receiveAESEncryptedStringData(socketReader, sessionKey);
                } catch (Exception ex) {
                    break;
                }
                
                if (args == null) {
                    break;
                }
                
                if (args[0].equals("PING")) {
                	try {
                        SocketUtilities.sendAESEncryptedStringData("PING OK", socketWriter, sessionKey);
					} catch (Exception e) {
						e.printStackTrace();
					}
                } else if (args[0].equals("UPING")) {
                	setUPing(false);
                	boolean set = false;
                	try {
                        SocketUtilities.sendAESEncryptedStringData("SET", socketWriter, sessionKey);
					} catch (Exception e) {
						e.printStackTrace();
					}
                    for (int i = 0; i < 12; i++) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        if (getUPing()) {
                        	try {
                                SocketUtilities.sendAESEncryptedStringData("UPING OK", socketWriter, sessionKey);
							} catch (Exception e) {
								e.printStackTrace();
							}
                            set = true;
                            break;
                        }
                    }
                    if (!set) {
                    	try {
                            SocketUtilities.sendAESEncryptedStringData("UPING FAILED", socketWriter, sessionKey);
						} catch (Exception e) {
							e.printStackTrace();
						}
                    }
                    setUPing(false);

                } else if (args[0].equals("DPING")) {                    
                        byte[] data = UnityPacket.buildDpingPacket();
                        for (int i = 0; i < 10; i++) {
                            getSendQueue().offer(data);
                            try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
                        }                                                                  
                } else if (args[0].equals("DREFRESH")) {
                    AppLogger.getInstance().consolePrint(pre + " " + Vaddress + " UP REFRESH");
                    drefresh();
                } else if (args[0].equals("UREFRESH")) {
                    AppLogger.getInstance().consolePrint(pre + Vaddress + " DOWN REFRESH");
                    urefresh();
                } else if (args[0].equals("WHOAMI")) {
                    whoami();
                } else if (args[0].equals("EXIT")) {
                    break;
                } else {
                    //not recognized command
                	try {
                        SocketUtilities.sendAESEncryptedStringData("NRC", socketWriter, sessionKey);
					} catch (Exception e) {
						e.printStackTrace();
					}
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
    	try {
            SocketUtilities.sendAESEncryptedStringData(Hostname + "/" + Vaddress + " ~ " + phAddressStr + ":" + send.getServerPort() + ":" + receive.getServerPort(), socketWriter, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
        try {
            SocketUtilities.sendAESEncryptedStringData("DREFRESH "+send.getServerPort(), socketWriter, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
        try {
            SocketUtilities.sendAESEncryptedStringData("UREFRESH "+receive.getServerPort(), socketWriter, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void exit() {
    	try {
            SocketUtilities.sendAESEncryptedStringData("BYE", socketWriter, sessionKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
