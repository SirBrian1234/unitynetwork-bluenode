package org.kostiskag.unitynetwork.bluenode;

import java.util.Optional;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.function.BooleanSupplier;

import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.state.PublicKeyState;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

import org.kostiskag.unitynetwork.bluenode.routing.FlyRegister;
import org.kostiskag.unitynetwork.bluenode.rundata.table.AccountTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeSonarService;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeservice.BlueNodeServer;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.service.PortHandle;
import org.kostiskag.unitynetwork.bluenode.service.NextIpPoll;
import org.kostiskag.unitynetwork.bluenode.gui.CollectTrackerKeyView;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;


/**
 * 
 * @author Konstantinos Kagiampakis
 */
public final class Bluenode {
	private static final String pre = "^BlueNode ";
	private static Bluenode BLUENODE;

	// global timings
	public enum Timings {
		KEEP_ALIVE_TIME(10),
		TRACKER_CHECK_TIME(20),
		TRACKER_MAX_IDLE_TIME(5 * 60),
		BLUENODE_STEP_TIME(20),
		BLUENODE_CHECK_TIME(120),
		BLUENODE_MAX_IDLE_TIME(2 * BLUENODE_CHECK_TIME.getWaitTimeInSec());

		private int waitTimeInSec;

		Timings(int waitTimeInSec) {
			this.waitTimeInSec = waitTimeInSec;
		}

		public int getWaitTimeInSec() {
			return waitTimeInSec;
		}
	}

	//new instance, effective only once!
	public static void newInstance(ReadBluenodePreferencesFile inPrefs,
								   AccountTable inAccounts,
								   KeyPair inBluenodeKeys,
								   PublicKey inTrackerPublicKey)  {

		if (BLUENODE == null) {
			BLUENODE = new Bluenode(inPrefs,
					inAccounts,
					inBluenodeKeys,
					inTrackerPublicKey);
		}
	}

	//TODO to be permanently removed, all its calls should become injected objs instead
	public static Bluenode getInstance() {
		return BLUENODE;
	}

	// initial configuration settings
	private final boolean networkMode;
	private final PhysicalAddress trackerAddress;
	private final int trackerMaxIdleTimeMin;
	private final String name;
	private final boolean listMode;
	private final int authPort;
	private final int startPort;
	private final int endPort;
	private final int maxRednodeEntries;
	private final boolean gui;
	private final boolean soutTraffic;
	private final boolean log;
	private final KeyPair bluenodeKeys;
	private final int trackerPort;
	private final AccountTable accounts;
	//NOT FINAL!!!
	private PublicKey trackerPublicKey;

	// run data
	private boolean joined;

	//TODO security breach public modifiers, to make those injected objs
	public LocalRedNodeTable localRedNodesTable;
	public BlueNodeTable blueNodeTable;
	private BlueNodeSonarService blueNodeSonarService;
	private BlueNodeServer blueNodeServer;

	//our method "tickets"
	//what is extremely useful about them is that we can distribute them
	//as dependency injections and therefore to not have to sent the whole bn object
	//which results in a more secure environment
	private final Runnable bluenodeTerminate = () -> this.terminate();
	private final boolean isNetworkMode = this.isNetworkMode();
	private final BooleanSupplier isJoinedNetwork = () -> this.isJoinedNetwork();


