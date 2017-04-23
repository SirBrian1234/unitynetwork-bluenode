package kostiskag.unitynetwork.bluenode;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.DnsServer;
import kostiskag.unitynetwork.bluenode.Routing.FlyRegister;
import kostiskag.unitynetwork.bluenode.Routing.Router;
import kostiskag.unitynetwork.bluenode.Routing.QueueManager;
import kostiskag.unitynetwork.bluenode.RunData.IpPoll;
import kostiskag.unitynetwork.bluenode.RunData.tables.AccountsTable;
import kostiskag.unitynetwork.bluenode.RunData.tables.BlueNodesTable;
import kostiskag.unitynetwork.bluenode.RunData.tables.LocalRedNodeTable;
import kostiskag.unitynetwork.bluenode.blueThreads.BlueNodeTimeBuilder;
import kostiskag.unitynetwork.bluenode.functions.PortHandle;
import kostiskag.unitynetwork.bluenode.socket.blueNodeClient.BlueNodeSonarService;
import kostiskag.unitynetwork.bluenode.socket.blueNodeService.BlueNodeServer;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerClient;
import kostiskag.unitynetwork.bluenode.socket.trackClient.TrackerTimeBuilder;

public class BlueNode extends Thread{
	private static final String pre = "^BlueNode ";
	// Initial settings
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
	// gui data
	public boolean viewTraffic = true;
	private int messageCount = 0;	
	public IpPoll bucket;
	public PrintWriter prt;
	public boolean[] viewType = new boolean[] { true, true, true, true };
	public boolean[] viewhostType = new boolean[] { true, true };
	public boolean autoScrollDown = true;
	// tracker data
	public String echoAddress;
	// run data
	public boolean joined = false;	
	public PortHandle UDPports;	
	// timings
	// tracker
	public final int trackerCheckSec = 20;
	public final int trackerMaxIdleTime = 5;
	// bluenodes
	public final int blueNodeTimeStepSec = 20;
	public final int blueNodeCheckTimeSec = 120;
	public final int blueNodeMaxIdleTimeSec = 2*blueNodeCheckTimeSec;	
	// our most important tables
	public LocalRedNodeTable localRedNodesTable;
	public BlueNodesTable blueNodesTable;
	// objects
	public MainWindow window;
	public BlueNodeServer auth;
	public Router router;	
	public DnsServer dns;
	public FlyRegister flyreg;
	public QueueManager manager;
	public BlueNodeSonarService bnSornar;
	public BlueNodeTimeBuilder bnTimeBuilder;
	public TrackerTimeBuilder trackerTimeBuilder;
	// triggers
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
		
		/*
		 *  1. gui goes first to verbose the following on console
		 */
		if (gui) {
			window = new MainWindow();
			window.setVisible(true);
			window.setBlueNodeInfo();
		}
		
		/*
		 *  2. Initialize tables
		 */
		UDPports = new PortHandle(startPort, endPort);
		localRedNodesTable = new LocalRedNodeTable(maxRednodeEntries);
		if (network) {
			blueNodesTable = new BlueNodesTable();
		}

		/*
		 *  3. Initialize packet queues
		 */
		manager = new QueueManager(200);
		
		/*
		 *  3. Initialize auth server 
		 *  
		 *  The service to receive responses from RBNs RNs Tracker
		 */
		auth = new BlueNodeServer(authPort);
		auth.start();

		/*
		 *  5. Initialize a router object 
		 *  
		 *  basically a blue node's router
		 */
		router = new Router();
		router.start();
		
		/*
		 * 6. Init DNS
		 * 
		 * DNS is essential for the internal platform's rooting
		 * 
		 */
		dns = new DnsServer();
		dns.start();
		
		/* 
		 * 7. Initialize sonar
		 * 
		 * sonarService periodically checks the remote BNs associated as clients
		 * whereas the timeBuilder keeps track of the remote BNs associated as servers
		 * 
		 */
		if (network) {
			bnSornar = new BlueNodeSonarService(blueNodeCheckTimeSec);
			bnSornar.start();
		}

		/* 
		 * 8. Initialize Register On The Fly
		 * 
		 *  when a packet heading to an unknown destination is received
		 *  the FlyReg may do all the tasks in order to dynamically build 
		 *  the path from this BN to a remote BN where the target RN exists,
		 *  unknown router the one that manages new hosts new
		 *  FlyReg is meaningful to work only in a network.
		 *  
		 */
		if (network) {
			flyreg = new FlyRegister();
			flyreg.start();
		}

		/*
		 *  9. lease with the network, use a predefined user's list or dynamically allocate
		 *  virtual addresses to connected RNs
		 */
		if (network) {
			try {
				if (joinNetwork()) {
					joined = true;
					trackerTimeBuilder = new TrackerTimeBuilder();
					trackerTimeBuilder.start();
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
			TrackerClient tr = new TrackerClient();			
			boolean leased = tr.leaseBn(authPort);
			if (tr.isConnected() && leased) {
				ConsolePrint("^SUCCESFULLY REGISTERED WITH THE NETWORK");
				return true;
			} else {
				if (!tr.isConnected()) {
					ConsolePrint("^FAILED TO REGISTER WITH THE NETWORK, HOST NOT FOUND");
				} else {
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
			TrackerClient tr = new TrackerClient();	
			tr.releaseBn();			
			//release from bns
			blueNodesTable.sendKillSigsAndReleaseForAll();
			joined = false;
			trackerTimeBuilder.Kill();
			die();
		} else {
			throw new Exception("called leaveNetwork whithout join first.");
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
