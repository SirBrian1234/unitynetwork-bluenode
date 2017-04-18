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

	public static String pre = "^CLIENT ";
	private final String name;
	private final InetAddress phAddress;
	private final int authPort;
	private final BlueNodeInstance bn;
	private boolean connected = false;
	Socket socket;
	BufferedReader inputReader;
	PrintWriter outputWriter;

	public BlueNodeClient(BlueNodeInstance bn) {
		this.bn = bn;
		this.name = bn.getName();
		this.phAddress = bn.getPhaddress();
		this.authPort = bn.getRemoteAuthPort();
		initConnection();
	}

	public BlueNodeClient(String name, String phAddressStr, int authPort) {
		this.name = name;
		this.phAddress = TCPSocketFunctions.getAddress(phAddressStr);
		this.authPort = authPort;	
		this.bn = null;
		initConnection();
	}

	private void initConnection() {
		socket = TCPSocketFunctions.absoluteConnect(phAddress, authPort);
		if (socket == null) {
			return;
		}

		BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
		PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);

		String[] args = null;
		args = TCPSocketFunctions.readData(inputReader);

		if (!name.equals(args[1])) {
			TCPSocketFunctions.connectionClose(socket);
			return;
		}

		args = TCPSocketFunctions.sendData("BLUENODE " + App.bn.name, outputWriter, inputReader);
		if (args[0].equals("OK")) {
			connected = true;
		}
	}

	private void closeConnection() {
		TCPSocketFunctions.connectionClose(socket);
		connected = false;
	}

	public boolean checkBlueNode() {
		String[] args = TCPSocketFunctions.sendData("CHECK", outputWriter, inputReader);
		if (args[0].equals("OK")) {
			closeConnection();
			return true;
		}
		closeConnection();
		return false;
	}
	
	public void removeThisBlueNodesProjection() {
		TCPSocketFunctions.sendFinalData("RELEASE", outputWriter);        
        closeConnection();
	}
	
	public int UPing() {
		if (bn != null) {
			TCPSocketFunctions.sendFinalData("UPING ", outputWriter);
	        byte[] payload = ("00002 " + App.bn.name + " [UPING PACKET]").getBytes();
	        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
	        bn.getQueueMan().offer(data);
	        
	        try {
	            sleep(1700);
	        } catch (InterruptedException ex) {
	            ex.printStackTrace();
	        }
	        
	        String[] args = TCPSocketFunctions.readData(inputReader);
	
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
	        TCPSocketFunctions.sendData("DPING ", outputWriter, inputReader);
	        TCPSocketFunctions.connectionClose(socket);
	        
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
	
	public String getRedNodeVaddressByHostname(String hostname) {
		String[] args = TCPSocketFunctions.sendData("GET_RED_HOSTNAME " + hostname, outputWriter, inputReader);
        closeConnection();
		if (args[0].equals("ONLINE")) {
            return args[1];
        } else {
            return null;
        }
	}

	public String getRedNodeHostnameByVaddress(String vaddress) {
		String[] args = TCPSocketFunctions.sendData("GET_RED_VADDRESS "+vaddress, outputWriter, inputReader);
        closeConnection();
		if (args[0].equals("ONLINE")) {
            return args[1];
        } else {
            return null;
        }
	}
	
	public boolean feedReturnRoute(String hostname, String vaddress) {
		String[] args = TCPSocketFunctions.sendData("FEED_RETURN_ROUTE "+hostname+" "+vaddress, outputWriter, inputReader);        
		closeConnection();
        if (args[0].equals("OK")) {
            return true;
        } else {
            return false;
        }
	}
	
	public void removeRedNodeProjectionByHn(String hostname) {
		String[] args = TCPSocketFunctions.sendData("RELEASE_REMOTE_REDNODE_BY_HN "+hostname, outputWriter, inputReader);     
		closeConnection();
	}
	
	public void removeRedNodeProjectionByVaddr(String vaddress) {
		String[] args = TCPSocketFunctions.sendData("RELEASE_REMOTE_REDNODE_BY_VADDRESS "+vaddress, outputWriter, inputReader);     
		closeConnection();
	}
	
	public int getRemoteRedNodes() {
		if (bn != null) {
			String[] args = TCPSocketFunctions.sendData("GET_RED_NODES ", outputWriter, inputReader);
			int count = Integer.parseInt(args[1]);
	        for (int i = 0; i < count; i++) {
	            args = TCPSocketFunctions.readData(inputReader);
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
			String[] args = TCPSocketFunctions.sendData("EXCHANGE_RED_NODES ", outputWriter, inputReader);
	        int count = Integer.parseInt(args[1]);
	        for (int i = 0; i < count; i++) {
	            args = TCPSocketFunctions.readData(inputReader);
	            if (App.bn.blueNodesTable.checkRemoteRedNodeByHostname(args[0])) {
	                bn.table.lease(args[0], args[1]);
	            } else {
	                App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
	            }
	        }
	        TCPSocketFunctions.readData(inputReader);
	        GlobalSocketFunctions.sendLocalRedNodes(outputWriter);
	        closeConnection();
	        return 1;
		}
		closeConnection();
		return -1;
	}
	
	public static void addRemoteBlueNode(String phAddress, int authPort, String AuthHostname, boolean full) {
    	//leasing
    	BlueNodeInstance node;
		try {
			node = new BlueNodeInstance(phAddress, authPort, AuthHostname, full);
			if (node.getStatus() > 0) {
	            App.bn.blueNodesTable.leaseBn(node);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}                        
    }

	
}
