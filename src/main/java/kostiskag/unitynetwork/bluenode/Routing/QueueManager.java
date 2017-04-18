package kostiskag.unitynetwork.bluenode.Routing;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author kostis
 */
public class QueueManager extends Thread {
    private final int maxCapacity;
    private final LinkedList<byte[]> queue;
    private final AtomicBoolean kill = new AtomicBoolean(false);
    
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

        while(queue.size() == maxCapacity && !kill.get()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        if (kill.get()) {
        	return;
        }

        queue.add(data);        
        notify();
    }

    public synchronized byte[] poll() {
        while(queue.isEmpty() && !kill.get()) {
            try {
                wait();
            } catch (InterruptedException ex) {
            	ex.printStackTrace();
            }
        }
        
        if (kill.get()) {
        	return null;
        }

        byte[] data  = queue.poll();        
        notify();
        return data;
    }
    
    public synchronized void clear(){
        queue.clear();        
        notifyAll();        
    }
    
    public synchronized void exit(){
    	kill.set(true);
    	queue.clear();
    	notifyAll();
    }

    public int getlen() {
        return queue.size();
    }
}
