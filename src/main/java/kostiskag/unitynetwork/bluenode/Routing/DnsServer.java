package kostiskag.unitynetwork.bluenode.Routing;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.functions.HashFunctions;

/**
 * This is the internal dns system 
 * 
 * @author kostis
 */
public class DnsServer extends Thread {

	private final String pre = "^DnsServer";
	private QueueManager queue = new QueueManager(100);
	private AtomicBoolean kill = new AtomicBoolean(false);
	
	public DnsServer() {
		
	}
	
	@Override
	public void run() {
		while(!kill.get()) {
			
			byte[] data = queue.poll();
			
			if (kill.get()) {
				break;
			} else if (data == null) {
				continue;
			}
			
			String sourcevaddress = IpPacket.getSourceAddress(data).getHostAddress();        	
			System.out.println("dns ouiouuu "+sourcevaddress);
        	
			String type = IpPacket.getIpPacketPayloadType(data);
			System.out.println("type "+type);
			
			if (type.equals("11")) {
				//this is a UDP packet
				System.out.println("this is a udp datagramm");
				byte[] datagramm = IpPacket.getIpPacketPayload(data);
				
				//now you should check if  THE DESTINATION PORT IS 53
				int destPort = IpPacket.getUDPdestPort(datagramm);
				System.out.println("dest port is "+destPort);
				
				if (destPort == 53) {
					System.out.println("heading to dns");
					int sourcePort = IpPacket.getUDPsourcePort(datagramm);
					System.out.println("source - dest port is "+sourcePort+" - "+destPort);
					
					//this is a udp heading to a dns server
					byte[] updPayload = IpPacket.getUDPpayload(datagramm);
					String sessionId = getSessionIdInHex(updPayload);
					String flags = getFlagsInHex(updPayload);
					//query
					byte[] query = getQueryInBytes(updPayload);
					String name = null;
					try {
						name = getQueryNameInStr(query);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					char[] nameArray = new char[name.length()];
					name.getChars(0, 1, nameArray, 0);
					
					String qType = getQueryTypeInHex(query);
					String qClass = getQueryClassInHex(query);
					
					System.out.println("session id is... "+sessionId);
					System.out.println("flags are... "+flags);
					System.out.println("query is... "+new String(query));					
					System.out.println("name is... "+name);
					
					System.out.println("byte 0 is "+nameArray[0]);
					System.out.println("byte 0 is "+(int) nameArray[0]);
					
					System.out.println("qType is... "+qType);
					System.out.println("qClass is... "+qClass);
					
					/*
					if (qType.equals("0001") || qType.equals("001c")) {
						
						//resolve it
						if (App.bn.joined) {
							//operates on full network
							
						} else {
							//operates on local network
							
						}						
						//offer responce to local bn for routing
						
						
					} else {
						//build a non allowed service responce
						
					}
					*/
					
					//include responce to udp
					//include udp to ip
					byte[] reply = "hiii :P".getBytes();
					
					//make datagramm and include reply
					byte[] datagrammToSend = IpPacket.MakeUDPDatagramm(reply, destPort, sourcePort);
					
					//make ip packet and include datagramm
					byte[] protocolType = HashFunctions.hexStrToBytes(type);
					byte[] sourceIp = null;
					byte[] destIp = null;
					try {
						sourceIp = InetAddress.getByName("10.0.0.1").getAddress();
						destIp = InetAddress.getByName(sourcevaddress).getAddress();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					byte[] packet = IpPacket.MakeIpPacket(datagrammToSend, sourceIp, destIp, protocolType);
					
					//offer the packet for routing
					App.bn.manager.offer(packet);
				}
			}
		
		}
		App.bn.ConsolePrint(pre+"ENDED");
	}	
	
	public void kill() {
		kill.set(true);
	}
	
	public void offer(byte[] data) {
		queue.offer(data);
	}
	
	public static String getSessionIdInHex(byte[] dnsQuery) {
		byte[] id = new byte[2];
        System.arraycopy(dnsQuery, 0, id, 0, 2);
        return HashFunctions.bytesToHexStr(id);
	}
	
	public static String getFlagsInHex(byte[] dnsQuery) {
		byte[] flags = new byte[2];
        System.arraycopy(dnsQuery, 2, flags, 0, 2);
        return HashFunctions.bytesToHexStr(flags);
	}
	
	public static byte[] getQueryInBytes(byte[] udpPayload) {
		byte[] query = new byte[udpPayload.length-2-8];
        System.arraycopy(udpPayload, 2+8, query, 0, udpPayload.length-2-8);
        return query;
	}
	
	public static String getQueryNameInStr(byte[] query) throws UnsupportedEncodingException {
		byte[] name = new byte[query.length-2-2];
        System.arraycopy(query, 0, name, 0, query.length-2-2);
        return new String(name, "utf-8");
	}
	
	public static String getQueryTypeInHex(byte[] query) {
		byte[] type = new byte[2];
        System.arraycopy(query, query.length-2-2, type, 0, 2);
        return HashFunctions.bytesToHexStr(type);
	}
	
	public static String getQueryClassInHex(byte[] query) {
		byte[] classType = new byte[2];
        System.arraycopy(query, query.length-2, classType, 0, 2);
        return HashFunctions.bytesToHexStr(classType);
	}
}
