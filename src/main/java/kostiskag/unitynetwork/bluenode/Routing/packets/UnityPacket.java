package kostiskag.unitynetwork.bluenode.Routing.packets;

import org.kostiskag.unitynetwork.common.utilities.HashUtilities;

import java.net.InetAddress;

/**
 * The unity packet is a payload carried out with UDP in order to 
 * send or receive system and synchronization messages between the nodes appart from
 * the exchanging IPv4 packet traffic.
 * 
 * A unity packet's first byte is 0x00 as compared to an IPv4 packet which is 0x45.
 * After the version byte, the code byte follows
 * The code is an unsigned int of 1byte size found in the second byte of the packet
 * depending on the code, the package may change form after it.
 * The code signifies the type of the expected operation.
 * 
 * below are the defined code numbers:
 * 
 * 0  KEEP ALIVE
 * 1  UPING
 * 2  DPING
 * 3  SHORT ROUTED ACK -> Short Ack -> ACK_S
 * 4  END TO END ROUTED ACK -> Long Ack -> ACK_L
 * 5  MESSAGE
 * 
 * --------------------------------
 * | 1 byte version | 1 byte code | -> KEEP ALIVE, UPING, DPING they need no payload
 * --------------------------------
 *    or      /\ the above types do not need routing \/ the below need rooting
 * --------------------------------------------------
 * | 1 byte version | 1 byte code |  2 bytes number |-> SHORT ACK keeps a packet tracking number to aid control flow mechanisms 
 * --------------------------------------------------
 *    or
 * --------------------------------------------------------------------------------------------
 * | 1 byte version | 1 byte code | Source IP  (4 bytes) | Dest IP (4 bytes) | 2 bytes number |-> LONG ACK keeps a packet tracking number to aid control flow mechanisms 
 * --------------------------------------------------------------------------------------------
 *    or
 * ---------------------------------------------------------------------------------------------
 * | 1 byte version | 1 byte code | Source IP  (4 bytes) | Dest IP (4 bytes) | message N bytes | -> MESSAGE
 * ---------------------------------------------------------------------------------------------
 * 
 * 
 * @author Konstantinos Kagiampakis
 */
public class UnityPacket {

	private static final int UNITYversion = 0;
	private static final int MIN_LEN = 2;
	
	private static final int KEEP_ALIVE = 0;
	private static final int UPING = 1;
	private static final int DPING = 2;
	private static final int ACK_S = 3;
	private static final int ACK_L = 4;
	private static final int MESSAGE = 5;
	
	private static final byte[] noPayload = new byte[]{};
	
	public static boolean isUnity(byte[] packet) {
		if (packet.length >= MIN_LEN) {
			int version = (int) packet[0];
			if (version == UNITYversion) {
				return true;
			}
		}
		return false;
    }
    
    public static boolean isKeepAlive(byte[] packet) {
    	if (packet != null) {
	    	if (packet.length == 2) {
		    	int code = (int) packet[1];
				if (code == KEEP_ALIVE) {
					return true;
				}
	    	}
    	}
		return false;
    }	
    
    public static boolean isUping(byte[] packet) {
    	if (packet != null) {
    		if (packet.length == 2) {
		    	int code = (int) packet[1];
				if (code == UPING) {
					return true;
				}
	    	}
    	}
		return false;
    }	
    
    public static boolean isDping(byte[] packet) {
    	if (packet != null) {
    		if (packet.length == 2) {
		    	int code = (int) packet[1];
				if (code == DPING) {
					return true;
				}
	    	}
    	}
		return false;
    }	
    
    public static boolean isLongRoutedAck(byte[] packet) {
    	if (packet != null) {
	    	if (packet.length == 12) {
		    	int code = (int) packet[1];
				if (code == ACK_L) {
					return true;
				}
	    	}
    	}
		return false;
    }	
    
    public static boolean isShortRoutedAck(byte[] packet) {
    	if (packet != null) {
	    	if (packet.length == 4) {
		    	int code = (int) packet[1];
				if (code == ACK_S) {
					return true;
				}
	    	}
    	}
		return false;
    }
    
    public static boolean isMessage(byte[] packet) {
    	if (packet != null) {
	    	if (packet.length > 10) {
		    	int code = (int) packet[1];
				if (code == MESSAGE) {
					return true;
				}
	    	}
    	}
		return false;
    }
    
