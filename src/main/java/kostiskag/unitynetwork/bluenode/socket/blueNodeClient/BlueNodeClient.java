package kostiskag.unitynetwork.bluenode.socket.blueNodeClient;

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

public class BlueNodeClient {

	public static String pre = "^CLIENT ";
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
		String[] args = TCPSocketFunctions.sendData("CHECK", socketWriter, socketReader);
		closeConnection();
		if (args[0].equals("OK")) {			
			return true;
		}		
		return false;
	}
	
	public void associateClient() throws Exception {
		System.out.println(connected);
		//ports
		int remoteAuthPort = 0;
		int downport = 0;
		int upport = 0;
		
		//lease
        String[] args = TCPSocketFunctions.sendData("ASSOCIATE", socketWriter, socketReader);
        if (!args[0].equals("BLUE_NODE_ALLREADY_IN_LIST")) {
            remoteAuthPort = Integer.parseInt(args[1]);
        	downport = Integer.parseInt(args[2]);
            upport = Integer.parseInt(args[3]);
            TCPSocketFunctions.sendFinalData(App.bn.authPort+" ",socketWriter);
            App.bn.ConsolePrint(pre + "remote authport "+remoteAuthPort+" upport " + upport + " downport " + downport);
        } else {            	
            App.bn.ConsolePrint(pre + "BLUE_NODE_ALLREADY_IN_LIST");
            closeConnection();
            throw new Exception(pre+"BLUE_NODE_ALLREADY_IN_LIST");                
        }
        
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
		
	public int UPing() {
		if (bn != null) {
			TCPSocketFunctions.sendFinalData("UPING", socketWriter);
	        byte[] payload = ("00002 " + App.bn.name + " [UPING PACKET]").getBytes();
	        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
	        bn.getQueueMan().offer(data);
	        
	        try {
	            sleep(1700);
	        } catch (InterruptedException ex) {
	            ex.printStackTrace();
	        }
	        
	        String[] args = TCPSocketFunctions.readData(socketReader);
	
	        closeConnection();
	        if (args[1].equals("OK")) {
	            return 1;
	        } else {
	            return 0;
	        }
		}
		closeConnection();
		return -1;
	}
	
	public int DPing() {
		if (bn != null) {
			App.bn.dping = false;
	        TCPSocketFunctions.sendData("DPING", socketWriter, socketReader);
	        TCPSocketFunctions.connectionClose(sessionSocket);
	        
	        try {
	            sleep(2000);
	        } catch (InterruptedException ex) {
	            ex.printStackTrace();
	        }
	        
	        closeConnection();
	        if (App.bn.dping) {
	            return 1;
	        } else {
	            return 0;
	        }
		}
		closeConnection();
		return -1;
	}
	
	public void removeThisBlueNodesProjection() {
		TCPSocketFunctions.sendFinalData("RELEASE", socketWriter);        
        closeConnection();
	}
	
	public int getRemoteRedNodes() {
		if (bn != null) {
			String[] args = TCPSocketFunctions.sendData("GET_RED_NODES", socketWriter, socketReader);
			int count = Integer.parseInt(args[1]);
	        for (int i = 0; i < count; i++) {
	            args = TCPSocketFunctions.readData(socketReader);
	            if (App.bn.blueNodesTable.checkRemoteRedNodeByHostname(args[0])) {
	                bn.table.lease(args[0], args[1]);
	            } else {
	                App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
	            }
	        }
	        closeConnection();
	        return 1;
		}
		closeConnection();
		return -1;
	}
	
	public int exchangeRedNodes() {
		if (bn != null) {
			String[] args = TCPSocketFunctions.sendData("EXCHANGE_RED_NODES", socketWriter, socketReader);
	        int count = Integer.parseInt(args[1]);
	        for (int i = 0; i < count; i++) {
	            args = TCPSocketFunctions.readData(socketReader);
	            if (App.bn.blueNodesTable.checkRemoteRedNodeByHostname(args[0])) {
	                bn.table.lease(args[0], args[1]);
	            } else {
	                App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
	            }
	        }
	        TCPSocketFunctions.readData(socketReader);
	        GlobalSocketFunctions.sendLocalRedNodes(socketWriter);
	        closeConnection();
	        return 1;
		}
		closeConnection();
		return -1;
	}
	
	public String getRedNodeVaddressByHostname(String hostname) {
		String[] args = TCPSocketFunctions.sendData("GET_RED_HOSTNAME" + hostname, socketWriter, socketReader);
        closeConnection();
		if (args[0].equals("ONLINE")) {
            return args[1];
        } else {
            return null;
        }
	}

	public String getRedNodeHostnameByVaddress(String vaddress) {
		String[] args = TCPSocketFunctions.sendData("GET_RED_VADDRESS"+vaddress, socketWriter, socketReader);
        closeConnection();
		if (args[0].equals("ONLINE")) {
            return args[1];
        } else {
            return null;
        }
	}
	
	public void removeRedNodeProjectionByHn(String hostname) {
		TCPSocketFunctions.sendData("RELEASE_REMOTE_REDNODE_BY_HN"+hostname, socketWriter, socketReader);     
		closeConnection();
	}
	
	public void removeRedNodeProjectionByVaddr(String vaddress) {
		TCPSocketFunctions.sendData("RELEASE_REMOTE_REDNODE_BY_VADDRESS"+vaddress, socketWriter, socketReader);     
		closeConnection();
	}
	
	public void feedReturnRoute(String hostname, String vaddress) {
		TCPSocketFunctions.sendData("LEASE_REMOTE_REDNODE "+hostname+" "+vaddress, socketWriter, socketReader);        
		closeConnection();        
	}
}
