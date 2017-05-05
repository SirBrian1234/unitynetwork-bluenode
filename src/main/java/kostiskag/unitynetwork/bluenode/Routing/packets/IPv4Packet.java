package kostiskag.unitynetwork.bluenode.Routing.packets;

import java.net.InetAddress;

import kostiskag.unitynetwork.bluenode.functions.HashFunctions;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class IPv4Packet {
	
	public static final int IPversion = 69;
	public static final int MIN_LEN = 20;
	
    public static boolean isIPv4(byte[] packet) {
    	if (packet != null) {
	    	if (packet.length >= MIN_LEN) {
		        int version = HashFunctions.bytesToUnsignedInt(new byte[] {packet[0]});
		        if (version == IPversion) {
		        	return true;
		        }
	    	}
    	}
        return false;
    }    
    
    public static InetAddress getSourceAddress(byte[] packet) throws Exception {
        if (isIPv4(packet)) {
	    	byte[] addr = new byte[4];
	        for (int i=0; i<4; i++){
	             addr[i] = packet[12+i];             
	        }
	        return InetAddress.getByAddress(addr);
        }
        throw new Exception ("This is not an IPv4 packet.");
    }

    public static InetAddress getDestAddress(byte[] packet) throws Exception {
    	if (isIPv4(packet)) {
	    	byte[] addr = new byte[4];
	        for (int i=0; i<4; i++){
	             addr[i] = packet[16+i];             
	        }
	        return InetAddress.getByAddress(addr);
    	}
    	throw new Exception ("This is not an IPv4 packet.");
    }
}

