package org.kostiskag.unitynetwork.bluenode.routing;

import org.kostiskag.unitynetwork.common.address.VirtualAddress;

/**
 *
 * @author Konstantinos Kagiampakis
 */
final class SourceDestPair {
    public final VirtualAddress sourceAddress;
    public final VirtualAddress destAddress;

    public SourceDestPair(VirtualAddress sourceAddress, VirtualAddress destAddress) {
        this.sourceAddress = sourceAddress;
        this.destAddress = destAddress;
    }
}
