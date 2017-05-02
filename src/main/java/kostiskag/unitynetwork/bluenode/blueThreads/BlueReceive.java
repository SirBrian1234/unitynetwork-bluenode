package kostiskag.unitynetwork.bluenode.blueThreads;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import kostiskag.unitynetwork.bluenode.gui.MainWindow;
import kostiskag.unitynetwork.bluenode.redThreads.RedReceive;
import kostiskag.unitynetwork.bluenode.Routing.packets.IPv4Packet;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.App;

/**
 * In matters of communication this class it tricky. Do not expect BlueReceive to be a client or server
 * as it may be both. Depending on which bluenode starts the communication first the server and client roles
 * may be defined. However, as the class suggests despite being run either as server or as client this 
 * class will always receive.
 * 
 * @author kostis
 */
public class BlueReceive extends Thread {

    private final String pre;
    private final BlueNodeInstance blueNode;
    private final boolean isServer;
    //connection
    private int serverPort;
    private int portToReceive;   
    private DatagramSocket socket;
    private InetAddress blueNodePhAddress;
	//triggers
	private Boolean didTrigger = false;
    private AtomicBoolean kill = new AtomicBoolean(false);
    
    /**
     * This is the server constructor. 
     */
    public BlueReceive(BlueNodeInstance blueNode) {
    	this.isServer = true;
        this.serverPort = App.bn.UDPports.requestPort();
        this.blueNode = blueNode;
        this.pre = "^BlueDownServiceServer "+blueNode.getName()+" ";
        //simply since the server listens the port to receive is the server port
        this.portToReceive = serverPort;
    }
    
    /**
     * This is the client constructor
     */
    public BlueReceive(BlueNodeInstance blueNode, int portToReceive) {
    	this.isServer = false;
        this.blueNode = blueNode;
        this.pre = "^BlueReceive "+blueNode.getName()+" ";
        this.blueNodePhAddress = blueNode.getPhaddress();    	
        //collects the port to receive from auth
    	this.portToReceive = portToReceive;        
    }
    
    public int getServerPort() {
    	return serverPort;
    }   
    
    public int getPortToReceive() {
		return portToReceive;
	}

    public BlueNodeInstance getBlueNode() {
		return blueNode;
	}
    
    public boolean isKilled() {
		return kill.get();
	}
    
    @Override
    public void run() {
    	if (isServer) {
    		buildServer();
    	} else {
    		buildClient();
    	}
    	
    	App.bn.ConsolePrint(pre + "STARTED AT " + Thread.currentThread().getName());  
    	byte[] receiveData = new byte[2048];
    	while (!kill.get()) {
        	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(receivePacket);
                int len = receivePacket.getLength();
                if (len > 0 && len <= 1500) {
                    byte[] packet = new byte[len];
                    System.arraycopy(receivePacket.getData(), 0, packet, 0, len);
                    if (UnityPacket.isUnity(packet)) {
    					if (UnityPacket.isKeepAlive(packet)) {
    						// keep alive
    						App.bn.TrafficPrint(pre +"KEEP ALIVE RECEIVED", 0, 1);
    					} else if (UnityPacket.isUping(packet)) {
                            //blue node uping!
    						blueNode.setUping(true);    
    						App.bn.TrafficPrint(pre + "UPING RECEIVED", 1, 1);
                        } else if (UnityPacket.isDping(packet)) {
                            //blue node dping!
    						blueNode.setDping(true);    
    						App.bn.TrafficPrint(pre + "DPING RECEIVED", 1, 1);
                        } else if (UnityPacket.isShortRoutedAck(packet)) {
                        	//do something
                        	App.bn.TrafficPrint(pre + "SHORT ACK RECEIVED", 1, 1);
                        } else if (UnityPacket.isLongRoutedAck(packet)) {
    						try {
    							App.bn.manager.offer(packet); 
    							App.bn.TrafficPrint(pre + "ACK-> "+UnityPacket.getDestAddress(packet)+" RECEIVED", 2, 1);
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
    					} else if (UnityPacket.isMessage(packet)) {
    						App.bn.manager.offer(packet); 
    						App.bn.TrafficPrint(pre + "MESSAGE RECEIVED", 3, 1);
    					}        				
                    } else if (IPv4Packet.isIPv4(packet)){
                        App.bn.manager.offer(packet);                        
                    }
                    
                    if (!didTrigger) {
                    	if (App.bn.gui) {
                    		MainWindow.jCheckBox7.setSelected(true);
                        }
                    	didTrigger = true;
                    }
                } else {
                    System.out.println(pre + "MAXIMUM LENGTH EXCEDED");
                }
            } catch (java.net.SocketException ex1) {
                App.bn.ConsolePrint(pre + "SOCKET ERROR");
            } catch (IOException ex) {
                App.bn.ConsolePrint(pre + "IO ERROR");
            }
        }
    	socket.close();
    	if (isServer) {
    		App.bn.UDPports.releasePort(serverPort);
    	}
        App.bn.ConsolePrint(pre + "ENDED");        
    }

	public void kill() {
    	kill.set(true);
    	socket.close();             
    }      
	
	private void buildServer() {
    	socket = null;
        try {
            socket = new DatagramSocket(serverPort);
        } catch (java.net.BindException ex) {
        	App.bn.ConsolePrint(pre + "PORT ALLREADY IN USE");
            ex.printStackTrace();
            return;
        } catch (SocketException ex) {
            ex.printStackTrace();
            return;
        }
	}
	
	private void buildClient() {
		socket = null;
        try {
            socket = new DatagramSocket();
        } catch (java.net.BindException ex) {
            App.bn.ConsolePrint(pre + "PORT ALLREADY IN USE, EXITING");
            return;
        } catch (SocketException ex) {
            Logger.getLogger(RedReceive.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        byte[] sendData = "FISH PACKET".getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, blueNodePhAddress, portToReceive);
        try {
            for (int i = 0; i < 3; i++) {
                socket.send(sendPacket);
            }
        } catch (java.net.SocketException ex1) {
            App.bn.TrafficPrint("FISH PACKET SEND ERROR",3,1);
            return;
        } catch (IOException ex) {
            App.bn.TrafficPrint("FISH PACKET SEND ERROR",3,1);
            ex.printStackTrace();
            return;
        }
        App.bn.TrafficPrint("FISH PACKET",3,1);
	}
}
