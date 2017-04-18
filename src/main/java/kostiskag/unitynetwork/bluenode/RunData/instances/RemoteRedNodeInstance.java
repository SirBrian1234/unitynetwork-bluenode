package kostiskag.unitynetwork.bluenode.RunData.instances;

import kostiskag.unitynetwork.bluenode.functions.GetTime;

/**
 * A remote red node instance is used from a remote red node table. 
 * 
 * @author Konstantinos Kagiampakis
 */
public class RemoteRedNodeInstance {

	private final String hostname;
	private final String vAddress;    
    private final BlueNodeInstance blueNode;
    private String timestamp;

    public RemoteRedNodeInstance(String hostname, String vAddress, BlueNodeInstance blueNode) {
        this.hostname = hostname;
        this.vAddress = vAddress;
        this.blueNode = blueNode;        
        this.timestamp = GetTime.getSmallTimestamp();
    }

    public String getHostname() {
        return hostname;
    }
    
    public String getVaddress() {
        return vAddress;
    }

    public BlueNodeInstance getBlueNode() {
        return blueNode;
    }

    public String getTime() {
        return timestamp;
    }
    
    public void updateTime() {
        this.timestamp = GetTime.getSmallTimestamp();
    }   
}