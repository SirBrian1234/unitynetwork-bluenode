package kostiskag.unitynetwork.bluenode.Routing.Packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class IpDatagramm {
                           
    public static String getVersion(byte[] packet) {
        String version = Integer.toHexString(packet[0]);
        return version;
    }    
    
    public static InetAddress getSourceAddress(byte[] packet) {
        byte[] addr = new byte[4];
        for (int i=0; i<4; i++){
             addr[i] = packet[12+i];             
        }
        InetAddress source = null;
        try {
            source = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(IpDatagramm.class.getName()).log(Level.SEVERE, null, ex);
        }                
        return source;
    }

    public static InetAddress getDestAddress(byte[] packet) {
        byte[] addr = new byte[4];
        for (int i=0; i<4; i++){
             addr[i] = packet[16+i];             
        }
        InetAddress dest = null;
        try {
            dest = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(IpDatagramm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dest;
    }
    
    public static long checksum(byte[] buf, int length) {
        int i = 0;
        long sum = 0;
        while (length > 0) {
            sum += (buf[i++] & 0xff) << 8;
            if ((--length) == 0) {
                break;
            }
            sum += (buf[i++] & 0xff);
            --length;
        }

        return (~((sum & 0xFFFF) + (sum >> 16))) & 0xFFFF;
    }      
}

