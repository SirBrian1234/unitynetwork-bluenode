package org.kostiskag.unitynetwork.bluenode.service;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public final class NextIpPoll {

    private static NextIpPoll IP_POLL;

    private int count;

    public static NextIpPoll newInstance()  {
        if (IP_POLL == null) {
            IP_POLL = new NextIpPoll();
        }
        return IP_POLL;
    }

    public static NextIpPoll getInstance() {
        return IP_POLL;
    }

    private NextIpPoll() {
        count = 0;
    }
    
    public synchronized int poll(){
        return ++count;
    }
}
