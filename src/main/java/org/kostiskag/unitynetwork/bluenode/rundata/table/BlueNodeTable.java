package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.util.*;
import java.util.stream.Stream;
import java.util.concurrent.locks.Lock;
import java.net.UnknownHostException;

import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.table.NodeTable;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.AppLogger;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public final class BlueNodeTable extends NodeTable<PhysicalAddress, BlueNode> {

	private final String pre = "^REMOTE BLUENODE TABLE ";
	private static boolean INSTANTIATED;

	private final boolean verbose;
	private final boolean notifyGui;
	private final MainWindow window;

	/**
	 * Similar to a singleton, this pattern ensures that only one object of this class may be instantiated
	 * the difference is that it will only create one object reference as well and give it to the first caller!
	 *
	 * @param window either provide the MainWindow or null
	 * @return
	 */
	public static BlueNodeTable newInstance(MainWindow window) {
		//the only one who can get the only one reference is the first caller
		if (!INSTANTIATED) {
			INSTANTIATED = true;
			return new BlueNodeTable(true, window);
		}
		return null;
	}

	private BlueNodeTable(boolean verbose, MainWindow window) {
		this.verbose = verbose;
		this.notifyGui = window != null;
		this.window = window;
		verbose("INITIALIZED");
	}

	@Locking(LockingScope.NO_LOCK)
	private Stream<RemoteRedNode> getRemoteRedNodeStreamFromAll() {
		return super.nodes.stream().map(BlueNode::getTable).flatMap(t -> {
			Lock lock = null;
			try {
				lock = t.aquireLock();
				return t.getNodeStream(lock);
			} catch (InterruptedException e) {
				verbose(e.getLocalizedMessage());
				return Stream.empty();
			} finally {
				lock.unlock();
			}
		});
	}

	@Deprecated
	public BlueNode getBlueNodeInstanceByRemoteRedNodeVirtualAddress(String vaddress) {
		Lock lock = null;
		try {
			lock = aquireLock();
			return getBlueNodeInstanceByRemoteRedNodeVirtualAddress(lock, VirtualAddress.valueOf(vaddress)).get();
		} catch (InterruptedException | UnknownHostException e) {

		} finally {
			lock.unlock();
		}
		return null;
	}

	public Optional<BlueNode> getBlueNodeInstanceByRemoteRedNodeVirtualAddress(Lock lock, String vaddress) throws UnknownHostException, InterruptedException {
		return getBlueNodeInstanceByRemoteRedNodeVirtualAddress(lock, VirtualAddress.valueOf(vaddress));
	}

	public Optional<BlueNode> getBlueNodeInstanceByRemoteRedNodeVirtualAddress(Lock bnlock, VirtualAddress vaddress) throws InterruptedException {
		validateLock(bnlock);
		return getRemoteRedNodeStreamFromAll().filter(r -> r.getAddress().equals(vaddress)).map(RemoteRedNode::getBlueNode).findFirst();
	}

	public Optional<BlueNode> getBlueNodeInstanceByRRNHostname(Lock bnlock, String hostname) throws InterruptedException {
		validateLock(bnlock);
		return getRemoteRedNodeStreamFromAll().filter(r -> r.getHostname().equals(hostname)).map(RemoteRedNode::getBlueNode).findFirst();
	}

	public Optional<BlueNode> getBlueNodeInstanceByRRNHostname(Lock bnlock, RemoteRedNode rrn) throws InterruptedException {
		validateLock(bnlock);
		return getRemoteRedNodeStreamFromAll().filter(r -> r.equals(rrn)).map(RemoteRedNode::getBlueNode).findFirst();
	}

	@Deprecated
	@Locking(LockingScope.NO_LOCK)
	public boolean checkBlueNode(String name) {
		Lock lock = null;
		try {
			lock = aquireLock();
			Iterator<BlueNode> it = nodes.iterator();
			while (it.hasNext()) {
				BlueNode bn = it.next();
				if (bn.getHostname().equals(name)) {
					return true;
				}
			}
		} catch (InterruptedException e) {
			AppLogger.getInstance().consolePrint(e.getLocalizedMessage());
		} finally {
			lock.unlock();
		}
		return false;
	}

	@Deprecated
	public boolean checkRemoteRedNodeByHostname(String hostname) throws InterruptedException {
		Iterator<BlueNode> it = nodes.iterator();
		while(it.hasNext()){
			BlueNode bn = it.next();
			Lock lock = null;
			try {
				lock = bn.getTable().aquireLock();
				if (bn.getTable().getNodeStream(lock).anyMatch(r -> r.getHostname().equals(hostname))) {
					return true;
				}
			} finally {
				lock.unlock();
			}
		}
		return false;
	}

	@Deprecated
	public boolean checkRemoteRedNodeByVaddress(String vaddress) {
		Iterator<BlueNode> it = nodes.iterator();
		while(it.hasNext()) {
			Lock lock = null;
			try {
				BlueNode bn = it.next();
				lock = bn.getTable().aquireLock();
				if (bn.getTable().getOptionalNodeEntry(lock, VirtualAddress.valueOf(vaddress)).isPresent()) {
					return true;
				}
			} catch (InterruptedException | UnknownHostException e) {
				AppLogger.getInstance().consolePrint(e.getMessage());
			} finally {
				lock.unlock();
			}
		}
		return false;
	}

	//lease is applied at the end of the associate process
	@Locking(LockingScope.EXTERNAL)
	public void leaseBlueNode(Lock lock, BlueNode blueNode) throws InterruptedException, IllegalAccessException {
		validateLock(lock);
		//check if already exists
		if (super.nodes.add(blueNode)) {
			verbose("LEASED BLUE NODE " + blueNode.getHostname());
			notifyGUI();
		} else {
			throw new IllegalAccessException(pre+"DUPLICATE ENTRY "+blueNode+" ATTEMPTED TO JOIN BNTABLE.");
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void releaseBlueNode(Lock lock, String name) throws InterruptedException, IllegalAccessException {
		super.validateLock(lock);
		var o = super.getOptionalNodeEntry(lock, name);
		if (o.isPresent()) {
			releaseBlueNode(lock, o.get());
		} else {
			throw new IllegalAccessException("NO ENTRY FOR "+name+" IN TABLE");
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void releaseBlueNode(Lock lock, BlueNode blueNode) throws InterruptedException, IllegalAccessException {
		super.validateLock(lock);
		if (nodes.remove(blueNode)) {
			notifyGUI();
			verbose("RELEASED BLUE NODE " + blueNode.getHostname());
		} else {
			throw new IllegalAccessException("NON EXISTING BN "+blueNode);
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void leaseRemoteRedNode(Lock bLock, BlueNode blueNode, String hostname, VirtualAddress vaddress) throws InterruptedException, IllegalAccessException {
		validateLock(bLock);
		//check if already exists from all bns
		if (getRemoteRedNodeStreamFromAll().anyMatch(r -> r.getHostname().equals(hostname) || r.getAddress().equals(vaddress))) {
			throw new IllegalAccessException(pre+"DUPLICATE ENTRY FOR REMOTE RED NODE "+hostname+" "+vaddress);
		}

		//if we are clear apply it!
		Lock lock = null;
		try {
			lock = blueNode.getTable().aquireLock();
			blueNode.getTable().lease(lock, hostname, vaddress);
			notifyRGUI();
			verbose("leased Remote Red Node "+hostname+" on BN:"+blueNode.getHostname());
		} catch (InterruptedException e) {
			verbose(e.getLocalizedMessage());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * This is triggered when a BN needs to exit the network.
	 * In this case and since the bn may exit bn.killTasks();
	 * is not required.
	 *
	 * @Locking(LockingScope.INTERNAL) as it is safe to say this would be a single action!
	 */
	@Locking(LockingScope.INTERNAL)
	public void sendKillSigsAndReleaseForAll() {
		Lock lock = null;
		try {
			for (var bn : nodes) {
				try {
					bn.release();
					verbose("RELEASED BLUE NODE " + bn.getHostname());
				} catch (InterruptedException | IllegalAccessException e) {
					AppLogger.getInstance().consolePrint("Failed to release " + bn.getHostname());
				}
			}
			nodes.clear();
			notifyGUI();
		} finally {
			lock.unlock();
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void releaseLocalRedNodeByHostnameFromAll(Lock lock, String hostname) throws InterruptedException, IllegalAccessException {
		validateLock(lock);
		for (BlueNode b: nodes) {
			b.releaseRRn(hostname);
		}
	}

	/**
	 * This is triggered by the sonarService
	 * for all the associated blue nodes inside the table where the calling bn is
	 * a server this has to be called in order to detect dead entries and disconnected rrns
	 */
	//TODO
	@Locking(LockingScope.EXTERNAL)
	public void rebuildTableViaAuthClient(Lock lock) throws InterruptedException {
		validateLock(lock);
		for (var element : super.nodes) {
			if (element.isServer()) {
				try {
					Lock lock = null;
					try {
						lock = element.getTable().aquireLock();
						BlueNodeClient cl = new BlueNodeClient(element);
						if (cl.checkBlueNode()) {
							verbose("Fetching RNs from BN " + element.getHostname());
							element.updateTimestamp();
							cl = new BlueNodeClient(element);
							Collection<RemoteRedNode> rns = cl.getRemoteRedNodesObj();
							Stream<RemoteRedNode> in = element.getTable().getNodeStream(lock);
							Collection<RemoteRedNode> valid = new LinkedList<>();

							in.forEach(remoteRedNode -> {
								if (rns.contains(remoteRedNode)) {
									valid.add(remoteRedNode);
								}
							});

							element.getTable().updateTable(lock, valid);
						} else {
							element.killtasks();
							verbose("RELEASED NON RESPONDING BLUE NODE " + element.getHostname());
						}
					} finally {
						lock.unlock();
					}
				} catch (InterruptedException e) {
					element.killtasks();
					verbose("RELEASED BLUE NODE " + element.getHostname() +" FOR "+ e.getLocalizedMessage());
				}
			}
		}
		System.out.println(pre+" BN Table rebuilt");
		notifyGUI();
		notifyRGUI();
	}

	@Locking(LockingScope.EXTERNAL)
	public String[][] buildBNGUIObj(Lock lock) throws InterruptedException {
		validateLock(lock);
		return buildBNGUIObj();
	}

	@Locking(LockingScope.NO_LOCK)
	public String[][] buildBNGUIObj() {
		return (String[][]) super.getStream().map(bn -> new String[]{bn.getHostname(), bn.isTheRemoteAServer(), bn.getAddress().asString(), ""+bn.getRemoteAuthPort(),""+bn.getPortToSend(), ""+bn.getPortToReceive(), bn.getTimestamp().asDate().toString()}).toArray();
	}

	@Locking(LockingScope.EXTERNAL)
	public String[][] buildRRNGUIObj(Lock lock) throws InterruptedException {
		validateLock(lock);
		return buildRRNGUIObj();
	}

	@Locking(LockingScope.NO_LOCK)
	public String[][] buildRRNGUIObj() {
		return (String[][]) this.getRemoteRedNodeStreamFromAll().map(rn -> new String[]{rn.getHostname(), rn.getAddress().asString() , rn.getBlueNode().getHostname(), rn.getTimestamp().asDate().toString()}).toArray();
	}

	@Locking(LockingScope.EXTERNAL)
	public void notifyGUI (Lock lock) throws InterruptedException {
		validateLock(lock);
		notifyGUI();
	}

	@Locking(LockingScope.NO_LOCK)
	private void notifyGUI() {
		if (notifyGui) {
			window.updateBNs(buildBNGUIObj());
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void notifyRGUI (Lock lock) throws InterruptedException {
		validateLock(lock);
		notifyRGUI();
	}

	@Locking(LockingScope.NO_LOCK)
	private void notifyRGUI () {
		if (notifyGui) {
			window.updateRemoteRns(buildRRNGUIObj());
		}
	}

	@Locking(LockingScope.NO_LOCK)
	private void verbose(String message) {
		if (verbose) {
			AppLogger.getInstance().consolePrint(pre + message);
		}
	}
}
