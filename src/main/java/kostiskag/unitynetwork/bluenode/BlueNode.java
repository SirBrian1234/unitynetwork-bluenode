package kostiskag.unitynetwork.bluenode;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.FlyRegister;
import kostiskag.unitynetwork.bluenode.Routing.PacketToHandle;
import kostiskag.unitynetwork.bluenode.Routing.QueueManager;
import kostiskag.unitynetwork.bluenode.RunData.IpPoll;
import kostiskag.unitynetwork.bluenode.RunData.tables.AccountsTable;
import kostiskag.unitynetwork.bluenode.RunData.tables.BlueNodesTable;
import kostiskag.unitynetwork.bluenode.RunData.tables.LocalRedNodeTable;
import kostiskag.unitynetwork.bluenode.functions.PortHandle;
import kostiskag.unitynetwork.bluenode.socket.blueNodeService.BlueNodeServer;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackingBlueNodeFunctions;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackingTracker;

public class BlueNode extends Thread{
	private static final String pre = "^BlueNode ";
	// init settings
	public final boolean network;
	public final String trackerAddress;
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
	public final AccountsTable accounts;
	// gui
	public boolean[] viewType = new boolean[] { true, true, true, true };
	public boolean[] viewhostType = new boolean[] { true, true };
	public boolean autoScrollDown = true;
	// run data
	public boolean joined = false;
	public boolean dping;
	public int keepAliveTime = 5;
	public PortHandle UDPports;
	public TrackingTracker addr;
	// our most important tables
	public LocalRedNodeTable localRedNodesTable;
	public BlueNodesTable blueNodesTable;
	// tracker data
	public String echoAddress;
	public final int trackerCheckSec = 20;
	// gui data
	public boolean viewTraffic = true;
	private int messageCount = 0;	
	public IpPoll bucket;
	public PrintWriter prt;
	// objects
	public BlueNodeServer auth;
	public PacketToHandle router;
	public MainWindow window;
	public FlyRegister flyreg;
	public QueueManager manager;
	public QueueManager flyman;
	//
	public AtomicInteger trackerRespond = new AtomicInteger(0);

	/**
	 * This is a bluenode constructor. In a typical scenario
	 * there will be only one instance of this class
	 * accessible from App.bn . However in cases of network testing
	 * it is very useful to be in a position to start multiple bluenodes
	 * this is why a bluenode is an object.
	 * 
	 * @param network
	 * @param trackerAddress
	 * @param trackerPort
	 * @param name
	 * @param useList
	 * @param authPort
	 * @param startPort
	 * @param endPort
	 * @param maxRednodeEntries
	 * @param gui
	 * @param soutTraffic
	 * @param log
	 * @param accounts
	 */
	public BlueNode(
		boolean network,
		String trackerAddress, 
		int trackerPort, 
		int trackerMaxIdleTimeMin,
		String name,
		boolean useList,
		int authPort,
		int startPort,
		int endPort,
		int maxRednodeEntries,
		boolean gui,
		boolean soutTraffic,        
		boolean log,
		AccountsTable accounts	
	) {
		this.network = network;
		this.trackerAddress = trackerAddress;
		this.trackerPort = trackerPort;
		this.trackerMaxIdleTimeMin = trackerMaxIdleTimeMin;
		this.name = name;
		this.useList = useList;
		this.authPort = authPort;
		this.startPort = startPort;
		this.endPort = endPort;
		this.maxRednodeEntries = maxRednodeEntries;
		this.gui = gui;
		this.soutTraffic = soutTraffic;
		this.log = log;
		this.accounts = accounts;
	}
	
	@Override
	public void run() {
		super.run();
		System.out.println(pre + "started BlueNode at thread " + Thread.currentThread().getName());
		// 1. gui goes first to verbose init on print
		if (gui) {
			window = new MainWindow();
			window.setVisible(true);
			window.setBlueNodeInfo();
		}
		
		// 2. initializing bluenodes data
		UDPports = new PortHandle(startPort, endPort);
		localRedNodesTable = new LocalRedNodeTable(maxRednodeEntries);
		blueNodesTable = new BlueNodesTable();

		// 3. init packet queues
		manager = new QueueManager(200);
		flyman = new QueueManager(1000);

		// 3. init auth server ~ the service that authenticates clients
		auth = new BlueNodeServer(authPort);
		auth.start();

		// 5. init router ~ basically blue node's router
		router = new PacketToHandle();
		router.start();

		// 6. init urouter ~ unknown router the one that manages new hosts new
		// packets and stuff
		// flyreg is pointless to work unless in a network
		if (network) {
			flyreg = new FlyRegister();
			flyreg.start();
		}

		// 7. lease with the network or use predefined users
		if (network) {
			try {
				if (joinNetwork()) {
					joined = true;
					addr = new TrackingTracker();
					addr.start();
				} else {
					die();
				}
			} catch (Exception e) {
				e.printStackTrace();
				die();
			}
		} else if (!useList) {
			bucket = new IpPoll();
			ConsolePrint("WARNING! BLUENODE DOES NOT USE EITHER NETWORK NOR A USERLIST\nWHICH MEANS THAT ANYONE WHO KNOWS THE BN'S ADDRESS AND IS PHYSICALY ABLE TO CONNECT CAN LOGIN");
		}
	}

	public void ConsolePrint(String Message) {
		if (gui) {
			System.out.println(Message);
			window.jTextArea1.append(Message + "\n");
		} else {
			System.out.println(Message);
		}

		if (log) {
			App.writeToLogFile(Message);
		}
	}

	/**
	 *  Prints a message to traffic console. 
	 *  
	 *  @param messageType ~ 0 keep alive, 1 pings, 2 acks, 3 routing
	 *  @param hostType ~ 0 reds, 1 blues
	 */
	public void TrafficPrint(String Message, int messageType, int hostType) {
		if (gui) {
			if (viewTraffic) {
				if (viewType[messageType] == true && viewhostType[hostType] == true) {
					messageCount++;
					window.jTextArea2.append(Message + "\n");
				}
			}
			if (messageCount > 10000) {
				messageCount = 0;
				window.jTextArea2.setText("");
			}
			if (autoScrollDown) {
				window.jTextArea2.select(window.jTextArea2.getHeight() + 10000, 0);
			}
		} else if (soutTraffic) {
			System.out.println(Message);
		}
	}

	

	public boolean joinNetwork() throws Exception {
		if (network && !name.isEmpty() && authPort > 0 && authPort <= 65535) {
			int leased = TrackingBlueNodeFunctions.lease(name, authPort);
			if (leased > 0) {
				ConsolePrint("^SUCCESFULLY REGISTERED WITH THE NETWORK");
				return true;
			} else {
				if (leased == -2) {
					ConsolePrint("^FAILED TO REGISTER WITH THE NETWORK, HOST NOT FOUND");
				} else if (leased == -1) {
					ConsolePrint("^FAILED TO REGISTER WITH THE NETWORK, NOT REGISTERED BN");
				} else if (leased == 0) {
					ConsolePrint("^FAILED TO REGISTER WITH THE NETWORK, LEASE FAILED");
				}
				return false;
			}
		} else {
			throw new Exception("bad credentials");
		}
	}

	public void leaveNetworkAndDie() throws Exception {
		if (joined) {
			//release from tracker
			TrackingBlueNodeFunctions.release();
			//release from bns
			blueNodesTable.sendKillSigsAndReleaseForAll();
			joined = false;
			addr.Kill();
			die();
		} else {
			throw new Exception("called leaveNetwork whithout being joined.");
		}
	}

	public void die() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		System.exit(1);
	}
}
