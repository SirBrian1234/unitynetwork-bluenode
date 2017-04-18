package kostiskag.unitynetwork.bluenode.Routing;

import java.util.LinkedList;

/**
 *
 * @author kostis
 */
public class QueueManager extends Thread {
    private final int maxCapacity;
    private final LinkedList<byte[]> queue;
    
    /**
     * This constructor can be used from the bluenode and for each 
     * local rednode or bluenode instance.
     * 
     * @param blueNode
     * @param maxCapacity
     */
    public QueueManager(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        queue = new LinkedList<byte[]>();
    }
    
   public synchronized void offer(byte[] data) {        

        while(queue.size() == maxCapacity) {
            try {
                wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        queue.add(data);        
        notify();
    }

    public synchronized byte[] poll() {
        while(queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ex) {
            	ex.printStackTrace();
            }
        }

        byte[] data  = queue.poll();        
        notify();
        return data;
    }
    
    public synchronized void clear(){
        queue.clear();        
        notify();        
    }

    public int getlen() {
        return queue.size();
    }
}
