/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.Routing.Packets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */
public class UnityDatagramm {

    //unity datagramm lives inside an envelope - the envelope defines its type but the datagramm defines its code
    public static byte[] MakeUDatagramm(int code, InetAddress source, InetAddress dest, byte[] payload) {
        byte Bcode = (byte) code;

        if (source == null) {
            try {
                source = InetAddress.getByName("0.0.0.0");
            } catch (UnknownHostException ex) {
                Logger.getLogger(IpDatagramm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (dest == null) {
            try {
                dest = InetAddress.getByName("0.0.0.0");
            } catch (UnknownHostException ex) {
                Logger.getLogger(IpDatagramm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        byte[] packet = new byte[1 + source.getAddress().length + dest.getAddress().length + payload.length];
        System.arraycopy(Bcode, 0, packet, 0, 1);
        System.arraycopy(source.getAddress(), 0, packet, 1, source.getAddress().length);
        System.arraycopy(source.getAddress(), 0, packet, 5, source.getAddress().length);
        System.arraycopy(payload, 0, packet, 9, payload.length);
        return packet;
    }
    
    public static int getCode(byte[] packet) {
        int version = (int) packet[0];
        return version;
    }    
    
    public static InetAddress getUSourceAddress(byte[] packet) {
        byte[] addr = new byte[4];
        for (int i = 0; i < 4; i++) {
            addr[i] = packet[1 + i];
        }
        InetAddress source = null;
        try {
            source = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(IpDatagramm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return source;
    }

    public static InetAddress getUDestAddress(byte[] packet) {
        byte[] addr = new byte[4];
        for (int i = 0; i < 4; i++) {
            addr[i] = packet[5 + i];
        }
        InetAddress dest = null;
        try {
            dest = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(IpDatagramm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dest;
    }
    
    public static byte[] getUPayload(byte[] packet) {
        byte[] payload = new byte[packet.length - 9];
        System.arraycopy(packet, 9, payload, 0, packet.length - 9);
        return payload;
    }

}
