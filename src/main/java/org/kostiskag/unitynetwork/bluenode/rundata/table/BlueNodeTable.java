package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Consumer;
import java.util.concurrent.locks.Lock;
import java.util.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.table.NodeTable;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.RemoteRedNode;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeClient;
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
	private final Consumer<String[][]> BNconsumer;
	private final Consumer<String[][]> RRNconsumer;


	/**
	 * Similar to a singleton, this pattern ensures that only one object of this class may be instantiated
	 * the difference is that it will only create one object reference as well and give it to the first caller!
	 *
	 *
	 * @return
	 */
	public static BlueNodeTable newInstance(Consumer<String[][]> BNconsumer, Consumer<String[][]> RRNconsumer) {
		//the only one who can get the only one reference is the first caller
		if (!INSTANTIATED) {
			INSTANTIATED = true;
			return new BlueNodeTable(true, BNconsumer, RRNconsumer);
		}
		return null;
	}

	private BlueNodeTable(boolean verbose, Consumer<String[][]> BNconsumer, Consumer<String[][]> RRNconsumer) {
		this.verbose = verbose;
		this.notifyGui = BNconsumer != null && RRNconsumer != null;
		this.BNconsumer = BNconsumer;
		this.RRNconsumer = RRNconsumer;
		verbose("INITIALIZED");
	}

	/*
		BLUENODE RELATED METHODS
	 */

	@Locking(LockingScope.NO_LOCK)
	private Map<Boolean, List<BlueNode>> splitByIsServer() {
		return super.getStream().collect(Collectors.partitioningBy(bn -> bn.isServer()));
	}

	@Locking(LockingScope.NO_LOCK)
	private List<BlueNode> getServerBlueNodes() {
		return splitByIsServer().get(true);
	}

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
		var o = super.getOptionalEntry(lock, name);
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

	/**
	 * This is triggered when a BN needs to exit the network.
	 * In this case and since the bn may exit bn.killTasks();
	 * is not required.
	 *
	 * @Locking(LockingScope.INTERNAL) as it is safe to say this would be a single action!
	 */
	@Locking(LockingScope.INTERNAL)
	public void sendKillSigsAndReleaseAll() {
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

	/*
		REMOTE REDNODE METHODS
	 */

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

	@Locking(LockingScope.EXTERNAL)
	public void leaseRemoteRedNode(Lock bLock, BlueNode blueNode, RemoteRedNode rrn) throws InterruptedException, IllegalAccessException {
		Lock lock = null;
		try {
			lock = blueNode.getTable().aquireLock();
			blueNode.getTable().lease(lock, rrn);
			notifyRGUI();
			verbose("leased Remote Red Node "+rrn.getHostname()+" on BN:"+blueNode.getHostname());
		} finally {
			lock.unlock();
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public void leaseRemoteRedNode(Lock bLock, BlueNode blueNode, String hostname, VirtualAddress address) throws InterruptedException, IllegalAccessException {
		var newRrn = RemoteRedNode.newInstance(hostname, address, blueNode);
		leaseRemoteRedNode(bLock, blueNode, newRrn);
	}

	/**
	 * This is triggered by the sonarService
	 * for all the associated blue nodes inside the table where the calling bn is
	 * a server this has to be called in order to detect dead entries and disconnected rrns
	 */
	@Locking(LockingScope.INTERNAL)
	public void rebuildTableViaAuthClient() throws InterruptedException, IllegalAccessException, IOException, GeneralSecurityException {
		Lock bLock = null;
		try {
			bLock = this.aquireLock();
			for (var bn: nodes) {
				Lock lock = null;
				try {
					lock = bn.getTable().aquireLock();
					BlueNodeClient cl = new BlueNodeClient(bn);
					if (cl.checkBlueNode()) {
						verbose("Fetching RNs from BN " + bn.getHostname());
						cl = new BlueNodeClient(bn);
						Collection<RemoteRedNode> fetched = cl.getRemoteRedNodesObj();
						bn.getTable().renewAll(lock, fetched, false);
						bn.updateTimestamp();
						verbose("Fetch complete! "+bn.getHostname()+" on:"+bn.getTimestamp().asDate().toString());
					} else {
						bn.killTasks();
						verbose("RELEASED NON RESPONDING BLUE NODE " + bn.getHostname());
					}
				} finally {
					lock.unlock();
				}
			}
			verbose("BN Table rebuilt");
			notifyGUI();
			notifyRGUI();
		} finally {
			bLock.unlock();
		}
	}

	/*
		GET BLUENODE THROUGH REMOTE REDNODE
	 */

	@Locking(LockingScope.EXTERNAL)
	public Optional<BlueNode> getBlueNodeEntryByRemoteRedNode(Lock bnlock, RemoteRedNode rrn) throws InterruptedException {
		validateLock(bnlock);
		return getRemoteRedNodeStreamFromAll().filter(r -> r.equals(rrn)).map(RemoteRedNode::getBlueNode).findFirst();
	}

	@Locking(LockingScope.EXTERNAL)
	public Optional<BlueNode> getBlueNodeEntryByRemoteRedNode(Lock bnlock, String hostname) throws InterruptedException {
		validateLock(bnlock);
		return getRemoteRedNodeStreamFromAll().filter(r -> r.getHostname().equals(hostname)).map(RemoteRedNode::getBlueNode).findFirst();
	}

	@Locking(LockingScope.EXTERNAL)
	public Optional<BlueNode> getBlueNodeEntryByRemoteRedNode(Lock bnlock, VirtualAddress vaddress) throws InterruptedException {
		validateLock(bnlock);
		return getRemoteRedNodeStreamFromAll().filter(r -> r.getAddress().equals(vaddress)).map(RemoteRedNode::getBlueNode).findFirst();
	}

	@Locking(LockingScope.EXTERNAL)
	public void releaseLocalRedNodeProjectionFromAll(Lock lock, String hostname) throws InterruptedException, IllegalAccessException {
		validateLock(lock);
		for (BlueNode b: nodes) {
			b.releaseLocalRedNodeProjection(hostname);
		}
	}

	@Locking(LockingScope.EXTERNAL)
	public String[][] buildBNGUIObj(Lock lock) throws InterruptedException {
		validateLock(lock);
		return buildBNGUIObj();
	}

	@Locking(LockingScope.NO_LOCK)
	private String[][] buildBNGUIObj() {
		return (String[][]) super.getStream().map(bn -> new String[]{bn.getHostname(), bn.isTheRemoteAServer(), bn.getAddress().asString(), ""+bn.getRemoteAuthPort(),""+bn.getPortToSend(), ""+bn.getPortToReceive(), bn.getTimestamp().asDate().toString()}).toArray();
	}

	@Locking(LockingScope.EXTERNAL)
	public String[][] buildRRNGUIObj(Lock lock) throws InterruptedException {
		validateLock(lock);
		return buildRRNGUIObj();
	}

	@Locking(LockingScope.NO_LOCK)
	private String[][] buildRRNGUIObj() {
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
			BNconsumer.accept(buildBNGUIObj());
			//window.updateBNs(buildBNGUIObj());
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
			RRNconsumer.accept(buildRRNGUIObj());
			//window.updateRemoteRns(buildRRNGUIObj());
		}
	}

	@Locking(LockingScope.NO_LOCK)
	private void verbose(String message) {
		if (verbose) {
			AppLogger.getInstance().consolePrint(pre + message);
		}
	}
}
