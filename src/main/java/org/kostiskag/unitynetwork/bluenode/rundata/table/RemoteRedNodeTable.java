package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.Bluenode;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.table.NodeTable;

/**
 * Here we keep all the remote red nodes leased by each blue node.
 * In other words, every object of BlueNodeInstance keeps an object of this class.
 *
 * @author Konstantinos Kagiampakis
 */
public class RemoteRedNodeTable extends NodeTable<VirtualAddress, RemoteRedNode> {

	private final String pre = "^REDNODE REMOTE TABLE ";
	private final BlueNode blueNode;
	private final boolean verbose;
	private final boolean notifyGui;

	public RemoteRedNodeTable(BlueNode blueNode, Collection<RemoteRedNode> rrnSet) {
		this(blueNode, rrnSet, true, true);
	}

	public RemoteRedNodeTable(BlueNode blueNode) {
		this(blueNode, Collections.emptySet(), true, true);
	}

	public RemoteRedNodeTable(BlueNode blueNode, Collection<RemoteRedNode> rrnSet, boolean verbose, boolean notifyGui) {
		super(rrnSet);
		this.blueNode = blueNode;
		this.verbose = verbose;
		this.notifyGui = notifyGui;
		verbose("INITIALIZED FOR "+blueNode.getHostname());
	}

	@Locking(LockingScope.NO_LOCK)
	public BlueNode getBlueNode() {
		return blueNode;
	}

	@Locking(LockingScope.EXTERNAL)
	public Stream<RemoteRedNode> getNodeStream(Lock lock) throws InterruptedException {
		validateLock(lock);
		return nodes.stream();
	}

	@Locking(LockingScope.EXTERNAL)
	public int getSize(Lock lock) throws InterruptedException {
		validateLock(lock);
		return nodes.size();
	}

	@Locking(LockingScope.EXTERNAL)
	public void lease(Lock lock, String hostname, VirtualAddress vAddress) throws IllegalAccessException, InterruptedException {
		var r = RemoteRedNode.newInstance(hostname, vAddress, blueNode);
		lease(lock, r);
	}

	@Locking(LockingScope.EXTERNAL)
	public void lease(Lock lock, RemoteRedNode rn) throws IllegalAccessException, InterruptedException {
		validateLock(lock);
		if (nodes.add(rn)) {
			verbose("LEASED " + rn.getHostname() + " - " + rn.getAddress() + " ON BLUE NODE " + blueNode.getHostname());
			notifyGUI();
		} else {
			throw new IllegalAccessException("element already exists on the table!");
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void release(Lock lock , String hostname) throws InterruptedException, IllegalAccessException {
		validateLock(lock);
		var opt = super.getOptionalEntry(lock, hostname);
		if(opt.isPresent()) {
			release(lock, opt.get());
		} else {
			throw new IllegalAccessException(pre + "NO ENTRY FOR " + hostname + " IN TABLE");
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void release(Lock lock, VirtualAddress vAddress) throws IllegalAccessException, InterruptedException {
		validateLock(lock);
		var opt = super.getOptionalEntry(lock, vAddress);
		if(opt.isPresent()) {
			release(lock, opt.get());
		} else {
			throw new IllegalAccessException(pre + "NO ENTRY FOR " + vAddress.asString() + " IN TABLE");
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void release(Lock lock, RemoteRedNode r) throws IllegalAccessException, InterruptedException {
		validateLock(lock);
		if (nodes.remove(r)) {
			verbose("RELEASED " + r.getHostname() + " - " + r.getAddress().asString() + " FROM BLUE NODE " + blueNode.getHostname());
			notifyGUI();
		} else {
			throw new IllegalAccessException(pre + "NO ENTRY FOR " + r.getHostname() + " IN TABLE");
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void renewAll(Lock lock, Collection<RemoteRedNode> rrns, boolean notifyGui) throws IllegalAccessException, InterruptedException {
		validateLock(lock);
		if (rrns.stream().distinct().count() != rrns.size()) {
			throw new IllegalAccessException("duplicate elements found");
		}
		nodes.clear();
		nodes.addAll(rrns);
		verbose("RENEWED ALL RRN ENTRIES FOR BLUENODE "+blueNode.getHostname());
		if (notifyGui) {
			notifyGUI();
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void removeAll(Lock lock, boolean notifyGui) throws InterruptedException {
		validateLock(lock);
		nodes.clear();
		verbose("REMOVED ALL ENTRIES FOR BLUENODE "+blueNode.getHostname());
		if (notifyGui) {
			notifyGUI();
		}
	}

	@Locking(LockingScope.NO_LOCK)
	private void verbose(String message) {
		if (verbose) {
			AppLogger.getInstance().consolePrint(pre + message);
		}
	}

	//Warning the annotation is right!
	//The lock is for the parent!!!
	@Locking(LockingScope.NO_LOCK)
	private void notifyGUI () {
		if (notifyGui) {
			Lock lock = null;
			try {
				lock = Bluenode.getInstance().blueNodeTable.aquireLock();
				Bluenode.getInstance().blueNodeTable.notifyRGUI(lock);
			} catch (InterruptedException e) {
				verbose(e.getLocalizedMessage());
			} finally {
				lock.unlock();
			}
		}
	}
}
