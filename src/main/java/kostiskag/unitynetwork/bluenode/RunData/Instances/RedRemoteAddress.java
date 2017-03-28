/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.RunData.Instances;

import kostiskag.unitynetwork.bluenode.Functions.getTime;

/**
 *
 * @author kostis
 */
public class RedRemoteAddress {

    private String VirtualAddress;
    private String Hostname;
    private String BlueNodeHostname;
    private String timestamp;

    public RedRemoteAddress(String VirtualAddress, String Hostname, String BlueNodeHostname, String timestamp) {
        this.VirtualAddress = VirtualAddress;
        this.Hostname = Hostname;
        this.BlueNodeHostname = BlueNodeHostname;        
        this.timestamp = timestamp;
    }

    public void init(String VirtualAddress, String Hostname, String BlueNodeHostname, String timestamp) {
        this.VirtualAddress = VirtualAddress;
        this.Hostname = Hostname;
        this.BlueNodeHostname = BlueNodeHostname;        
        this.timestamp = timestamp;
    }

    public String getVAddress() {
        return VirtualAddress;
    }

    public String getHostname() {
        return Hostname;
    }

    public String getBlueNodeHostname() {
        return BlueNodeHostname;
    }

    public String getTime() {
        return timestamp;
    }
    
    public void updateTime() {
        this.timestamp = getTime.getSmallTimestamp();
    }   
}