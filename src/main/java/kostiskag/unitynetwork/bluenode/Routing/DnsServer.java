package kostiskag.unitynetwork.bluenode.Routing;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.functions.HashFunctions;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;

/**
 * This is the internal dns system 
 * 
 * @author Konstantinos Kagiampakis
 */
public class DnsServer extends Thread {

	private final String pre = "^DnsServer ";
	private QueueManager queue = new QueueManager(100);
	private AtomicBoolean kill = new AtomicBoolean(false);
	
	private static byte[] flagsSetToFound = new byte[] {(byte) 0x81, (byte) 0x80};
	private static byte[] flagsSetToNotFound = new byte[] {(byte) 0x81, (byte) 0x83};
	private static byte[] oneIn2Bytes = new byte[] {(byte) 0x00, (byte) 0x01};
	private static byte[] zeroIn2Bytes = new byte[] {(byte) 0x00, (byte) 0x00};
	
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
			String type = IpPacket.getIpPacketPayloadType(data);
			
			if (sourcevaddress.startsWith("10.") && type.equals("11")) {
				//this is a UDP packet from an internal network host
				byte[] datagramm = IpPacket.getIpPacketPayload(data);
				
				//now you should check if THE DESTINATION PORT IS 53
				int destPort = IpPacket.getUDPdestPort(datagramm);
				
				if (destPort == 53) {
					//this is a udp datagramm heading to the dns service
					int sourcePort = IpPacket.getUDPsourcePort(datagramm);
					byte[] updPayload = IpPacket.getUDPpayload(datagramm);
					
					if (updPayload.length >= 12 && updPayload.length <= 512) {
					
						String sessionId = getSessionIdInHex(updPayload);
						String flags = getFlagsInHex(updPayload);
						int questions = getQuestionNum(updPayload);
						int answers = getAnswerNum(updPayload);
						int authority = getAuthorityNum(updPayload);
						int additional = getAdditionalNum(updPayload);
						
						if (flags.equals("0100") && questions == 1 && answers == 0 && updPayload.length >= 12+7)  {
						
							//accept and answer the given query
							boolean denied = false;
							byte[] query = getQueryInBytes(updPayload);
							
							char[] nameArray = null;
							byte[] nameArrayInBytes = null;
							String name = null;
							
							try {
								nameArray = getQueryNameInCharArray(query);
								nameArrayInBytes = getQueryNameInByteArray(query);
								name = new String(nameArray);
							} catch (UnsupportedEncodingException e1) {						
								e1.printStackTrace();
							}
							
							LinkedList<String> nameList = getLogicalDomainRepresentationAsAList(nameArray); 
							String qType = getQueryTypeInHex(query);
							String qClass = getQueryClassInHex(query);
							
							//START OF VERBOSE STUFF FOR HUMANS
							System.out.println("session id is..........:"+sessionId);
							System.out.println("flags are..............:"+flags);
							System.out.println("query is...............:"+new String(query));
							System.out.println("num of questions are...:"+questions);
							System.out.println("num of answers are.....:"+answers);
							System.out.println("num of authorities are.:"+authority);
							System.out.println("num of additional are..:"+additional);
							System.out.println("name is................:"+name);
							System.out.println("qType is...............:"+qType);
							System.out.println("qClass is..............:"+qClass);
							System.out.println("I have ["+nameList.size()+"] domain parts");
							//END OF VERBOSE HUMAN STUFF
							
							byte[] flagsToSend = zeroIn2Bytes;
							byte[] answer = zeroIn2Bytes;
							byte[] ans = zeroIn2Bytes;
							
							if (qType.equals("0001") && nameList.size() == 1) {
								//first lets check for a given hostname to provide a virtual address
								String vaddress = null;
								String hostname = nameList.get(0);
								System.out.println(hostname);
								if (hostname.length() <= App.max_str_len_large_size) {
									App.bn.ConsolePrint(pre+"hostname lookup "+hostname);
								
									if (App.bn.localRedNodesTable.checkOnlineByHostname(hostname)) {
										//check for local red nodes										
										vaddress = App.bn.localRedNodesTable.getRedNodeInstanceByHn(hostname).getVaddress();
										App.bn.ConsolePrint(pre+"hostname "+hostname+" found as a local RN with "+vaddress);
										
									} else if (App.bn.network && App.bn.joined && App.bn.blueNodesTable.checkRemoteRedNodeByHostname(hostname)) {
										//check for registered remote red nodes
										try {
											vaddress = App.bn.blueNodesTable.getBlueNodeInstanceByRRNHostname(hostname).table.getByHostname(hostname).getVaddress();
											App.bn.ConsolePrint(pre+"hostname "+hostname+" found as a RRN with "+vaddress);
										} catch (Exception e) {
											denied = true;
										}
										
									} else if (App.bn.network && App.bn.joined) {
										//check tracker
										App.bn.ConsolePrint(pre+"hostname nslookup through tracker.");
										TrackerClient tr = new TrackerClient();
										String trackerAnswer = tr.nslookupByHostname(hostname);
										if (trackerAnswer != null) {											
											//got it!
											vaddress = trackerAnswer;											
											//we have another job here and that is to build the path
											//as a lookup may lead to future data exchange
											App.bn.ConsolePrint(pre+"Building path for "+sourcevaddress+"  towards -> "+vaddress);
											App.bn.flyreg.seekDest(sourcevaddress, vaddress);											
											
										} else {
											App.bn.ConsolePrint(pre+"hostname query "+hostname+" was not found on network.");
											denied = true;
										}
									} else {
										denied = true;
									}									
								} else {
									denied = true;
								}
								
								if (!denied) {
									System.out.println("building answer with "+vaddress);
									//build answer
									flagsToSend = flagsSetToFound;
									ans = oneIn2Bytes;
									
									//nameArrayInBytes
									answer = new byte[16];
									
									//name set to a dot 2 bytes
									byte[] nameToSend = new byte[] {(byte) 0xc0, (byte) 0x0c};
									System.arraycopy(nameToSend, 0, answer, 0, 2);
									
									//type set to A 2 bytes
									byte[] typeToSend = oneIn2Bytes;
									System.arraycopy(typeToSend, 0, answer, 2, 2);
									
									//class set to IN 2 bytes
									byte[] classToSend = oneIn2Bytes;
									System.arraycopy(classToSend, 0, answer, 4, 2);
									
									//ttl set to 128 4 bytes
									byte[] ttl = HashFunctions.UnsignedIntTo4Bytes(128);
									System.arraycopy(ttl, 0, answer, 6, 4);
									
									//len 2 bytes set to 4
									byte[] lenToSend = HashFunctions.UnsignedIntTo2Bytes(4);
									System.arraycopy(lenToSend, 0, answer, 10, 2);
									
									//address
									byte[] addressToSend = new byte[4];
									try {
										addressToSend = InetAddress.getByName(vaddress).getAddress();
									} catch (UnknownHostException e) {
										e.printStackTrace();
									}
									System.arraycopy(addressToSend, 0, answer, 12, 4);									
								}
								
							} else if (qType.equals("000c") && nameList.size() == 6 && nameList.get(5).equals("arpa") && nameList.get(4).equals("in-addr") && nameList.get(3).equals("10")) {
								// then look for a given ip address to provide a hostname
								// do not forget to answer for yourself! hi! I am a dns
								String hostname = null;
								String vaddress= nameList.get(3)+"."+nameList.get(2)+"."+nameList.get(1)+"."+nameList.get(0);
								System.out.println(vaddress);
								
								if (vaddress.length() <= App.max_str_addr_len) {		
									App.bn.ConsolePrint(pre+"vaddress lookup "+vaddress);
									
									if (App.bn.localRedNodesTable.checkOnlineByVaddress(vaddress)) {
										App.bn.ConsolePrint(pre+"vaddress nslookup is local.");
										hostname = App.bn.localRedNodesTable.getRedNodeInstanceByAddr(vaddress).getHostname();
										
									} else if (App.bn.network && App.bn.joined && App.bn.blueNodesTable.checkRemoteRedNodeByVaddress(vaddress)) {
										try {
											hostname = App.bn.blueNodesTable.getBlueNodeInstanceByRRNVaddr(vaddress).table.getByVaddress(vaddress).getHostname();
											App.bn.ConsolePrint(pre+"vaddress nslookup is an associated rrn.");
										} catch (Exception e) {
											denied = true;
										}
										
									} else if  (App.bn.network && App.bn.joined) {
										App.bn.ConsolePrint(pre+"vaddress nslookup through tracker.");
										TrackerClient tr = new TrackerClient();
										String trackerAnswer = tr.nslookupByVaddr(vaddress);
										if (trackerAnswer != null) {											
											//got it!
											hostname = trackerAnswer;											
											//we have another job here and that is to build the path
											//as a lookup may lead to future data exchange
											App.bn.ConsolePrint(pre+"Building path for "+sourcevaddress+"  towards -> "+vaddress);
											App.bn.flyreg.seekDest(sourcevaddress, vaddress);											
											
										} else {
											App.bn.ConsolePrint(pre+"vaddress query "+vaddress+" was not found on network.");
											denied = true;
										}
										
									} else {
										denied = true;									
									}
									
									if (!denied) {
										//build answer
										System.out.println("building answer with "+hostname);
										
									}
									
								} else {
									denied = true;
								}
							}  else {
								denied = true;
							}
							
							if (denied) {
								//not what we were expecting at least let's reply with not found
								//flags set to not found
								flagsToSend = flagsSetToNotFound;
								//number answers set to zero
								ans =  zeroIn2Bytes;
								//no answer
								answer = new byte[] {};
							}
							
							//headder
							byte[] header = new byte[12];
							
							//session
							byte[] sessionToSend = HashFunctions.hexStrToBytes(sessionId);
							System.arraycopy(sessionToSend, 0, header, 0, 2);
							
							//flags set to standard query response 0x8180 0x8183 for does not exist
							System.arraycopy(flagsToSend, 0, header, 2, 2);
							
							//questions set to one
							byte[] qts =  oneIn2Bytes;
							System.arraycopy(qts, 0, header, 4, 2);
							
							//answer number
							System.arraycopy(ans, 0, header, 6, 2);
							
							//authorities set to zero
							byte[] auth =  zeroIn2Bytes;
							System.arraycopy(auth, 0, header, 8, 2);
							
							//additional set to zero
							byte[] add =  zeroIn2Bytes;
							System.arraycopy(add, 0, header, 10, 2);
							
							//compile reply
							byte[] dnsReply = new byte[header.length+query.length+answer.length];
							System.arraycopy(header, 0, dnsReply, 0, header.length);
							System.arraycopy(query, 0, dnsReply, header.length, query.length);
							System.arraycopy(answer, 0, dnsReply, header.length+query.length, answer.length);
						
							//build datagramm and include reply
							byte[] datagrammToSend = IpPacket.MakeUDPDatagramm(dnsReply, destPort, sourcePort);
							
							//build ip packet and include datagramm
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
	
	//get the name of a query in a char array
	public static char[] getQueryNameInCharArray(byte[] query) throws UnsupportedEncodingException {
		char[] name = new char[query.length-2-2];
        for(int i=0; i<query.length-2-2; i++) {
        	name[i] = (char) query[i];
        }
        return name;
	}
	
	//get the name of a query in byte array
	public static byte[] getQueryNameInByteArray(byte[] query) throws UnsupportedEncodingException {
		byte[] name = new byte[query.length-2-2];
        for(int i=0; i<query.length-2-2; i++) {
        	name[i] = (byte) query[i];
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
		
		for (int i=0; i<sizes.size(); i++) {
			int size = sizes.get(i);
			int start = starts.get(i);
			StringBuilder str = new StringBuilder();
			for(int j=start; j<size+start; j++) {
				str.append(nameArray[j]);
			}
			list.add(str.toString());
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
