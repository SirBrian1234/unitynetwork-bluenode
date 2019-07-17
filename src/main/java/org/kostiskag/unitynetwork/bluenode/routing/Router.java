package org.kostiskag.unitynetwork.bluenode.routing;

import java.util.concurrent.atomic.AtomicBoolean;

import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.routing.packet.IPv4Packet;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;

import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.bluenode.AppLogger.MessageType;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNodeInstance;

/**
 * An object of this class can be owned either by a blue node or a red node instance
 * They provide their receive queu here. The router routes the packet in another
 * queue. Since every instance has its own router the thread may wait for the target 
 * queue to have available space.
 * 
 * @author Konstantinos Kagiampakis
 */
public class Router extends Thread {

    public final String pre;
    public final QueueManager queueToRoute;
    public final AtomicBoolean kill = new AtomicBoolean(false);
    
    public Router(String name, QueueManager queueToRoute) {
    	this.pre = "^Router "+name+" ";
    	this.queueToRoute = queueToRoute;
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "started routing at thread " + Thread.currentThread().getName());

        while (!kill.get()) {
            byte[] data;
            try {
                data = queueToRoute.poll();
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
	                    App.bn.TrafficPrint(pre + "IP " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", MessageType.ROUTING, NodeType.REDNODE);
					} catch (Exception e) {
						e.printStackTrace();
					}                	
                } else {
                    try {
						sourcevaddress = UnityPacket.getSourceAddress(data).getHostAddress();
						destvaddress = UnityPacket.getDestAddress(data).getHostAddress();
	                    App.bn.TrafficPrint(pre + "Unity " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", MessageType.ROUTING, NodeType.REDNODE);
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
	                    App.bn.localRedNodesTable.getRedNodeInstanceByAddr(destvaddress).getSendQueue().offer(data);
	                    App.bn.TrafficPrint(pre+"LOCAL DESTINATION", MessageType.ROUTING, NodeType.REDNODE);
	                    
	                } else if (App.bn.joined) {
	                    if (App.bn.blueNodeTable.checkRemoteRedNodeByVaddress(destvaddress)) {
	                    	//load the packet to remote blue node's queue
	                        BlueNodeInstance bn;
							try {
								bn = App.bn.blueNodeTable.getBlueNodeInstanceByRRNVaddr(destvaddress);
								bn.getSendQueue().offer(data);
		                        App.bn.TrafficPrint(pre +"REMOTE DESTINATION -> " + bn.getName(), MessageType.ROUTING, NodeType.BLUENODE);
							} catch (Exception e) {
								e.printStackTrace();
							}                        
	                    } else {
	                    	//lookup via tracker from a bluenode with this rrd
	                        App.bn.TrafficPrint(pre +"NOT KNOWN RRN WITH "+destvaddress+" SEEKING TARGET BN", MessageType.ROUTING, NodeType.BLUENODE);
	                        App.bn.flyreg.seekDest(sourcevaddress, destvaddress);
	                    }
	                } else {
	                    App.bn.TrafficPrint(pre +"NOT IN THIS BN " + destvaddress, MessageType.ROUTING, NodeType.BLUENODE);
	                }
                } else {
                	App.bn.TrafficPrint(pre+"source address "+sourcevaddress+" does not belong in network range.",MessageType.ROUTING,NodeType.BLUENODE);
                }
            } else {
            	App.bn.TrafficPrint(pre+"wrong header packet detected in router.", MessageType.ROUTING, NodeType.BLUENODE);
				
            }           
        }
        App.bn.ConsolePrint(pre + "ended");
    }
    
    public void kill() {
    	kill.set(true);
    	queueToRoute.exit();
    }
}