	private Bluenode(
		ReadBluenodePreferencesFile prefs,
		AccountTable accounts,
		KeyPair bluenodeKeys,
		PublicKey trackerPublicKey
	) {
		this.networkMode = prefs.network;
		this.trackerAddress = prefs.trackerAddress;
		this.trackerPort = prefs.trackerPort;
		this.trackerMaxIdleTimeMin = prefs.trackerMaxIdleTimeMin;
		this.name = prefs.name;
		this.listMode = prefs.useList;
		this.authPort = prefs.authPort;
		this.startPort = prefs.startPort;
		this.endPort = prefs.endPort;
		this.maxRednodeEntries = prefs.maxRednodeEntries;
		this.gui = prefs.gui;
		this.soutTraffic = prefs.soutTraffic;
		this.log = prefs.log;
		this.accounts = accounts;
		this.bluenodeKeys = bluenodeKeys;
		//from all of these data only tracker public key may be null!
		this.trackerPublicKey = trackerPublicKey;

		System.out.println(pre + "started BlueNode at thread " + Thread.currentThread().getName());

		// 1. initial data distribution
		if (networkMode) {
			if (this.trackerPublicKey != null) {
				//The bn has never requested tracker's public!
				TrackerClient.configureTracker(this.name, this.bluenodeKeys, this.trackerPublicKey, this.trackerAddress, this.trackerPort);
			}
			CollectTrackerKeyView.configureView(Optional.ofNullable(this.trackerPublicKey));
		}

		/*
		 *  2. gui goes first to verbose the following on console
		 */
		if (gui) {
			boolean trackerKeySet = trackerPublicKey == null;
			MainWindow.MainWindowPrefs mainWindowPrefs = new MainWindow.MainWindowPrefs(

					//this this
					this.name,
					this.authPort,
					this.maxRednodeEntries,
					this.startPort,
					this.endPort,
					this.networkMode,
					trackerKeySet,
					this.listMode);
			MainWindow.newInstance(mainWindowPrefs, bluenodeTerminate);
		}

		// 3. Logger (logger needs gui to be set in the case it is enabled)
		AppLogger.newInstance(this.gui, this.log, this.soutTraffic);

		//verbose rsa public key
		AppLogger.getInstance().consolePrint("Your public key is:\n" + CryptoUtilities.bytesToBase64String(bluenodeKeys.getPublic().getEncoded()));

		// 4. Porthandler
		PortHandle.newInstance(startPort, endPort);

		/*
		 *  5. Initialize tables
		 */
		this.localRedNodesTable = LocalRedNodeTable.newInstance(maxRednodeEntries);
		if (networkMode) {
			this.blueNodeTable = BlueNodeTable.newInstance();
		}

		/*
		 *  6. lease with the network, use a predefined user's list or dynamically allocate
		 *  virtual addresses to connected RNs
		 */
		try {
			if (networkMode) {
				if (joinNetwork()) {
					/*
					 * 5. Initialize Register On The Fly
					 *
					 *  when a packet heading to an unknown destination is received
					 *  the FlyReg may do all the tasks in order to dynamically build
					 *  the path from this BN to a remote BN where the target RN exists,
					 *  unknown router the one that manages new hosts new
					 *  FlyReg is meaningful to work only in a network.
					 *
					 */
					FlyRegister.newInstance(this.localRedNodesTable, this.blueNodeTable);

					/*
					 * 4. Initialize sonar
					 *
					 * sonarService periodically checks the remote BNs associated as clients
					 * whereas the timeBuilder keeps track of the remote BNs associated as servers
					 *
					 */
					blueNodeSonarService = BlueNodeSonarService.newInstance(this.blueNodeTable, Timings.BLUENODE_CHECK_TIME.getWaitTimeInSec());

					/*
					 *  7. Finally. Initialize auth server so that the BN may accept clients
					 *
					 */
					blueNodeServer = BlueNodeServer.newInstance(this.name, this.localRedNodesTable, this.blueNodeTable, this.bluenodeKeys, this.trackerPublicKey, Timings.TRACKER_CHECK_TIME.getWaitTimeInSec(), authPort, this.bluenodeTerminate);
				} else {
					AppLogger.getInstance().consolePrint("This bluenode is not connected in the network.");
				}
			} else if (isListMode()) {
				/*
				 *  Finally. Initialize auth server so that the BN may accept clients
				 *
				 */
				this.blueNodeServer = BlueNodeServer.newInstance(this.name, this.accounts, this.localRedNodesTable, authPort, this.bluenodeTerminate);
				AppLogger.getInstance().consolePrint(pre + "USES A LIST MODE!");

			} else if (isPlainMode()) {
				NextIpPoll.newInstance();
				/*
				 *  Finally. Initialize auth server so that the BN may accept clients
				 *
				 */
				this.blueNodeServer = BlueNodeServer.newInstance(this.name, localRedNodesTable, authPort, this.bluenodeTerminate);
				AppLogger.getInstance().consolePrint("WARNING! BLUENODE DOES NOT USE EITHER NETWORK NOR A USERLIST\nWHICH MEANS THAT ANYONE WHO KNOWS THE BN'S ADDRESS AND IS PHYSICALY ABLE TO CONNECT CAN LOGIN");
			}
		} catch (IOException | IllegalAccessException e) {
			AppLogger.getInstance().consolePrint(pre + " " + e.getMessage());
			terminate();
		}
	}

