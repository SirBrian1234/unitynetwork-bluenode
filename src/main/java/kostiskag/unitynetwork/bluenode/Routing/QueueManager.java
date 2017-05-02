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
	 * This constructor can be used from the bluenode and for each local rednode
	 * or bluenode instance.
	 * 
	 * @param blueNode
	 * @param maxCapacity
	 */
	public QueueManager(int maxCapacity) {
		this.maxCapacity = maxCapacity;
		queue = new LinkedList<byte[]>();
	}

	public synchronized int getlen() {
		return queue.size();
	}

	public synchronized int getSpace() {
		if (queue.size() <  maxCapacity) {
			return maxCapacity - queue.size();
		} else {
			return 0;
		}		
	}
	
	public synchronized boolean hasSpace() {
		if (queue.size() <  maxCapacity) {
			return true;
		} else {
			return false;
		}	
	}
	
	/**
	 * Offer may make the calling thread to WAIT until empty
	 * 
	 * @param data
	 */
	public synchronized void offer(byte[] data) {
		boolean kill = this.kill.get();
		while (queue.size() == maxCapacity && !kill) {
			try {
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		if (kill) {
			return;
		}
		queue.add(data);
		notify();
	}

	/**
	 * A thread which is not allowed to wait may call this first. This method
	 * REPLACES the oldest packet with a new one if the queue is full
	 * 
	 * @return
	 */
	public synchronized void offerNoWait(byte[] data) {
		if (queue.size() < maxCapacity) {
			queue.add(data);
			notify();
		} else {
			queue.poll();
			queue.add(data);
			notify();
		}
	}

	/**
	 * In order to not poll something which may not be
	 * later sent we can poll first. Determine where the packet goes
	 * and then poll.
	 * 
	 * @return
	 */
	public synchronized byte[] peek() {
		while (queue.isEmpty() && !kill.get()) {
			try {
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		if (kill.get()) {
			return null;
		}
		byte[] data = queue.peek();
		return data;
	}
	
	public synchronized byte[] poll() {
		while (queue.isEmpty() && !kill.get()) {
			try {
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

		if (kill.get()) {
			return null;
		}

		byte[] data = queue.poll();
		notify();
		return data;
	}

	public synchronized void clear() {
		queue.clear();
		notifyAll();
	}

	public synchronized void exit() {
		kill.set(true);
		queue.clear();
		notifyAll();
	}

	
}
