package kostiskag.unitynetwork.bluenode.Routing;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import kostiskag.unitynetwork.bluenode.functions.HashFunctions;

/**
 *
 * @author kostis
 */
public class IpPacket {
    
	public static byte[] MakeIpPacket(byte[] payload, byte[] sourceIp, byte[] destIp, byte[] protocolType) {
		return null;
	}
	
	public static byte[] MakeUDPDatagramm(byte[] payload, int sourcePort, int destPort) {
		//make the new datagramm
		byte[] datagramm = new byte[payload.length+8];
		//copy the payload
		System.arraycopy(payload, 0, datagramm, 8, payload.length);
		
		//calculate header
		//header is 8 bytes
		byte[] header = new byte[8];
		//2 bytes source
		byte[] source = HashFunctions.UnsignedIntTo2Bytes(sourcePort);
		System.arraycopy(source, 0, header, 0, 2);
		//2 btes destination
		byte[] dest = HashFunctions.UnsignedIntTo2Bytes(destPort);
		System.arraycopy(dest, 0, header, 2, 2);
		//2 bytes len
		byte len[] = HashFunctions.UnsignedIntTo2Bytes(datagramm.length);
		System.arraycopy(dest, 0, header, 4, 2);
		
		//copy header to datagramm
		System.arraycopy(header, 0, datagramm, 0, header.length);
		
		//calculate checksum
		//leave it unsigned for now
		
		return datagramm;
	}
	
    public static byte[] MakeUPacket(byte[] payload, InetAddress source, InetAddress dest,boolean type) {        
        byte[] version=null;
        
        if (type == true){
           version = new byte[] {0x00};                
        }
        else {
            version = new byte[] {0x01};                
        }         
        if (source == null){
            try {
                source = InetAddress.getByName("0.0.0.0");
            } catch (UnknownHostException ex) {
                Logger.getLogger(IpPacket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (dest == null){
            try {
                dest = InetAddress.getByName("0.0.0.0");
            } catch (UnknownHostException ex) {
                Logger.getLogger(IpPacket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        byte[] packet = new byte[version.length+source.getAddress().length+dest.getAddress().length+payload.length];        
        System.arraycopy(version,0,packet,0,version.length);
        System.arraycopy(source.getAddress(),0,packet,1,source.getAddress().length);
        System.arraycopy(source.getAddress(),0,packet,5,source.getAddress().length);
        System.arraycopy(payload,0,packet,9,payload.length);        
        return packet;
    }
    

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
            Logger.getLogger(IpPacket.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(IpPacket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dest;
    }
    
    public static InetAddress getUSourceAddress(byte[] packet) {
        byte[] addr = new byte[4];
        for (int i=0; i<4; i++){
             addr[i] = packet[1+i];             
        }
        InetAddress source = null;
        try {
            source = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(IpPacket.class.getName()).log(Level.SEVERE, null, ex);
        }                
        return source;
    }

    public static InetAddress getUDestAddress(byte[] packet) {
        byte[] addr = new byte[4];
        for (int i=0; i<4; i++){
             addr[i] = packet[5+i];             
        }
        InetAddress dest = null;
        try {
            dest = InetAddress.getByAddress(addr);
        } catch (UnknownHostException ex) {
            Logger.getLogger(IpPacket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dest;
    }
    
    public static byte[] getPayloadU(byte[] packet) {
        byte[] payload = new byte[packet.length-9];
        System.arraycopy(packet, 9, payload, 0, packet.length-9);
        return payload;        
    }
    
    //TCP, UDP, etc.
    public static String getIpPacketPayloadType(byte[] packet) {
    	byte[] type = new byte[1];
    	System.arraycopy(packet, 9, type, 0, 1);
    	return HashFunctions.bytesToHexStr(type);
    }
    
    //what's inside the ip packet
    public static byte[] getIpPacketPayload(byte[] packet) {
        byte[] payload = new byte[packet.length-20];
        System.arraycopy(packet, 20, payload, 0, packet.length-20);
        return payload;        
    }    
    
    public static int getUDPsourcePort(byte[] datagramm) {
    	byte[] port = new byte[2];
    	port[0] = datagramm[0];
    	port[1] = datagramm[1];
    	return HashFunctions.bytesToUnsignedInt(port);
    }
    
    public static int getUDPdestPort(byte[] datagramm) {
    	byte[] port = new byte[2];
    	port[0] = datagramm[2];
    	port[1] = datagramm[3];
    	return HashFunctions.bytesToUnsignedInt(port);
    }
    
    //what's inside a udp packet
    public static byte[] getUDPpayload(byte[] datagramm) {
    	byte[] payload = new byte[datagramm.length-8];
        System.arraycopy(datagramm, 8, payload, 0, datagramm.length-8);
        return payload;
    }    
}