	private boolean joinNetwork() throws IllegalAccessException {
		if (trackerPublicKey == null) {
			AppLogger.getInstance().consolePrint("You do not have an availlable public key for the given tracker.\n" +
					"In order to download the key press the Collect Tracker Key button and follow the guide.\n" +
					"After you have a Public Key for the provided tracker you may restart the bluenode.");
			return false;
		} else if (networkMode && !name.isEmpty() && authPort > 0 && authPort <= NumericConstraints.MAX_ALLOWED_PORT_NUM.size()) {
			TrackerClient tr = new TrackerClient();
			boolean leased = tr.leaseBn(authPort);
			if (tr.isConnected() && leased) {
				this.joined = true;
				AppLogger.getInstance().consolePrint("^SUCCESSFULLY REGISTERED WITH THE NETWORK");
				return true;
			} else {
				AppLogger.getInstance().consolePrint("^FAILED TO REGISTER WITH THE NETWORK");
				return false;
			}
		} else {
			throw new IllegalAccessException("bad credentials");
		}
	}

	public void updateTrackerPublicKey() {
		//update only a null key
		if (this.trackerPublicKey == null) {
			PublicKey pub = TrackerClient.getTrackersPublicKey(trackerAddress.asInet(), trackerPort);
			//sanitize
			if (pub == null) return;
			//store
			try {
				App.writeTrackerPublicKey(pub);
				//In this mode, you are only allowed to upload your pub key
				this.trackerPublicKey = pub;
				TrackerClient.configureTracker(this.name, this.bluenodeKeys, this.trackerPublicKey, this.trackerAddress, this.trackerPort);
				MainWindow.getInstance().enableUploadPublicKey();
				AppLogger.getInstance().consolePrint("Tracker key was collected! Please upload the bluenode's public key next.");
			} catch (IOException e) {
				AppLogger.getInstance().consolePrint("Could not store tracker's public key into a file after successful fetch! !" +e.getLocalizedMessage());
			}
		}
	}

	public PublicKeyState offerPubKey(String ticket) {
		PublicKeyState response = TrackerClient.offerPubKey(this.name, ticket, this.trackerPublicKey, this.trackerAddress.asInet(), this.trackerPort);
		if (response.equals(PublicKeyState.KEY_SET) || response.equals(PublicKeyState.KEY_IS_SET)) {
			AppLogger.getInstance().consolePrint("Your public key has been uploaded to the tracker.\nPlease restart this BlueNode in order to connect.");
		}
		return response;
	}

	//TODO
	public void revokePubKey(String ticket) {

	}

	public void terminate() {
		silentTerminate();
		exit();
	}

	public void silentTerminate() {
		if (isListMode() || isPlainMode() || isJoinedNetwork()) {
			this.blueNodeServer.kill();
		}
		localRedNodesTable.exitAll();

		if (isJoinedNetwork()) {
			FlyRegister.getInstance().kill();
			this.blueNodeSonarService.kill();

			new TrackerClient().releaseBn();
			blueNodeTable.sendKillSigsAndReleaseForAll();
			joined = false;
		}
	}

	private void exit() {
		AppLogger.getInstance().consolePrint("Blue Node "+name+" is going to exit.");
		System.exit(1);
	}

	public String getName() {
		return name;
	}

	public boolean isGui() {
		return gui;
	}

	public boolean isNetworkMode() {
		return this.networkMode;
	}

	public boolean isJoinedNetwork() {
		return this.networkMode && this.joined;
	}

	public boolean isListMode() {
		return this.listMode;
	}

	public boolean isPlainMode() {
		return !this.listMode && !this.networkMode;
	}

}
