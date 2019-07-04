package org.kostiskag.unitynetwork.bluenode.rundata;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class IpPoll {
    
    int count;
    
    public IpPoll() {
        count = 0;
    }
    
    public synchronized int poll(){
        count++;
        return count;
    }
}
