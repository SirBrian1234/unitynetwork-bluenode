package kostiskag.unitynetwork.bluenode.BlueNodeClient;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import static java.lang.Thread.sleep;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Functions.TCPSocketFunctions;
import kostiskag.unitynetwork.bluenode.RunData.Instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.Routing.*;

/**
 *
 * @author kostis
 */
public class BlueNodeClientFunctions {

    public static String pre = "^CLIENT ";

    public static void addRemoteBlueNode(String PhAddress, int authPort, String AuthHostname, boolean exclusive, boolean full) {
        BlueNodeInstance node = new BlueNodeInstance(PhAddress, authPort, AuthHostname, exclusive, full);        
        //leasing
        if (node.getStatus() == 1) {
            App.BlueNodesTable.lease(node);
        }
    }

    public static int getRemoteRedNodesU(String BlueNodeHostname) {
        String bnaddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return -1;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);
        
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return -1;
        }
        String RemoteHostname;
        RemoteHostname = args[1];

        TCPSocketFunctions.sendData("BLUENODE " + App.Hostname + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("GET_RED_NODES ", outputWriter, inputReader);
        int count = Integer.parseInt(args[1]);
        for (int i = 0; i < count; i++) {
            args = TCPSocketFunctions.readData(inputReader);
            if (App.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                App.remoteRedNodesTable.lease(args[0], args[1], BlueNodeHostname);
            } else {
                App.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
            }
        }
        TCPSocketFunctions.connectionClose(socket);
        return 1;
    }

    public static boolean checkBlueNode(String BlueNodeHostname) {
        String bnaddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return false;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return false;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);
        String RemoteHostname = null;
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return false;
        }
        RemoteHostname = args[1];
        args = TCPSocketFunctions.sendData("BLUENODE " + App.Hostname, outputWriter, inputReader);
        TCPSocketFunctions.sendFinalData("EXIT ", outputWriter);
        TCPSocketFunctions.connectionClose(socket);
        return true;
    }

    public static void removeBlueNodeProjection(int i) {
        String BlueNodeHostname = App.BlueNodesTable.getBlueNodeInstance(i).getHostname();
        String BlueNodeAddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(BlueNodeAddress);
        String[] args = null;
        if (IPaddress == null) {
            return;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        args = TCPSocketFunctions.readData(inputReader);
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return;
        }
        args = TCPSocketFunctions.sendData("BLUENODE " + App.Hostname, outputWriter, inputReader);
        if (args[0].equals("OK")) {
            TCPSocketFunctions.sendData("RELEASE", outputWriter, inputReader);
        }
        TCPSocketFunctions.connectionClose(socket);
    }

    public static void removeBlueNodeProjection(String BlueNodeHostname) {
        String BlueNodeAddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(BlueNodeAddress);
        String[] args = null;
        if (IPaddress == null) {
            return;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        args = TCPSocketFunctions.readData(inputReader);
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return;
        }
        args = TCPSocketFunctions.sendData("BLUENODE " + App.Hostname + " ", outputWriter, inputReader);
        if (args[0].equals("OK")) {
            TCPSocketFunctions.sendFinalData("RELEASE", outputWriter);
        }
        TCPSocketFunctions.connectionClose(socket);
    }

    //-1 host not found for error
    //0 for not found remote rednode
    //1 for found rednote
    public static int checkRemoteRedNode(String vaddress) {
        String BlueNodeHostname = App.remoteRedNodesTable.getRedRemoteAddress(vaddress).getBlueNodeHostname();
        String bnaddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return -1;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);

        String RemoteHostname = null;
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return -1;
        }
        RemoteHostname = args[1];

        TCPSocketFunctions.sendData("BLUENODE " + App.Hostname + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("CHECK " + vaddress + " ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);

        if (args[1].equals("ONLINE")) {
            return 1;
        } else {
            App.ConsolePrint(pre + "USER IS NOT CONNECTED TO BN " + BlueNodeHostname);
            return 0;
        }
    }

    public static boolean checkRemoteRedNodeAbsolute(String vaddress, String BlueNodeHostname) {
        String bnaddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);

        String RemoteHostname = null;
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return false;
        }
        RemoteHostname = args[1];

        TCPSocketFunctions.sendData("BLUENODE " + App.Hostname + " ", outputWriter, inputReader);
        TCPSocketFunctions.sendData("CHECK " + vaddress + " ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);

        if (args[1].equals("ONLINE")) {
            return true;
        } else {
            App.ConsolePrint(pre + "USER IS NOT KNOWN TO BN " + BlueNodeHostname);
            return false;
        }
    }

    public static int UPing(String BlueNodeHostname) {
        String bnaddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return -1;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);

        String RemoteHostname = null;
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return -2;
        }
        RemoteHostname = args[1];
        if (!BlueNodeHostname.equals(RemoteHostname)) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return -3;
        }

        TCPSocketFunctions.sendData("BLUENODE " + App.Hostname + " ", outputWriter, inputReader);
        TCPSocketFunctions.sendFinalData("UPING ", outputWriter);
        byte[] payload = ("00002 " + App.Hostname + " [UPING PACKET]").getBytes();
        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
        App.BlueNodesTable.getBlueNodeInstanceByHn(RemoteHostname).getQueueMan().offer(data);
        
        try {
            sleep(1700);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueNodeClientFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        args = TCPSocketFunctions.readData(inputReader);

        TCPSocketFunctions.connectionClose(socket);
        if (args[1].equals("OK")) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int DPing(String BlueNodeHostname) {
        String bnaddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return -1;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return -2;
        }
        String RemoteHostname = args[1];
        if (!BlueNodeHostname.equals(RemoteHostname)) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return -3;
        }
        TCPSocketFunctions.sendData("BLUENODE " + App.Hostname + " ", outputWriter, inputReader);

        App.dping = false;
        TCPSocketFunctions.sendData("DPING ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);
        
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueNodeClientFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (App.dping) {
            return 1;
        } else {
            return 0;
        }
    }

    public static boolean isSameHostname(String targetHostname) {
        if (App.Hostname.equals(targetHostname)) {                            
            App.ConsolePrint(pre + "BLUE NODES HAVE THE SAME NAME, QUITING");
            return true;
        } else {            
            return false;
        }
    }

    public static String getRedHostname(String BlueNodeHostname, String RedNodeAddress) {
        String bnaddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return "";
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return "";
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);
        String RemoteHostname = null;
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return "";
        }
        RemoteHostname = args[1];
        args = TCPSocketFunctions.sendData("BLUENODE " + App.Hostname + " ", outputWriter, inputReader);
        if (args[0].equals("OK")) {
            args = TCPSocketFunctions.sendData("GET_RED_HOSTNAME " + RedNodeAddress, outputWriter, inputReader);
            if (args[0].equals("ONLINE")) {
                TCPSocketFunctions.connectionClose(socket);
                return args[1];
            } else {
                TCPSocketFunctions.connectionClose(socket);
                return "OFFLINE";
            }
        } else {
            TCPSocketFunctions.connectionClose(socket);
            return "";
        }
    }

    static int ExchangeRedNodesU(String BlueNodeHostname) {
        String bnaddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return -1;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return -1;
        }

        TCPSocketFunctions.sendData("BLUENODE " + App.Hostname + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("EXCHANGE_RED_NODES ", outputWriter, inputReader);
        int count = Integer.parseInt(args[1]);
        for (int i = 0; i < count; i++) {
            args = TCPSocketFunctions.readData(inputReader);
            if (App.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                App.remoteRedNodesTable.lease(args[0], args[1], BlueNodeHostname);
            } else {
                App.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
            }
        }
        TCPSocketFunctions.readData(inputReader);

        int size = App.localRedNodesTable.getSize();
        TCPSocketFunctions.sendFinalData("SENDING_LOCAL_RED_NODES " + size, outputWriter);
        for (int i = 0; i < size; i++) {
            String vaddress = App.localRedNodesTable.getRedNodeInstance(i).getVaddress();
            String hostname = App.localRedNodesTable.getRedNodeInstance(i).getHostname();
            TCPSocketFunctions.sendFinalData(vaddress + " " + hostname, outputWriter);
        }
        TCPSocketFunctions.sendFinalData("", outputWriter);
        TCPSocketFunctions.connectionClose(socket);
        return 1;
    }

    public static int feedReturnRoute(String BlueNodeHostname, String vaddress) {
        String bnaddress = App.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.authport);
        if (socket == null) {
            return -1;
        }
        BufferedReader inputReader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter outputWriter = TCPSocketFunctions.makeWriteWriter(socket);
        String[] args = null;
        args = TCPSocketFunctions.readData(inputReader);

        String RemoteHostname = null;
        if (isSameHostname(args[1])) {
            TCPSocketFunctions.sendFinalData("NOTHING_TO_DO_HERE ", outputWriter);
            TCPSocketFunctions.connectionClose(socket);
            return -1;
        }
        RemoteHostname = args[1];

        TCPSocketFunctions.sendData("BLUENODE " + App.Hostname + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("FEED_RETURN_ROUTE " + vaddress + " ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);

        if (args[0].equals("OK")) {
            return 1;
        } else {
            App.ConsolePrint(pre + "USER IS NOT CONNECTED TO BN " + BlueNodeHostname);
            return 0;
        }
    }
}
