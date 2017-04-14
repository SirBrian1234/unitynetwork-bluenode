package kostiskag.unitynetwork.bluenode.RunData.instances;

import kostiskag.unitynetwork.bluenode.functions.getTime;

/**
 *
 * @author kostis
 */
public class RedRemoteAddress {

	private String hostname;
	private String virtualAddress;    
    private String blueNodeName;
    private String timestamp;

    public RedRemoteAddress(String virtualAddress, String hostname, String blueNodeHostname, String timestamp) {
        this.virtualAddress = virtualAddress;
        this.hostname = hostname;
        this.blueNodeName = blueNodeHostname;        
        this.timestamp = timestamp;
    }

    public void init(String VirtualAddress, String Hostname, String BlueNodeHostname, String timestamp) {
        this.virtualAddress = VirtualAddress;
        this.hostname = Hostname;
        this.blueNodeName = BlueNodeHostname;        
        this.timestamp = timestamp;
    }

    public String getVAddress() {
        return virtualAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public String getBlueNodeName() {
        return blueNodeName;
    }

    public String getTime() {
        return timestamp;
    }
    
    public void updateTime() {
        this.timestamp = getTime.getSmallTimestamp();
    }   
}