    public static int getLongRoutedAckTrackNum(byte[] packet) throws Exception {
    	if (isLongRoutedAck(packet)) {
    		byte[] byteNum = new byte[2];
    		System.arraycopy(packet, 10, byteNum, 0, 2);
    		return HashUtilities.bytesToUnsignedInt(byteNum);
    	}
    	throw new Exception("The packet was not a long routed ack packet"); 
    }
    
    public static int getShortRoutedAckTrackNum(byte[] packet) throws Exception {
    	if (isShortRoutedAck(packet)) {
    		byte[] byteNum = new byte[2];
    		System.arraycopy(packet, 2, byteNum, 0, 2);
    		return HashUtilities.bytesToUnsignedInt(byteNum);
    	}
    	throw new Exception("The packet was not an short routed ack packet"); 
    }
    
    public static String getMessageMessage(byte[] packet) throws Exception {
        if (isMessage(packet)) {
	    	byte[] payload = new byte[packet.length-2-8];
	        System.arraycopy(packet, 10, payload, 0, packet.length -2-8);
	        return new String(payload,"utf-8");
        }
        throw new Exception("The packet was not a message packet");
    }

	public static InetAddress getSourceAddress(byte[] packet) throws Exception {
		if (isMessage(packet) || isLongRoutedAck(packet)) {
	        byte[] addr = new byte[4];
	        for (int i = 0; i < 4; i++) {
	            addr[i] = packet[2 + i];
	        }
	        return InetAddress.getByAddress(addr);
		}
		throw new Exception("The packet was not a message nor ack packet");
    }

    public static InetAddress getDestAddress(byte[] packet) throws Exception {
    	if (isMessage(packet) || isLongRoutedAck(packet)) {
	    	byte[] addr = new byte[4];
	        for (int i = 0; i < 4; i++) {
	            addr[i] = packet[6 + i];
	        }
	        return InetAddress.getByAddress(addr);
    	}
    	throw new Exception("The packet was not a message nor ack packet");
    }
    
    public static byte[] buildKeepAlivePacket() {
		return buildPacket(KEEP_ALIVE, noPayload);
	}
	
	public static byte[] buildUpingPacket() {
		return buildPacket(UPING, noPayload);
	}
	
	public static byte[] buildDpingPacket() {
		return buildPacket(DPING, noPayload);
	}
	
	public static byte[] buildShortRoutedAckPacket(int trackNumber) {
		byte[] trackNumBytes = HashUtilities.UnsignedIntTo2Bytes(trackNumber);
		return buildPacket(ACK_S, trackNumBytes);
	}
	
	public static byte[] buildLongRoutedAckPacket(InetAddress source, InetAddress dest, int trackNumber) {
		byte[] sourceBytes = source.getAddress();
		byte[] destBytes = dest.getAddress();
		byte[] trackNumBytes = HashUtilities.UnsignedIntTo2Bytes(trackNumber);
		byte[] payload = new byte[10];
		
		System.arraycopy(sourceBytes,   0, payload, 0, 4);
        System.arraycopy(destBytes,     0, payload, 4, 4);
        System.arraycopy(trackNumBytes, 0, payload, 8, 2);
		return buildPacket(ACK_L, payload);
	}
	
	public static byte[] buildMessagePacket(InetAddress source, InetAddress dest, String message) {
		byte[] sourceBytes = source.getAddress();
		byte[] destBytes = dest.getAddress();
		byte[] messg = message.getBytes();
		byte[] payload = new byte[8+messg.length];
		
        System.arraycopy(sourceBytes, 0, payload, 0, 4);
        System.arraycopy(destBytes,   0, payload, 4, 4);
        System.arraycopy(messg,       0, payload, 8, messg.length);
        return buildPacket(MESSAGE, payload);
	}
	
	private static byte[] buildPacket(int type, byte[] payload) {
		//build the payload before calling this one
        byte[] version = new byte[]{HashUtilities.UnsignedIntTo1Byte(UNITYversion)};
        byte[] typeBytes = HashUtilities.UnsignedIntToByteArray(type);

        byte[] packet = new byte[version.length + typeBytes.length + payload.length];
        System.arraycopy(version, 0, packet, 0, version.length);
        System.arraycopy(typeBytes, 0, packet, 1, typeBytes.length);
        System.arraycopy(payload, 0, packet, 2, payload.length);
        return packet;
    }
}
