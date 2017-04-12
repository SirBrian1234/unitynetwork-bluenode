package kostiskag.unitynetwork.bluenode.functions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class Packet {

    public static byte[] GetPacket(byte[] frame) {        
        byte[] packet = new byte[frame.length-14];        
        System.arraycopy(frame, 14, packet, 0, packet.length);
        return packet;
    }

    public static byte[] MakePacket(byte[] payload, InetAddress source, InetAddress dest, boolean type) {
        byte[] version = null;

        if (type == true) {
            version = new byte[]{0x00};
        } else {
            version = new byte[]{0x01};
        }

        byte[] packet = new byte[version.length + source.getAddress().length + dest.getAddress().length + payload.length];
        System.arraycopy(version, 0, packet, 0, version.length);
        System.arraycopy(source.getAddress(), 0, packet, 1, source.getAddress().length);
        System.arraycopy(dest.getAddress(), 0, packet, 5, dest.getAddress().length);
        System.arraycopy(payload, 0, packet, 9, payload.length);
        return packet;
    }

    public static String getVersion(byte[] packet) {
        String version = Integer.toHexString(packet[0]);
        return version;
    }

    public static InetAddress getSourceAddress(byte[] packet) {
        byte[] addr = new byte[4];
        for (int i = 0; i < 4; i++) {
            addr[i] = packet[12 + i];
        }
        InetAddress source = null;
        try {
            source = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Packet.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return source;
    }

    public static InetAddress getDestAddress(byte[] packet) {
        byte[] addr = new byte[4];
        for (int i = 0; i < 4; i++) {
            addr[i] = packet[16 + i];
        }
        InetAddress dest = null;
        try {
            dest = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Packet.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return dest;
    }

    public static byte[] getPayloadU(byte[] packet) {
        byte[] payload = new byte[packet.length - 9];
        System.arraycopy(packet, 9, payload, 0, packet.length - 9);
        return payload;
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
