package kostiskag.unitynetwork.bluenode.functions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackingRedNodeFunctions;

/**
 *
 * @author kostis
 */
public class IpAddrFunctions {

    public static String numberTo10ipAddr(int vaddressNum) {
        if (vaddressNum > 0) {
	    	byte[] networkpart = new byte[]{0x0a};
	        int hostnum = vaddressNum + App.systemReservedAddressNumber;
	
	        byte[] hostpart = new byte[]{
	            (byte) ((hostnum) >>> 16),
	            (byte) ((hostnum) >>> 8),
	            (byte) (hostnum)};
	
	        byte[] address = new byte[4];
	        System.arraycopy(networkpart, 0, address, 0, networkpart.length);
	        System.arraycopy(hostpart, 0, address, 1, hostpart.length);
	        try {
	            return InetAddress.getByAddress(address).getHostAddress();
	        } catch (UnknownHostException ex) {
	            Logger.getLogger(TrackingRedNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
	            return null;
	        }
        } else {
        	return null;
        }
    }

    public static int _10ipAddrToNumber(String vaddress) {
        InetAddress addr = null;
        try {
            addr = InetAddress.getByName(vaddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(TrackingRedNodeFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] address = addr.getAddress();
        byte[] hostpart = new byte[3];
        System.arraycopy(address, 1, hostpart, 0, 3);
        int hostnum = 0;
        for (int i = 0; i < hostpart.length; i++) {
            hostnum = (hostnum << 8) + (hostpart[i] & 0xff);
        }
        hostnum = hostnum - App.systemReservedAddressNumber;
        return hostnum;
    }
        
}
