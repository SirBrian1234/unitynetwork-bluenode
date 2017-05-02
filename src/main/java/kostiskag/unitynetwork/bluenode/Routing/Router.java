package kostiskag.unitynetwork.bluenode.Routing;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.packets.IPv4Packet;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

/**
 * An object of this class takes a packet from a queue and forwards it to 
 * a red node's queue or a blue node's queue. When a packet has a not known destination
 * the FlyRegister is called.
 * 
 * This class's thread should NEVER wait in a full queue as if it does so the whole rooting goes poof!!!
 * This outcome should be avoided. Wait only on an empty queue.
 * 
 * @author kostis
 */
public class Router extends Thread {

    public final String pre = "^Router ";
    
    public Router() {
    	
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "started routing at thread " + Thread.currentThread().getName());

        while (true) {
            byte[] data;
            try {
                data = App.bn.manager.poll();
            } catch (java.lang.NullPointerException ex1) {
                continue;
            } catch (java.util.NoSuchElementException ex) {
                continue;
            }
            
            /* 
             * from this point on a packet needs to be routed... now lets check 
             * if destination is registered check vaddress table and sent to 
             * specific udp vaddr - addr:udp then when you get the stuff send it
             */

            String sourcevaddress = null;
            String destvaddress = null;

            if (IPv4Packet.isIPv4(data) || UnityPacket.isMessage(data) || UnityPacket.isLongRoutedAck(data)) {
                if (IPv4Packet.isIPv4(data)) {
                	try {
						sourcevaddress = IPv4Packet.getSourceAddress(data).getHostAddress();
						destvaddress = IPv4Packet.getDestAddress(data).getHostAddress();
	                    App.bn.TrafficPrint(pre + "IP " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", 3, 0);
					} catch (Exception e) {
						e.printStackTrace();
					}                	
                } else {
                    try {
						sourcevaddress = UnityPacket.getSourceAddress(data).getHostAddress();
						destvaddress = UnityPacket.getDestAddress(data).getHostAddress();
	                    App.bn.TrafficPrint(pre + "Unity " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", 3, 0);
                    } catch (Exception e) {
						e.printStackTrace();
					}                    
                }

                if (destvaddress.startsWith("10.")) {
	                if (destvaddress.equals("10.0.0.0") || destvaddress.equals("10.255.255.255")) {
	                	// allow no special purpose addresses
	                	
	                } else if (destvaddress.equals("10.0.0.1")) {
	                	// the first address is reserved
	                	
	                } else if (App.bn.localRedNodesTable.checkOnlineByVaddress(destvaddress)) {
	                    //load the packet data to local red node's queue
	                    App.bn.localRedNodesTable.getRedNodeInstanceByAddr(destvaddress).getQueueMan().offerNoWait(data);
	                    App.bn.TrafficPrint(pre+"LOCAL DESTINATION", 3, 0);
	                    
	                } else if (App.bn.joined) {
	                    if (App.bn.blueNodesTable.checkRemoteRedNodeByVaddress(destvaddress)) {
	                    	//load the packet to remote blue node's queue
	                        BlueNodeInstance bn;
							try {
								bn = App.bn.blueNodesTable.getBlueNodeInstanceByRRNVaddr(destvaddress);
								bn.getQueueMan().offerNoWait(data);
		                        App.bn.TrafficPrint(pre +"REMOTE DESTINATION -> " + bn.getName(), 3, 1);
							} catch (Exception e) {
								e.printStackTrace();
							}                        
	                    } else {
	                    	//lookup via tracker from a bluenode with this rrd
	                        App.bn.TrafficPrint(pre +"NOT KNOWN RRN WITH "+destvaddress+" SEEKING TARGET BN", 3, 1);
	                        App.bn.flyreg.seekDest(sourcevaddress, destvaddress);
	                    }
	                } else {
	                    App.bn.TrafficPrint(pre +"NOT IN THIS BN " + destvaddress, 3, 1);
	                }
                } else {
                	App.bn.TrafficPrint(pre+"source address "+sourcevaddress+" does not belong in network range.",3,1);
                }
            } else {
            	App.bn.TrafficPrint(pre+"wrong header packet detected in router.",3,1);
            }           
        }
    }
}