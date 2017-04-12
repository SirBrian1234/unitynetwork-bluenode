package kostiskag.unitynetwork.bluenode.RunData.Instances;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.RedThreads.RedDownService;
import kostiskag.unitynetwork.bluenode.RedThreads.RedKeepAlive;
import kostiskag.unitynetwork.bluenode.RedThreads.RedlUpService;
import kostiskag.unitynetwork.bluenode.Routing.QueueManager;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import kostiskag.unitynetwork.bluenode.Functions.ipAddrFunctions;
import kostiskag.unitynetwork.bluenode.TrackClient.TrackingRedNodeFunctions;

/** 
 * RedAuthService runs every time for a single user only It is responsible for
 * user registering user's uplink port fishing and it stays alive as long as the
 * user is connected
 * 
 * @author kostis
 */
public class RedNodeInstance extends Thread {

	//to check if a RN is connected in another BN before auth
	private String pre = "^AUTH ";
    //object data
    private String PhAddressStr;
    private InetAddress PhAddress;
    private String Vaddress;
    private String Hostname;
    private String Username;
    private String Password;
    private int state = 0;
    private boolean uping = false;
    //object socket objs
    private Socket socket;
    private BufferedReader inFromClient;
    private PrintWriter outputWriter;
    //object threads queue
    private RedDownService down;
    private RedlUpService up;
    private RedKeepAlive ka;
    private QueueManager man;
    private static Boolean didTrigger = false;

    public RedNodeInstance() {
        state = 0;
    }
    
    public RedNodeInstance(String Hostname, String Username, String Password, String Vaddress, String PhAddressStr) {
        state = 0;
        this.Username = Username;
        this.Hostname = Hostname;
        this.Password = Password;
        this.Vaddress = Vaddress;
        this.PhAddressStr = PhAddressStr;
    }

