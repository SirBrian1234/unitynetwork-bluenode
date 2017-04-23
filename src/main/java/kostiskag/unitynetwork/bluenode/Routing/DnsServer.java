package kostiskag.unitynetwork.bluenode.Routing;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
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
					int questions = getQuestionNum(updPayload);
					int answers = getAnswerNum(updPayload);
					int authority = getAuthorityNum(updPayload);
					int additional = getAdditionalNum(updPayload);
					
					//get the query
					byte[] query = getQueryInBytes(updPayload);
					
					char[] nameArray = null;
					String name = null;
					
					try {
						nameArray = getQueryNameInCharArray(query);
						name = new String(nameArray);
					} catch (UnsupportedEncodingException e1) {						
						e1.printStackTrace();
					}
					
					LinkedList<String> nameList = getLogicalDomainRepresentationAsAList(nameArray); 
					String qType = getQueryTypeInHex(query);
					String qClass = getQueryClassInHex(query);
					
					
					System.out.println("session id is..........:"+sessionId);
					System.out.println("flags are..............:"+flags);
					System.out.println("query is...............:"+new String(query));
					System.out.println("num of questions are...:"+questions);
					System.out.println("num of answers are.....:"+answers);
					System.out.println("num of authorities are.:"+authority);
					System.out.println("num of additional are..:"+additional);
					System.out.println("name is................:"+name);
					
					for (int i=0; i<nameArray.length; i++) {
						System.out.println("char at "+i+" is ["+nameArray[i]+"] of num:"+(int) nameArray[i]);
					}
					
					System.out.println("qType is...............:"+qType);
					System.out.println("qClass is..............:"+qClass);
					
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
					//App.bn.manager.offer(packet);
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
	
	//first 2 bytes is session (transaction)
	public static String getSessionIdInHex(byte[] dnsQuery) {
		byte[] id = new byte[2];
        System.arraycopy(dnsQuery, 0, id, 0, 2);
        return HashFunctions.bytesToHexStr(id);
	}
	
	//next 2 bytes are flags
	public static String getFlagsInHex(byte[] dnsQuery) {
		byte[] flags = new byte[2];
        System.arraycopy(dnsQuery, 2, flags, 0, 2);
        return HashFunctions.bytesToHexStr(flags);
	}
	
	//next 2 bytes are num of questions
	public static int getQuestionNum(byte[] dnsQuery) {
		byte[] q = new byte[2];
        System.arraycopy(dnsQuery, 4, q, 0, 2);
        return HashFunctions.bytesToUnsignedInt(q);
	}
	
	//next 2 bytes are num of answers
	public static int getAnswerNum(byte[] dnsQuery) {
		byte[] a = new byte[2];
        System.arraycopy(dnsQuery, 6, a, 0, 2);
        return HashFunctions.bytesToUnsignedInt(a);
	}
	
	//next 2 bytes are num of authority
	public static int getAuthorityNum(byte[] dnsQuery) {
		byte[] a = new byte[2];
        System.arraycopy(dnsQuery, 8, a, 0, 2);
        return HashFunctions.bytesToUnsignedInt(a);
	}
	
	//next 2 bytes are num of additional
	public static int getAdditionalNum(byte[] dnsQuery) {
		byte[] a = new byte[2];
        System.arraycopy(dnsQuery, 10, a, 0, 2);
        return HashFunctions.bytesToUnsignedInt(a);
	}
	
	//next the queries follow
	public static byte[] getQueryInBytes(byte[] udpPayload) {
		byte[] query = new byte[udpPayload.length-12];
        System.arraycopy(udpPayload, 12, query, 0, udpPayload.length-12);
        return query;
	}
	
	//get the name of a query
	public static char[] getQueryNameInCharArray(byte[] query) throws UnsupportedEncodingException {
		char[] name = new char[query.length-2-2];
        for(int i=0; i<query.length-2-2; i++) {
        	name[i] = (char) query[i];
        }
        return name;
	}
	
	private LinkedList<String> getLogicalDomainRepresentationAsAList(char[] nameArray) {
		LinkedList<String> list = new LinkedList<>();
		
		//we have to determine how many parts are there
		//and we have to collect starting points and sizes 
		LinkedList<Integer> starts = new LinkedList<Integer>();
		LinkedList<Integer> sizes = new LinkedList<Integer>();
		
		int nextToVisit = 0;
		starts.add((int) nextToVisit + 1);
		while(nameArray[nextToVisit] != 0) {			
			sizes.add((int) nameArray[nextToVisit]);
			nextToVisit = nextToVisit + nameArray[nextToVisit] +1;
			starts.add((int) nextToVisit + 1);
		}
		System.out.println(sizes.size()+" parts");
		
		for (int i=0; i<sizes.size(); i++) {
			System.out.println("word "+i+" start from:"+starts.get(i)+" read:"+sizes.get(i));					 
		}
		
		return list;
	}
	
	//get the type of a query
	public static String getQueryTypeInHex(byte[] query) {
		byte[] type = new byte[2];
        System.arraycopy(query, query.length-2-2, type, 0, 2);
        return HashFunctions.bytesToHexStr(type);
	}
	
	//get the class of a query
	public static String getQueryClassInHex(byte[] query) {
		byte[] classType = new byte[2];
        System.arraycopy(query, query.length-2, classType, 0, 2);
        return HashFunctions.bytesToHexStr(classType);
	}
}
