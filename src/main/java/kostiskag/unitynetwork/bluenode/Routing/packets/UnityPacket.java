package kostiskag.unitynetwork.bluenode.Routing.packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class UnityPacket {

	public static byte[] buildPacket(byte[] payload, InetAddress source, InetAddress dest, int type) {
        byte[] version = null;

        if (type == 0) {
            version = new byte[]{0x00};
        } else if (type == 1) {
            version = new byte[]{0x01};
        } else {
            version = new byte[]{0x02};
        }

        byte[] packet = new byte[version.length + source.getAddress().length + dest.getAddress().length + payload.length];
        System.arraycopy(version, 0, packet, 0, version.length);
        System.arraycopy(source.getAddress(), 0, packet, 1, source.getAddress().length);
        System.arraycopy(dest.getAddress(), 0, packet, 5, dest.getAddress().length);
        System.arraycopy(payload, 0, packet, 9, payload.length);
        return packet;
    }
    
    public static int getCode(byte[] packet) {
        int version = (int) packet[0];
        return version;
    }    
    
    public static InetAddress getSourceAddress(byte[] packet) {
        byte[] addr = new byte[4];
        for (int i = 0; i < 4; i++) {
            addr[i] = packet[1 + i];
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
        for (int i = 0; i < 4; i++) {
            addr[i] = packet[5 + i];
        }
        InetAddress dest = null;
        try {
            dest = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(IPv4Packet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dest;
    }
    
    public static byte[] getPayload(byte[] packet) {
        byte[] payload = new byte[packet.length - 9];
        System.arraycopy(packet, 9, payload, 0, packet.length - 9);
        return payload;
    }

}
