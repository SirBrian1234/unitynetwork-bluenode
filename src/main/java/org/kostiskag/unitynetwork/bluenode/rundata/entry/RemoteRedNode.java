package org.kostiskag.unitynetwork.bluenode.rundata.entry;

import java.net.UnknownHostException;

import org.kostiskag.unitynetwork.common.entry.NodeEntry;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;


/**
 * A remote red node instance is used from a remote red node table. 
 * 
 * @author Konstantinos Kagiampakis
 */
public final class RemoteRedNode extends NodeEntry<VirtualAddress> {

	private final BlueNode blueNode;

	public static RemoteRedNode newInstance(String hostname, String vAddress, BlueNode blueNode) throws UnknownHostException, IllegalAccessException{
        if (hostname == null || vAddress == null || blueNode == null) {
            throw new IllegalArgumentException("null data were given!");
        }

	    var address = VirtualAddress.valueOf(vAddress);
        return new RemoteRedNode(hostname, address, blueNode);
    }

    public static RemoteRedNode newInstance(String hostname, VirtualAddress address, BlueNode blueNode) throws IllegalAccessException{
        if (hostname == null || address == null || blueNode == null) {
            throw new IllegalArgumentException("null data were given!");
        }

        return new RemoteRedNode(hostname, address, blueNode);
    }

    private RemoteRedNode(String hostname, VirtualAddress vAddress, BlueNode blueNode) throws IllegalAccessException {
        super(hostname, vAddress);
        this.blueNode = blueNode;
    }

    public BlueNode getBlueNode() {
        return blueNode;
    }
}