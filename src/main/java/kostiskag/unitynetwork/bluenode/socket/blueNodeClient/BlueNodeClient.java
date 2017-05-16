package kostiskag.unitynetwork.bluenode.socket.blueNodeClient;

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.RunData.instances.RemoteRedNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

/**
 * TODO
 * 
 * @author Konstantinos Kagiampakis
 */
public class BlueNodeClient {

	public static String pre = "^BlueNodeClient ";
	private final String name;
	Socket sessionSocket;
	BufferedReader socketReader;
	PrintWriter socketWriter;
	private final String phAddressStr;
	private final InetAddress phAddress;
	private final int authPort;
	private final BlueNodeInstance bn;
	private boolean connected = false;

	public BlueNodeClient(BlueNodeInstance bn) {
		this.bn = bn;
		this.name = bn.getName();
		this.phAddressStr = bn.getPhAddressStr();
		this.phAddress = bn.getPhaddress();
		this.authPort = bn.getRemoteAuthPort();
		initConnection();
	}

	public BlueNodeClient(String name, String phAddressStr, int authPort) {
		this.name = name;
		this.phAddressStr = phAddressStr;
		this.phAddress = TCPSocketFunctions.getAddress(phAddressStr);
		this.authPort = authPort;	
		this.bn = null;
		initConnection();
	}
	
	public boolean isConnected() {
		return connected;
	}

	private void initConnection() {
		sessionSocket = TCPSocketFunctions.absoluteConnect(phAddress, authPort);
		if (sessionSocket == null) {
			return;
		}

		socketReader = TCPSocketFunctions.makeReadWriter(sessionSocket);
		socketWriter = TCPSocketFunctions.makeWriteWriter(sessionSocket);

		String[] args = null;
		args = TCPSocketFunctions.readData(socketReader);

		if (!name.equals(args[1])) {
			TCPSocketFunctions.connectionClose(sessionSocket);
			return;
		}

		args = TCPSocketFunctions.sendData("BLUENODE " + App.bn.name, socketWriter, socketReader);
		if (args[0].equals("OK")) {
			connected = true;
		}
	}

	private void closeConnection() {
		TCPSocketFunctions.connectionClose(sessionSocket);
		connected = false;
	}
	
	public boolean checkBlueNode() {
		if (connected) {
			String[] args = TCPSocketFunctions.sendData("CHECK", socketWriter, socketReader);
			closeConnection();
			if (args[0].equals("OK")) {			
				return true;
			}		
		}
		return false;
	}
	
	public void associateClient() throws Exception {
		if (connected) {
			//ports
			int downport = 0;
			int upport = 0;
			
			//lease
	        String[] args = TCPSocketFunctions.sendData("ASSOCIATE", socketWriter, socketReader);
	        if (args[0].equals("ERROR")) {
	        	App.bn.ConsolePrint(pre + "Connection error");
	            closeConnection();
	            throw new Exception(pre+"ERROR");  
	        } 
	        
	        downport = Integer.parseInt(args[1]);
            upport = Integer.parseInt(args[2]);
            App.bn.ConsolePrint(pre + " upport " + upport + " downport " + downport);
	        
	        //build the object
	    	BlueNodeInstance node;
			try {
				//node = new BlueNodeInstance(name, phAddressStr, authPort, upport, downport);	        
			} catch (Exception e) {
				e.printStackTrace();
				bn.killtasks();
				closeConnection();
				return;
			}       
			
			//lease to local bn table
			//App.bn.blueNodesTable.leaseBn(node);
			closeConnection();
			App.bn.ConsolePrint(pre + "LEASED REMOTE BN "+name);
		}
    }		
		
