package kostiskag.unitynetwork.bluenode.socket.trackClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

public class TrackerClient {

	private final String pre = "^TrackerClient ";
	private final String name;
	private final InetAddress addr;
	private final int port;
	private final Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private boolean connected = false;

	public TrackerClient() {
		this.name = App.bn.name;
		this.addr = TCPSocketFunctions.getAddress(App.bn.trackerAddress);
		this.port = App.bn.trackerPort;
		this.socket = TCPSocketFunctions.absoluteConnect(addr, port);
		if (socket == null) {
			return;
		}
		this.reader = TCPSocketFunctions.makeReadWriter(socket);
		this.writer = TCPSocketFunctions.makeWriteWriter(socket);
		String args[] = TCPSocketFunctions.readData(reader);
		args = TCPSocketFunctions.sendData("BLUENODE"+" "+name, writer, reader);

		if (args[0].equals("OK")) {
			connected = true;
		}
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	private void closeCon() {
		if (!socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean leaseBn(int authport) {
		if (connected) {
			String[] args = TCPSocketFunctions.sendData("LEASE"+" "+authport, writer, reader);
			closeCon();
			
			if (args[0].equals("LEASED")) {
				App.bn.echoAddress = args[1];
				App.bn.window.setEchoIpAddress(args[1]);
				App.bn.ConsolePrint(pre + "ECHO ADDRESS IS " + App.bn.echoAddress);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean releaseBn() {
		if (connected) {
			String[] args = TCPSocketFunctions.sendData("RELEASE", writer, reader);
			closeCon();
			
			if (args[0].equals("RELEASED")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	/**
	 * Lookups and retrieves a Blue Node's address based on its name.
	 * 
	 * @param BNHostname the target Blue Node's name
	 * @return returns a string array with two elements. The first is the IP address, the second is the auth port
	 */
	public String[] getPhysicalBn(String BNHostname) {
		if (connected) {
			String[] args = TCPSocketFunctions.sendData("GETPH"+" "+BNHostname, writer, reader);
			closeCon();
			if (!args[0].equals("NOT_FOUND")) {
				return args;
			} else {
				return null;
			}
		}
		return null;
	}

	/**
	 * Request permission from tracker to lease a Local Red Node over the calling Blue Node.
	 * 
	 * @param Hostname the local red node's hostname
	 * @param Username the hostname's user owner
	 * @param Password a hashed version of the user's password
	 * 
	 * @return On a successful authorize, returns a virtual address for the given hostname. 
	 * Otherwise returns an error message in a string format.
	 */
	public String leaseRn(String Hostname, String Username, String Password) {
		if (connected) {
	    	String[] args = TCPSocketFunctions.sendData("LEASE_RN"+" "+Hostname+" "+Username+" "+Password, writer, reader);
	    	closeCon();
	
	        if (args[0].equals("LEASED")) {
	            return args[1];
	        } else {
	            return args[0];
	        }
		}
		return null;
    }    

	/**
	 * Notifies the network with a Local red node's release.
	 * 
	 * @param hostname the local red node's name
	 */
    public void releaseRnByHostname(String hostname) {
    	if (connected) {
	        TCPSocketFunctions.sendFinalData("RELEASE_RN"+" "+hostname, writer);       
	        closeCon();
    	}
    }
    
    /**
     * Retrieves the name of the BlueNode in which the given remote red node hostname
     * is connected.
     * 
     * @param hostanme the remote red node's hostname
     * @return returns the blue node's name
     */
    public String checkRnOnlineByHostname(String hostanme) {
    	if (connected) {
	    	String[] args = TCPSocketFunctions.sendData("CHECK_RN"+" "+hostanme, writer, reader);
	        closeCon();
	        
	        if (args[0].equals("OFFLINE")) {
	            return null;
	        } else {
	            return args[1];
	        }
    	}
    	return null;
    }
    
    /**
     * Retrieves the name of the BlueNode in which the given remote red node with vaddress
     * is connected.
     * 
     * @param vaddress the remote red node's vaddress
     * @return returns the blue node's name
     */
    public String checkRnOnlineByVaddr(String vaddress) {            
    	if (connected) {
	        String[] args = TCPSocketFunctions.sendData("CHECK_RNA"+" "+vaddress, writer, reader);        
	        closeCon();
	        
	        if (args[0].equals("OFFLINE")) {
	            return null;
	        } else {
	            return args[1];
	        }        
    	}
    	return null;
    }
    
    //we need two new dns queries
    //return hostname
    public String nslookupByVaddr(String vaddress) {
    	if (connected) {
    		String[] args = TCPSocketFunctions.sendData("LOOKUP_V"+" "+vaddress, writer, reader);        
	        closeCon();
	        
	        if (args[0].equals("NOT_FOUND")) {
	            return null;
	        } else {
	            return args[0];
	        }       
    	}
    	return null;
    }
    
    //returns vaddress
    public String nslookupByHostname(String hostanme) {
    	if (connected) {
    		String[] args = TCPSocketFunctions.sendData("LOOKUP_H"+" "+hostanme, writer, reader);
	        closeCon();
	        
	        if (args[0].equals("NOT_FOUND")) {
	            return null;
	        } else {
	            return args[0];
	        }
    	}
    	return null;
    }
}
