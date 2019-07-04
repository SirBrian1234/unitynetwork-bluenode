package org.kostiskag.unitynetwork.bluenode.Routing;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class QueuePair extends Thread {
    private final int maxCapacity;
    private final LinkedList<SourceDestPair> queue;
    private final AtomicBoolean kill = new AtomicBoolean(false);
    
    /**
     * This constructor can be used from the bluenode and for each 
     * local rednode or bluenode instance.
     * 
     * @param blueNode
     * @param maxCapacity
     */
    public QueuePair(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        queue = new LinkedList<SourceDestPair>();
    }
    
    public synchronized int getlen() {
        return queue.size();
    }
    
    /**
    * this should never wait
    * and that's because the calling thread is the router's
    * thread, otherwise bye bye routing for a few seconds
    */
    public synchronized void offer(SourceDestPair newPair) {        
	   if (queue.size() < maxCapacity ) {
		   queue.add(newPair);
	   } else {
		   queue.poll();
		   queue.add(newPair);
	   }	   
       notifyAll();
    }

    public synchronized SourceDestPair poll() {
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

        SourceDestPair pair = queue.poll();        
        return pair;
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
}
