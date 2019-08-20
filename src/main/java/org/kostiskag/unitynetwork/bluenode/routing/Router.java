package org.kostiskag.unitynetwork.bluenode.routing;

import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.io.IOException;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.entry.NodeEntry;
import org.kostiskag.unitynetwork.common.entry.NodeType;
import org.kostiskag.unitynetwork.common.routing.QueueManager;
import org.kostiskag.unitynetwork.common.routing.packet.IPv4Packet;
import org.kostiskag.unitynetwork.common.routing.packet.UnityPacket;
import org.kostiskag.unitynetwork.common.service.SimpleUnstoppedCyclicService;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.LocalRedNode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.AppLogger.MessageType;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;


/**
 * An object of this class can be owned either by a blue node or a red node instance
 * They provide their receive queu here. The router routes the packet in another
 * queue. Since every instance has its own router the thread may wait for the target 
 * queue to have available space.
 * 
 * @author Konstantinos Kagiampakis
 */
public class Router<A extends NodeEntry> extends SimpleUnstoppedCyclicService {

    private final String pre;
    private final A owner;
    private final QueueManager<byte[]> queueToRoute;
    
    public Router(A owner, QueueManager queueToRoute) {
    	if(!(owner instanceof LocalRedNode) && !(owner instanceof BlueNode)) {
    		throw new IllegalArgumentException("router by a non-allowed NodeEntry object");
		}
    	this.owner = owner;
    	this.pre = "^Router "+this.owner.getHostname()+" ";
    	this.queueToRoute = queueToRoute;
    }

	@Override
	protected void preActions() {
		AppLogger.getInstance().consolePrint(pre + "started routing at thread " + Thread.currentThread().getName());
	}

	@Override
	protected void postActions() {
		AppLogger.getInstance().consolePrint(pre + "ended");
	}

	@Override
	protected void cyclicActions() {
		byte[] data;
		try {
			data = queueToRoute.poll();
		} catch (NullPointerException | NoSuchElementException ex) {
			return;
		}

		/*
		 * from this point on a packet needs to be routed... now lets check
		 * if destination is registered check vaddress table and sent to
		 * specific udp vaddr - addr:udp then when you get the stuff send it
		 */
		if (IPv4Packet.isIPv4(data) || UnityPacket.isMessage(data) || UnityPacket.isLongRoutedAck(data)) {
			VirtualAddress sourceAddress = null;
			VirtualAddress destAddress = null;
			if (IPv4Packet.isIPv4(data)) {
				try {
					sourceAddress = IPv4Packet.getSourceAddress(data);
					destAddress = IPv4Packet.getDestAddress(data);
					AppLogger.getInstance().trafficPrint(pre + "IP " + sourceAddress + " -> " + destAddress + " " + data.length + "B", MessageType.ROUTING, NodeType.REDNODE);
				} catch (IOException e) {
					AppLogger.getInstance().consolePrint(pre+e.getMessage());
					return;
				}
			} else {
				try {
					sourceAddress = UnityPacket.getSourceAddress(data);
					destAddress = UnityPacket.getDestAddress(data);
					AppLogger.getInstance().trafficPrint(pre + "Unity " + sourceAddress + " -> " + destAddress + " " + data.length + "B", MessageType.ROUTING, NodeType.REDNODE);
				} catch (IOException e) {
					AppLogger.getInstance().consolePrint(pre+e.getMessage());
					return;
				}
			}

			if (Bluenode.getInstance().localRedNodesTable.checkOnlineByVaddress(destAddress.asString())) {
				//load the packet data to local red node's queue
				Bluenode.getInstance().localRedNodesTable.getRedNodeInstanceByAddr(destAddress.asString()).getSendQueue().offer(data);
				AppLogger.getInstance().trafficPrint(pre+"LOCAL DESTINATION", MessageType.ROUTING, NodeType.REDNODE);

			} else if (Bluenode.getInstance().isJoinedNetwork()) {
				var bnt = Bluenode.getInstance().blueNodeTable;
				Lock lock = null;
				try {
					lock = bnt.aquireLock();
					var opt = bnt.getBlueNodeEntryByRemoteRedNode(lock, destAddress);
					if (opt.isPresent()) {
						var bn = opt.get();
						bn.getSendQueue().offer(data);
						AppLogger.getInstance().trafficPrint(pre +"REMOTE DESTINATION -> " + bn.getHostname(), MessageType.ROUTING, NodeType.BLUENODE);
					} else {
						//lookup via tracker from a bluenode with this rrd
						AppLogger.getInstance().trafficPrint(pre +"NOT KNOWN RRN WITH "+destAddress+" SEEKING TARGET BN", MessageType.ROUTING, NodeType.BLUENODE);
						FlyRegister.getInstance().seekDest(sourceAddress, destAddress);
					}
				} catch (InterruptedException e) {
					AppLogger.getInstance().trafficPrint(pre +"Could not aquire lock for bluenode table the package drops "+e.getLocalizedMessage(), MessageType.ROUTING, NodeType.BLUENODE);
				} finally {
					lock.unlock();
				}
			} else {
				AppLogger.getInstance().trafficPrint(pre +"NOT IN THIS BN " + destAddress, MessageType.ROUTING, NodeType.BLUENODE);
			}
		} else {
			AppLogger.getInstance().trafficPrint(pre +"wrong header packet detected in router.", MessageType.ROUTING, NodeType.BLUENODE);
		}
	}

    public void kill() {
    	super.kill();
    	queueToRoute.exit();
    }
}