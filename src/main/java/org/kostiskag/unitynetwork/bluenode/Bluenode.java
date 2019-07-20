package org.kostiskag.unitynetwork.bluenode;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;

import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

import org.kostiskag.unitynetwork.bluenode.routing.FlyRegister;
import org.kostiskag.unitynetwork.bluenode.rundata.table.AccountTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.BlueNodeTable;
import org.kostiskag.unitynetwork.bluenode.rundata.table.LocalRedNodeTable;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeclient.BlueNodeSonarService;
import org.kostiskag.unitynetwork.bluenode.service.bluenodeservice.BlueNodeServer;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerClient;
import org.kostiskag.unitynetwork.bluenode.service.trackclient.TrackerTimeBuilder;
import org.kostiskag.unitynetwork.bluenode.service.PortHandle;
import org.kostiskag.unitynetwork.bluenode.service.NextIpPoll;
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
		TRACKER_MAX_IDLE_TIME(5),
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

	public static Bluenode getInstance() {
		return BLUENODE;
	}

	// initial configuration settings
	public final boolean network;
	public final PhysicalAddress trackerAddress;
	public final int trackerPort;
	public final int trackerMaxIdleTimeMin;
	public final String name;
	public final boolean useList;
	public final int authPort;
	public final int startPort;
	public final int endPort;
	public final int maxRednodeEntries;
	public final boolean gui;
	public final boolean soutTraffic;
	public final boolean log;
	public final AccountTable accounts;
	public final KeyPair bluenodeKeys;
	public final PublicKey trackerPublicKey;

	// run data
	public boolean joined = false;

	// these references should be removed from here
	public LocalRedNodeTable localRedNodesTable; //make singleton
	public BlueNodeTable blueNodeTable; //make singleton


	private Bluenode(
		ReadBluenodePreferencesFile prefs,
		AccountTable accounts,
		KeyPair bluenodeKeys,
		PublicKey trackerPublicKey
	) {
		this.network = prefs.network;
		this.trackerAddress = prefs.trackerAddress;
		this.trackerPort = prefs.trackerPort;
		this.trackerMaxIdleTimeMin = prefs.trackerMaxIdleTimeMin;
		this.name = prefs.name;
		this.useList = prefs.useList;
		this.authPort = prefs.authPort;
		this.startPort = prefs.startPort;
		this.endPort = prefs.endPort;
		this.maxRednodeEntries = prefs.maxRednodeEntries;
		this.gui = prefs.gui;
		this.soutTraffic = prefs.soutTraffic;
		this.log = prefs.log;
		this.accounts = accounts;
		this.bluenodeKeys = bluenodeKeys;
		this.trackerPublicKey = trackerPublicKey;

		System.out.println(pre + "started BlueNode at thread " + Thread.currentThread().getName());
		
		/*
		 *  1. gui goes first to verbose the following on console
		 */
		if (gui) {
			boolean trackerKeySet = trackerPublicKey != null;
			MainWindow.MainWindowPrefs mainWindowPrefs = new MainWindow.MainWindowPrefs(
					this.name,
					this.authPort,
					this.maxRednodeEntries,
					this.startPort,
					this.endPort,
					this.network,
					trackerKeySet,
					this.useList);
			MainWindow.newInstance(mainWindowPrefs);
		}

		// 2. Logger
		AppLogger.newInstance(this.gui, this.log, this.soutTraffic);
		
		//rsa public key
		AppLogger.getInstance().consolePrint("Your public key is:\n" + CryptoUtilities.bytesToBase64String(bluenodeKeys.getPublic().getEncoded()));

		// 3. Porthandler
		PortHandle.newInstance(startPort, endPort);

		/*
		 *  2. Initialize table
		 */
		localRedNodesTable = new LocalRedNodeTable(maxRednodeEntries);
		if (network) {
			blueNodeTable = new BlueNodeTable();
		}

		/*
		 *  3. Initialize auth server 
		 *  
		 *  The service to receive responses from RBNs RNs Tracker
		 */
		BlueNodeServer.newInstance(authPort);
		
		/* 
		 * 4. Initialize sonar
		 * 
		 * sonarService periodically checks the remote BNs associated as clients
		 * whereas the timeBuilder keeps track of the remote BNs associated as servers
		 * 
		 */
		if (network) {
			try {
				BlueNodeSonarService.newInstance(Timings.BLUENODE_CHECK_TIME.getWaitTimeInSec());
			} catch (IllegalAccessException e) {
				AppLogger.getInstance().consolePrint(e.getMessage());
			}
		}

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
		if (network) {
			FlyRegister.newInstance();
		}

		/*
		 *  6. lease with the network, use a predefined user's list or dynamically allocate
		 *  virtual addresses to connected RNs
		 */
		if (network) {
			try {
				if (joinNetwork()) {
					joined = true;
					TrackerClient.configureTracker(this.name, this.trackerPublicKey, this.trackerAddress, this.trackerPort);
					TrackerTimeBuilder.newInstance(Timings.TRACKER_CHECK_TIME.getWaitTimeInSec()).start();
				} else {
					AppLogger.getInstance().consolePrint("This bluenode is not connected in the network.");
				}
			} catch (IllegalAccessException e) {
				AppLogger.getInstance().consolePrint(pre + " " + e.getMessage());
				die();
			}
		} else if (!useList) {
			NextIpPoll.newInstance();
			AppLogger.getInstance().consolePrint("WARNING! BLUENODE DOES NOT USE EITHER NETWORK NOR A USERLIST\nWHICH MEANS THAT ANYONE WHO KNOWS THE BN'S ADDRESS AND IS PHYSICALY ABLE TO CONNECT CAN LOGIN");
		}
	}

	private boolean joinNetwork() throws IllegalAccessException {
		if (trackerPublicKey == null) {
			AppLogger.getInstance().consolePrint("You do not have an availlable public key for the given tracker.\n" +
					"In order to download the key press the Collect Tracker Key button and follow the guide.\n" +
					"After you have a Public Key for the provided tracker you may restart the bluenode.");
			return false;
		} else if (network && !name.isEmpty() && authPort > 0 && authPort <= NumericConstraints.MAX_ALLOWED_PORT_NUM.size()) {
			TrackerClient tr = new TrackerClient();
			boolean leased = tr.leaseBn(authPort);
			if (tr.isConnected() && leased) {
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

	public void leaveNetworkAndDie() throws IllegalAccessException {
		if (joined) {
			//release from tracker
			TrackerClient tr = new TrackerClient();
			tr.releaseBn();
		}
		leave();
	}
	
	public void leaveNetworkAfterRevoke() throws IllegalAccessException {
		leave();
	}

	private void leave() throws IllegalAccessException {
		if (joined) {
			//release from bns
			blueNodeTable.sendKillSigsAndReleaseForAll();
			joined = false;
			TrackerTimeBuilder.getInstance().kill();
			die();
		} else {
			throw new IllegalAccessException("called leaveNetwork whithout join first.");
		}
	}

	public void die() {
		AppLogger.getInstance().consolePrint("Blue Node "+name+" is going to die.");
		System.exit(1);
	}

	public void updateTrackerPublicKey(PublicKey trackerPublic) {
		try {
			App.writeTrackerPublicKey(trackerPublic);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
