package kostiskag.unitynetwork.bluenode.Routing.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class IPv4Packet {
                           
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
            Logger.getLogger(IPv4Packet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(IPv4Packet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dest;
    }
    
    public static byte[] getPayload(byte[] frame) {        
        byte[] packet = new byte[frame.length-14];        
        System.arraycopy(frame, 14, packet, 0, packet.length);
        return packet;
    }
}

