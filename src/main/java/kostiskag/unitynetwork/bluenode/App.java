package kostiskag.unitynetwork.bluenode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import kostiskag.unitynetwork.bluenode.TrackClient.TrackingDynamicAddress;
import kostiskag.unitynetwork.bluenode.BlueNodeService.BlueNodeServer;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.FlyRegister;
import kostiskag.unitynetwork.bluenode.Routing.PacketToHandle;
import kostiskag.unitynetwork.bluenode.Routing.QueueManager;
import kostiskag.unitynetwork.bluenode.RunData.Tables.BlueNodesTable;
import kostiskag.unitynetwork.bluenode.Functions.PortHandle;
import kostiskag.unitynetwork.bluenode.RunData.IPpoll;
import kostiskag.unitynetwork.bluenode.RunData.Tables.AccountsTable;
import kostiskag.unitynetwork.bluenode.RunData.Tables.RedNodesTable;
import kostiskag.unitynetwork.bluenode.RunData.Tables.RedRemoteAddressTable;

/*
 *
 * @author Konstantinos Kagiampakis
 * 
 */
public class App extends Thread {

    //data
    public static boolean[] viewType = new boolean[]{true, true, true, true};
    public static boolean[] viewhostType = new boolean[]{true, true};
    public static boolean autoScrollDown = true;
    public static boolean network = true;
    public static boolean UseList;
    public static boolean log;
    private String pre = "^BlueNode ";
    //basic arguments
    public static String Hostname;
    public static int authport;
    public static int startport;
    public static int endport;
    public static int hostnameEntries;
    //run data    
    public static boolean dping;
    public static int keepAliveTime = 5;
    public static PortHandle UDPports;
    //our most important tables
    public static RedNodesTable localRedNodesTable;
    public static RedRemoteAddressTable remoteRedNodesTable;
    public static BlueNodesTable BlueNodesTable;
    public static AccountsTable accounts;
    public static TrackingDynamicAddress addr;
    //threads
    public static BlueNodeServer auth;
    public static PacketToHandle router;
    public static MainWindow window;
    //tracker data
    public static String Taddr = "127.0.0.1";
    public static int Tport = 8000;
    public static boolean joined = false;
    public static String echoAddress;
    public static int DynAddUpdateMins = 5;
    //gui data
    public static boolean gui = false;
    public static boolean viewTraffic = true;
    private static int messageCount = 0;
    public static FlyRegister flyreg;
    public static QueueManager manager;
    public static QueueManager flyman;
    public static boolean soutTraffic = false;
    public static boolean autologin = false;
    public static IPpoll kouvas;
    public static PrintWriter prt;
    //files
    public static File logFile;

