package kostiskag.unitynetwork.bluenode.socket.blueNodeService;

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;

/**
 *
 * @author kostis
 */
public class BlueNodeFunctions {

    private static String pre = "^Blue Node Functions";
    
    static void associate(String name, Socket connectionSocket, BufferedReader socketReader, PrintWriter socketWriter) {        
    	App.bn.ConsolePrint(pre + "STARTING A BLUE AUTH AT " + Thread.currentThread().getName());
    	InetAddress phAddress;
    	String phAddressStr;
    	int authPort = 0;
    	String[] args;
        
        if (App.bn.name.equals(name)) {
        	socketWriter.println("ERROR");
        	return;
        } else if (App.bn.blueNodesTable.checkBlueNode(name)) {
        	socketWriter.println("ERROR");
        	return;
        } else {
        	//tracker lookup
        	TrackerClient tr = new TrackerClient();
        	args = tr.getPhysicalBn(name);
        	authPort = Integer.parseInt(args[1]);
        	
        	phAddress = connectionSocket.getInetAddress();
            phAddressStr = phAddress.getHostAddress(); 
            
            if (args[0].equals("OFFLINE")) {
            	socketWriter.println("ERROR");
            	return;
            } else if (!args[0].equals(phAddressStr)) {
            	socketWriter.println("ERROR");
            	return;
            }
        }
        App.bn.ConsolePrint(pre + "BN "+name+" IS VALID AT ADDR "+phAddressStr+":"+authPort);
        
    	//create obj first in order to open its threads
        BlueNodeInstance bn = null;
		try {
			bn = new BlueNodeInstance(name, phAddressStr, authPort);			
		} catch (Exception e) {
			e.printStackTrace();
			bn.killtasks();
			socketWriter.println("ERROR");
			return;
		}
        
        socketWriter.println("ASSOSIATING "+bn.getUpport()+" "+bn.getDownport());        
		App.bn.ConsolePrint(pre + "remote auth port "+bn.getRemoteAuthPort()+" upport "+bn.getUpport()+" downport "+bn.getDownport());
    	
    	try {
			App.bn.blueNodesTable.leaseBn(bn);
			App.bn.ConsolePrint(pre + "LEASED REMOTE BN "+name);
		} catch (Exception e) {
			e.printStackTrace();
			bn.killtasks();
		}            	        	    		     	       
    }

    /**
     * A Bn has requested to tell him if we can receive his packets
     */
    static void Uping(BlueNodeInstance bn, PrintWriter outputWriter) {
    	bn.setUping(false);
    	outputWriter.println("SET");
    	
    	try {
            sleep(2000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    	
        if (bn.getUPing()) {
            outputWriter.println("OK");
        } else {
            outputWriter.println("FAILED");
        }
    }

    /**
     * A Bn has requested to get some packets. That's all!
     */
    static void Dping(BlueNodeInstance bn, PrintWriter outputWriter) {
        byte[] payload = ("00003 "+App.bn.name+" [DPING PACKET]").getBytes();
        byte[] data = UnityPacket.buildPacket(payload, null, null, 0);
        try {
        	for (int i=0; i<3; i++) {
        		bn.getQueueMan().offer(data);
        		sleep(200);
        	}			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    static void releaseBn(String BlueNodeName, PrintWriter outputWriter) {
        try {
			App.bn.blueNodesTable.releaseBn(BlueNodeName);
		} catch (Exception e) {
			
		}        
    }
    
    public static void getLRNs(BlueNodeInstance bn, BufferedReader socketReader, PrintWriter socketWriter) {
		GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, socketWriter);
	}
    
    static void giveLRNs(PrintWriter outputWriter) {
    	GlobalSocketFunctions.sendLocalRedNodes(outputWriter);
    }

    static void exchangeRNs(BlueNodeInstance bn, BufferedReader inFromClient, PrintWriter outputWriter) {
    	GlobalSocketFunctions.sendLocalRedNodes(outputWriter);	
    	GlobalSocketFunctions.getRemoteRedNodes(bn, inFromClient, outputWriter);                
    }

    static void getLocalRnVaddressByHostname(String hostname, PrintWriter outputWriter) {
        if (App.bn.localRedNodesTable.checkOnlineByHostname(hostname)) {
            outputWriter.println(App.bn.localRedNodesTable.getRedNodeInstanceByHn(hostname).getVaddress());
        } else {
            outputWriter.println("OFFLINE");
        }
    }
    
    static void getLocalRnHostnameByVaddress(String vaddress, PrintWriter outputWriter) {
        if (App.bn.localRedNodesTable.checkOnlineByVaddress(vaddress)) {
        	outputWriter.println(App.bn.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).getHostname());
        } else {
            outputWriter.println("OFFLINE");
        }
    }

    static void getFeedReturnRoute(BlueNodeInstance bn, String hostname, String vaddress, PrintWriter outputWriter) {        
        try {
			App.bn.blueNodesTable.leaseRRn(bn, hostname, vaddress);
		} catch (Exception e) {
			
		}
        outputWriter.println("OK");        
    }

	public static void getRRNToBeReleasedByHn(BlueNodeInstance bn, String hostname, PrintWriter socketWriter) {
		try {
			bn.table.releaseByHostname(hostname);
		} catch (Exception e) {
			
		}
		socketWriter.println("OK");  
	}
	
	public static void getRRNToBeReleasedByVaddr(BlueNodeInstance bn, String vaddress, PrintWriter socketWriter) {
		try {
			bn.table.releaseByVaddr(vaddress);
		} catch (Exception e) {
			
		}
		socketWriter.println("OK");  
	}

	public static void check(String blueNodeName, PrintWriter socketWriter) {
		//if associated reset idleTime and update timestamp as well
		if (App.bn.blueNodesTable.checkBlueNode(blueNodeName)) {
			try {
				BlueNodeInstance bn = App.bn.blueNodesTable.getBlueNodeInstanceByName(blueNodeName);
				bn.resetIdleTime();
				bn.updateTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		socketWriter.println("OK");
	}
}	
