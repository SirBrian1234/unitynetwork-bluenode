package kostiskag.unitynetwork.bluenode.Routing;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kostis
 */

public class QueueManager extends Thread {
    private final int capacity;
    private Queue<byte[]> queue;

    public QueueManager(int capacity) {
        this.capacity = capacity;
        queue = new LinkedList();
    }        

    public synchronized void offer(byte[] data) {        

        while(queue.size() == capacity) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(QueueManager.class.getName()).log(Level.SEVERE, null, ex);
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
