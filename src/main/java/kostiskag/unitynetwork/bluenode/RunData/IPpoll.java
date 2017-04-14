package kostiskag.unitynetwork.bluenode.RunData;

/**
 *
 * @author kostis
 */
public class IPpoll {
    
    int count;
    
    public IPpoll() {
        count = 0;
    }
    
    public synchronized int poll(){
        count++;
        return count;
    }
}