	public boolean uPing() {
		if (bn != null) {
			if (connected) {
				byte[] data = UnityPacket.buildUpingPacket();
		        TCPSocketFunctions.sendData("UPING", socketWriter, socketReader);
				//wait to get set
		        for (int i=0; i<3; i++) {
		        	bn.getSendQueue().offer(data);
		        	try {
						sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		        }
		        
		        String[] args = TCPSocketFunctions.readData(socketReader);	
		        closeConnection();
		        
		        if (args[0].equals("OK")) {
		            return true;
		        } else {
		            return false;
		        }
			}
		}
		return false;
	}
	
	/**
	 * I am telling YOU to send ME some packets to check if I can get them 
	 * 
	 * @return
	 */
	public boolean dPing() {
		if (bn != null) {
			if (connected) {
			    bn.setDping(false);
		        TCPSocketFunctions.sendFinalData("DPING", socketWriter);
		        closeConnection();
		        
		        try {
		            sleep(2000);
		        } catch (InterruptedException ex) {
		            ex.printStackTrace();
		        }
		        
		        if (bn.getDPing()) {
		            return true;
		        } else {
		            return false;
		        }
			}
		}		
		return false;
	}
	
	public void removeThisBlueNodesProjection() {
		if (bn != null) {
			if (connected) {
				TCPSocketFunctions.sendFinalData("RELEASE", socketWriter);        
		        closeConnection();
			}
		}		
	}
	
	public void getRemoteRedNodes() {
		if (bn != null) {
			if (connected) {
				TCPSocketFunctions.sendFinalData("GET_RED_NODES", socketWriter);   
				//GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, socketWriter);
				closeConnection();
			}
		}
	}
	
	/**
	 * The difference of this method from getRemoteRedNodes is that the former requests and
	 * then internally leases the returned results although this method requests but returns the 
	 * results as a built object.
	 * 
	 * @return a linked list with all the retrieved RemoteRedNodeInstance
	 */
	public LinkedList<RemoteRedNodeInstance> getRemoteRedNodesObj() {
		LinkedList<RemoteRedNodeInstance> fetched = new LinkedList<RemoteRedNodeInstance>();
		if (bn != null) {
			if (connected) {
				String[] args = TCPSocketFunctions.sendData("GET_RED_NODES", socketWriter, socketReader);        
		        int count = Integer.parseInt(args[1]);
		        for (int i = 0; i < count; i++) {
		            args = TCPSocketFunctions.readData(socketReader);
		            RemoteRedNodeInstance r =  new RemoteRedNodeInstance(args[0], args[1], bn);                    
		            fetched.add(r); 
		        }
		        closeConnection();
			}
		}
		return fetched;
	}
	
	public void giveLocalRedNodes() {
		if (bn != null) {
			if (connected) {
				TCPSocketFunctions.sendFinalData("GIVE_RED_NODES", socketWriter);   
				//GlobalSocketFunctions.sendLocalRedNodes(socketWriter);
				closeConnection();
			}
		}
	}
	
	public void exchangeRedNodes() {
		if (bn != null) {
			if (connected) {
				TCPSocketFunctions.sendFinalData("EXCHANGE_RED_NODES", socketWriter);
		        //GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, socketWriter);
		        //GlobalSocketFunctions.sendLocalRedNodes(socketWriter);	    
		        closeConnection();
			}
		}
	}
	
	public String getRedNodeVaddressByHostname(String hostname) {
		if (bn != null) {
			if (connected) {
				String[] args = TCPSocketFunctions.sendData("GET_RED_VADDRESS "+hostname, socketWriter, socketReader);
		        closeConnection();
				if (args[0].equals("OFFLINE")) {
		            return null;
		        } else {
		            return args[0];
		        }
			}
		}
		return null;
	}

	public String getRedNodeHostnameByVaddress(String vaddress) {
		if (bn != null) {
			if (connected) {
				String[] args = TCPSocketFunctions.sendData("GET_RED_HOSTNAME "+vaddress, socketWriter, socketReader);
		        closeConnection();
				if (args[0].equals("OFFLINE")) {
		            return null;
		        } else {
		            return args[0];
		        }
			}
		}		
		return null;
	}
	
	public void removeRedNodeProjectionByHn(String hostname) {
		if (bn != null) {
			if (connected) {
				TCPSocketFunctions.sendData("RELEASE_REMOTE_REDNODE_BY_HN "+hostname, socketWriter, socketReader);     
				closeConnection();
			}
		}		
	}
	
	public void removeRedNodeProjectionByVaddr(String vaddress) {
		if (bn != null) {
			if (connected) {
				TCPSocketFunctions.sendData("RELEASE_REMOTE_REDNODE_BY_VADDRESS "+vaddress, socketWriter, socketReader);     
				closeConnection();
			}
		}
	}
	
	public void feedReturnRoute(String hostname, String vaddress) {
		if (bn != null) {
			if (connected) {
				TCPSocketFunctions.sendData("LEASE_REMOTE_REDNODE "+hostname+" "+vaddress, socketWriter, socketReader);        
				closeConnection(); 
			}
		}
	}
}
