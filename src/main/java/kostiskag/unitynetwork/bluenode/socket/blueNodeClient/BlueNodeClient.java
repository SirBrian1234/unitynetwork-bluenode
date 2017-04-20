package kostiskag.unitynetwork.bluenode.socket.blueNodeClient;

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

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
			return false;
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
				node = new BlueNodeInstance(name, phAddressStr, authPort, upport, downport);	        
			} catch (Exception e) {
				e.printStackTrace();
				bn.killtasks();
				closeConnection();
				return;
			}       
			
			//lease to local bn table
			App.bn.blueNodesTable.leaseBn(node);
			closeConnection();
			App.bn.ConsolePrint(pre + "LEASED REMOTE BN "+name);
		}
    }		
		
	public boolean uPing() {
		if (connected && bn != null) {
			TCPSocketFunctions.sendFinalData("UPING", socketWriter);
	        byte[] payload = ("00002 " + App.bn.name + " [UPING PACKET]").getBytes();
	        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
	        bn.getQueueMan().offer(data);
	        bn.getQueueMan().offer(data);
	        
	        try {
	            sleep(1700);
	        } catch (InterruptedException ex) {
	            ex.printStackTrace();
	        }
	        
	        String[] args = TCPSocketFunctions.readData(socketReader);
	
	        closeConnection();
	        if (args[1].equals("OK")) {
	            return true;
	        } else {
	            return false;
	        }
		}
		closeConnection();
		return false;
	}
	
	public boolean dPing() {
		if (connected && bn != null) {
		    bn.setDping(false);
	        TCPSocketFunctions.sendData("DPING", socketWriter, socketReader);
	        TCPSocketFunctions.connectionClose(sessionSocket);
	        
	        try {
	            sleep(2000);
	        } catch (InterruptedException ex) {
	            ex.printStackTrace();
	        }
	        
	        closeConnection();
	        if (bn.getDPing()) {
	            return true;
	        } else {
	            return false;
	        }
		}
		closeConnection();
		return false;
	}
	
	public void removeThisBlueNodesProjection() {
		if (connected && bn != null) {
			TCPSocketFunctions.sendFinalData("RELEASE", socketWriter);        
	        closeConnection();
		}
		closeConnection();
	}
	
	public void getRemoteRedNodes() {
		if (bn != null && connected) {
			TCPSocketFunctions.sendFinalData("GET_RED_NODES", socketWriter);   
			GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, socketWriter);
		}
		closeConnection();
	}
	
	public void giveLocalRedNodes() {
		if (bn != null && connected) {
			TCPSocketFunctions.sendFinalData("GIVE_RED_NODES", socketWriter);   
			GlobalSocketFunctions.sendLocalRedNodes(socketWriter);
		}
		closeConnection();
	}
	
	public void exchangeRedNodes() {
		if (bn != null && connected) {
			TCPSocketFunctions.sendFinalData("EXCHANGE_RED_NODES", socketWriter);
	        GlobalSocketFunctions.getRemoteRedNodes(bn, socketReader, socketWriter);
	        GlobalSocketFunctions.sendLocalRedNodes(socketWriter);	        
		}
		closeConnection();
	}
	
	public String getRedNodeVaddressByHostname(String hostname) {
		if (bn != null && connected) {
			String[] args = TCPSocketFunctions.sendData("GET_RED_VADDRESS "+hostname, socketWriter, socketReader);
	        closeConnection();
			if (args[0].equals("OFFLINE")) {
	            return null;
	        } else {
	            return args[0];
	        }
		}
		closeConnection();
		return null;
	}

	public String getRedNodeHostnameByVaddress(String vaddress) {
		if (bn != null && connected) {
			String[] args = TCPSocketFunctions.sendData("GET_RED_HOSTNAME "+vaddress, socketWriter, socketReader);
	        closeConnection();
			if (args[0].equals("OFFLINE")) {
	            return null;
	        } else {
	            return args[0];
	        }
		}
		closeConnection();
		return null;
	}
	
	public void removeRedNodeProjectionByHn(String hostname) {
		if (bn != null && connected) {
			TCPSocketFunctions.sendData("RELEASE_REMOTE_REDNODE_BY_HN "+hostname, socketWriter, socketReader);     
			closeConnection();
		}
		closeConnection();
	}
	
	public void removeRedNodeProjectionByVaddr(String vaddress) {
		if (bn != null && connected) {
			TCPSocketFunctions.sendData("RELEASE_REMOTE_REDNODE_BY_VADDRESS "+vaddress, socketWriter, socketReader);     
			closeConnection();
		}
		closeConnection();
	}
	
	public void feedReturnRoute(String hostname, String vaddress) {
		if (bn != null && connected) {
			TCPSocketFunctions.sendData("LEASE_REMOTE_REDNODE "+hostname+" "+vaddress, socketWriter, socketReader);        
			closeConnection();        
		}
		closeConnection();
	}
}