    //first starts main then this one
    public App() {
        System.out.println(pre + "started BlueNode at thread " + Thread.currentThread().getName());

        //0. init bluenode.log
        if (log) {
            logFile = new File("bluenode.log");
            FileWriter fw;
            try {
                fw = new FileWriter(logFile, false);                
                fw.write("---------------------------------------------------------------\n");
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //1. gui goes first to verbose init on print
        if (gui) {
            window = new MainWindow();
            window.setVisible(true);
            window.refreshinfo();
        }

        //2. initializing bluenodes data
        UDPports = new PortHandle(startport, endport);
        localRedNodesTable = new RedNodesTable(hostnameEntries);
        BlueNodesTable = new BlueNodesTable(hostnameEntries * 10);
        remoteRedNodesTable = new RedRemoteAddressTable(hostnameEntries * 10);

        //3. init packet queues
        manager = new QueueManager(200);
        flyman = new QueueManager(1000);

        //3. init auth server ~ the service that authenticates clients
        auth = new BlueNodeServer();
        auth.start();

        //5. init router ~ basically blue node's router
        router = new PacketToHandle();
        router.start();

        //6. init urouter ~ unknown router the one that manages new hosts new packets and stuff
        //flyreg is pointless to work unless in a network
        if (App.network) {
            flyreg = new FlyRegister();
            flyreg.start();
        }

        //7. lease with the network or use predefined users
        if (App.network) {
            if (App.autologin) {
                lease();
            }
        } else {
            if (App.UseList) {
                LoadUserList();
            } else {
                kouvas = new IPpoll();
            }
        }
    }

    public static void ConsolePrint(String Message) {
        if (gui) {
            System.out.println(Message);
            window.jTextArea1.append(Message + "\n");
        } else {
            System.out.println(Message);
        }

        if (log) {
            FileWriter fw;
            try {
                fw = new FileWriter(logFile, true);
                fw.append(Message + "\n");
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //messagetype ~ 0 keep alive, 1 pings, 2 acks, 3 routing 
    //hosttype ~ 0 reds, 1 blues
    public static void TrafficPrint(String Message, int messageType, int hostType) {
        if (gui) {
            if (viewTraffic == true) {
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

    public static void LoadUserList() {
        if (App.UseList && !App.network) {
            try {
                accounts = new AccountsTable();
                kostiskag.unitynetwork.bluenode.Functions.ReadPreferencesFile.ParseList(new File("users.list"));
                accounts.verbose();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                App.die();
            }
        } else {
            System.err.println("Error in running " + Thread.currentThread().getName() + " called load user list when it was not ready. this may be a bug");
        }

    }

    public static void lease() {
        if (App.network && App.Hostname != null && App.authport > 0) {
            int leased = kostiskag.unitynetwork.bluenode.TrackClient.TrackingBlueNodeFunctions.lease(App.Hostname, App.authport);
            if (leased > 0) {
                App.ConsolePrint("^SUCCESFULLY REGISTERED WITH THE NETWORK");
                App.joined = true;
                App.window.joined();
                App.addr = new TrackingDynamicAddress();
                App.addr.start();
            } else {
                if (leased == -2) {
                    App.ConsolePrint("^FAILED TO REGISTER WITH THE NETWORK, HOST NOT FOUND");
                }
                if (leased == -1) {
                    App.ConsolePrint("^FAILED TO REGISTER WITH THE NETWORK, NOT REGISTERED BN");
                }
                if (leased == 0) {
                    App.ConsolePrint("^FAILED TO REGISTER WITH THE NETWORK, LEASE FAILED");
                }
                App.joined = false;
            }
        } else {
            System.err.println("Error in running " + Thread.currentThread().getName() + " called lease() when it was not ready. this may be a bug");
        }
    }

    public static boolean leave() {
        if (App.joined) {
            if (kostiskag.unitynetwork.bluenode.TrackClient.TrackingBlueNodeFunctions.release()) {
                App.ConsolePrint("^SUCCESSFULLY RELEASED FROM THE NETWORK");
            }
            App.joined = false;
            App.addr.Kill();
            MainWindow.left();
            return true;
        } else {
            System.err.println("Error in running " + Thread.currentThread().getName() + " called leave() whithout prejoin. this may be a bug");
        }
        return false;
    }

    public static void die() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueNodeServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(1);
    }

    //we do not use args anymore... we use bluenode.conf
    //you cant use consoleprint on main!!! only sout
    //grave mistakes are being punished with exit(1) not die()
    public static void main(String argv[]) {
        System.out.println("@Started main at " + Thread.currentThread().getName());

        System.out.println("reading bluenode.conf...");
        try {
            InputStream file = new FileInputStream("bluenode.conf");
            kostiskag.unitynetwork.bluenode.Functions.ReadPreferencesFile.ParseFile(file);
        } catch (FileNotFoundException ex) {
            System.err.println("File not found make sure there is bluenode.conf file in your dir");
            System.exit(1);
        }

        if (gui) {
            System.out.println("checking gui libraries...");
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                } catch (Exception ex) {
                    try {
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                    } catch (Exception ex1) {
                        System.err.println("although asked for gui there are no supported libs on this machine");
                        System.err.println("fix it or disable gui from the bluenode.conf");
                        System.exit(1);
                    }
                }
            }
        }

        System.out.println("starting BlueNode...");
        App bn = new App();
    }
}