    public RedNodeInstance(Socket socket, String Hostname, String Username, String Password) {

        this.Hostname = Hostname;
        this.Username = Username;
        this.socket = socket;

        App.bn.ConsolePrint(pre + "STARTING A REDNODE AUTH AT " + Thread.currentThread().getName());
        String[] args;

        try {
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputWriter = new PrintWriter(socket.getOutputStream(), true);

            PhAddress = socket.getInetAddress();
            PhAddressStr = PhAddress.getHostAddress();

            if (App.bn.network && App.bn.joined) {
                Vaddress = TrackingRedNodeFunctions.lease(Hostname, Username, Password);
                
                //leasing - reverse error capture     
                if (Vaddress.equals("WRONG_COMMAND")) {
                    App.bn.ConsolePrint(pre + "WRONG_COMMAND");
                    outputWriter.println("BLUENODE FAILED");
                    socket.close();
                    state = -1;
                    return;
                } else if (Vaddress.equals("NOT_ONLINE")) {
                    App.bn.ConsolePrint(pre + "NOT_ONLINE");
                    outputWriter.println("BLUENODE FAILED");
                    socket.close();
                    state = -1;
                    return;
                } else if (Vaddress.equals("NOT_REGISTERED")) {
                    App.bn.ConsolePrint(pre + "NOT_REGISTERED");
                    outputWriter.println("BLUENODE FAILED");
                    socket.close();
                    state = -1;
                    return;
                } else if (Vaddress.equals("SYSTEM_ERROR")) {
                    App.bn.ConsolePrint(pre + "SYSTEM_ERROR");
                    outputWriter.println("BLUENODE FAILED");
                    socket.close();
                    state = -1;
                    return;
                } else if (Vaddress.equals("AUTH_FAILED")) {
                    App.bn.ConsolePrint(pre + "USER FAILED 1");
                    outputWriter.println("USER FAILED 1");
                    socket.close();
                    state = -1;
                    return;
                } else if (Vaddress.equals("USER_HOSTNAME_MISSMATCH")) {
                    App.bn.ConsolePrint(pre + "HOSTNAME FAILED 3");
                    outputWriter.println("HOSTNAME FAILED 3");
                    socket.close();
                    state = -1;
                    return;
                } else if (Vaddress.equals("ALLREADY_LEASED")) {
                    App.bn.ConsolePrint(pre + "HOSTNAME FAILED 2");
                    outputWriter.println("HOSTNAME FAILED 2");
                    socket.close();
                    state = -1;
                    return;
                } else if (Vaddress.equals("NOT_FOUND")) {
                    App.bn.ConsolePrint(pre + "HOSTNAME FAILED 1");
                    outputWriter.println("HOSTNAME FAILED 1");
                    socket.close();
                    state = -1;
                    return;
                } else if (Vaddress.equals("LEASE_FAILED")) {
                    App.bn.ConsolePrint(pre + "HOSTNAME FAILED 1");
                    outputWriter.println("HOSTNAME FAILED 1");
                    socket.close();
                    state = -1;
                    return;
                }
                                
            } else if (App.bn.useList) {
                Vaddress = App.bn.accounts.search(Hostname, Username, Password);
                Vaddress = ipAddrFunctions.numberTo10ipAddr(Vaddress);
            } else if (!App.bn.useList && App.bn.network) {
                Vaddress = App.bn.kouvas.poll();
                Vaddress = ipAddrFunctions.numberTo10ipAddr(Vaddress);
            } else {
            	Vaddress = null;
            	socket.close();
            	state = -1;
            	return;
            }
                   
            App.bn.ConsolePrint(pre + "USER AUTHED / STARTING ASSOSIATION");

            if (App.bn.gui && didTrigger == false) {
                MainWindow.jCheckBox2.setSelected(true);
                didTrigger = true;
            }

            //queue manager
            man = new QueueManager(10);

            //downlink (allways by the aspect of bluenode)
            down = new RedDownService(Vaddress);

            //uplink (allways by the aspect of bluenode)
            up = new RedlUpService(Vaddress);

            //keep alive
            ka = new RedKeepAlive(Vaddress);

            state = 1;
            
        } catch (IOException ex) {
            Logger.getLogger(RedNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * here we have the terminal loop a user may
     * send commands to the BN monitoring his status
     */
    public void startServices() {
        down.start();
        up.start();
        ka.start();
    }

    public void initTerm() {
        if (state > 0) {
            while (true) {
                String clientSentence = null;
                try {
                    clientSentence = inFromClient.readLine();
                } catch (java.net.SocketException ex1) {
                    break;
                } catch (IOException ex) {
                    Logger.getLogger(RedNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
                    break;
                }
                if (clientSentence == null) {
                    forceExit();
                    break;
                }
                if (clientSentence.startsWith("PING")) {
                    outputWriter.println("PING OK");
                } else if (clientSentence.startsWith("UPING")) {
                    boolean set = false;
                    for (int i = 0; i < 12; i++) {
                        try {
                            sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(RedNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (getUPing()) {
                            outputWriter.println("UPING OK");
                            set = true;
                            break;
                        }
                    }
                    if (set == false) {
                        outputWriter.println("UPING FAILED");
                    }
                    setUPing(false);

                } else if (clientSentence.startsWith("DPING")) {
                    if (getQueueMan() != null) {

                        byte[] payload = "00001 [DPING PACKET]".getBytes();
                        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);

                        for (int i = 0; i < 2; i++) {
                            getQueueMan().offer(data);
                        }
                        outputWriter.println("PING ON THE WAY");
                    } else {
                        outputWriter.println("BLUE NODE ERROR");
                        App.bn.ConsolePrint(pre + "NO QUEUE FOUND FOR " + Vaddress + " HOST KILLED");
                        killTasks();
                        break;
                    }
                } else if (clientSentence.startsWith("DREFRESH")) {
                    App.bn.ConsolePrint(pre + " " + Vaddress + " UP REFRESH");
                    urefresh();
                } else if (clientSentence.startsWith("UREFRESH")) {
                    App.bn.ConsolePrint(pre + Vaddress + " DOWN REFRESH");
                    drefresh();
                } else if (clientSentence.startsWith("WHOAMI")) {
                    whoami();
                } else if (clientSentence.startsWith("EXIT")) {
                    exit();
                    break;
                } else {
                    //not recognized command
                    outputWriter.println("NRC");
                }
            }
            try {
                socket.close();
            } catch (IOException ex) {
                App.bn.ConsolePrint(pre + "USER FORCE EXITED");
                forceExit();
            }
        }
    }

    private void whoami() {
        outputWriter.println(Username + "/" + Hostname + "/" + Vaddress + " ~ " + PhAddressStr + ":" + up.getUpport() + ":" + down.getDownport());
    }

    private void urefresh() {
        up.kill();

        up = new RedlUpService(Vaddress);
        up.start();

        try {
            sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(RedNodeInstance.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        outputWriter.println("DOWNLINK REFRESH " + up.getUpport());
        App.bn.localRedNodesTable.updateTable();
    }

    private void drefresh() {
        down.kill();
        down = new RedDownService(Vaddress);
        down.start();

        try {
            sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(RedNodeInstance.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        outputWriter.println("UPLINK REFRESH " + down.getDownport());
        App.bn.localRedNodesTable.updateTable();
    }

    public void exit() {
        //killing auth socket
        outputWriter.println("BYE");
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(RedNodeInstance.class.getName()).log(Level.SEVERE, null, ex);
        }

        //killing user tasks
        killTasks();
        //releasing host from table
        App.bn.localRedNodesTable.release(Vaddress);
        //informing user black node
        if (App.bn.network)
            TrackingRedNodeFunctions.release(Hostname);
    }

    private void forceExit() {
        //killing user tasks
        killTasks();
        state = -2;
        //releasing host from table
        App.bn.localRedNodesTable.release(Vaddress);
        //informing user black node
        TrackingRedNodeFunctions.release(Hostname);
    }

    private void killTasks() {
        //killing user tasks
        down.kill();
        up.kill();
        ka.kill();
    }

    //this function is started by red node table when an entry is force deleted
    public void forceDelete() {
        killTasks();
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(RedNodeInstance.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        TrackingRedNodeFunctions.release(Hostname);
    }

    public int getStatus() {
        return state;
    }

    public String getHostname() {
        return Hostname;
    }

    public String getVaddress() {
        return Vaddress;
    }

    public String getPhAddress() {
        return PhAddressStr;
    }

    public InetAddress getRealPhAddress() {
        return PhAddress;
    }

    public String getUsername() {
        return Username;
    }

    public QueueManager getQueueMan() {
        return man;
    }

    public boolean isUPinged() {
        return uping;
    }

    public void setUPing(boolean b) {
        this.uping = b;
    }

    private boolean getUPing() {
        return uping;
    }

    public RedlUpService getUp() {
        return up;
    }

    public RedDownService getDown() {
        return down;
    }
}
