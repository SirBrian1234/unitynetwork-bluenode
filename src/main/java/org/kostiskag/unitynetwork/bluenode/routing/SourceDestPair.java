package org.kostiskag.unitynetwork.bluenode.routing;

/**
 *
 * @author Konstantinos Kagiampakis
 */
class SourceDestPair {
    public final String sourcevaddress;
    public final String destvaddress;

    public SourceDestPair(String sourcevaddress, String destvaddress) {
        this.sourcevaddress = sourcevaddress;
        this.destvaddress = destvaddress;
    }
}
