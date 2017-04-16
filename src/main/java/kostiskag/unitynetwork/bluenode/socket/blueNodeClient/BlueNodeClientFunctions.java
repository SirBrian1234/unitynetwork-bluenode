package kostiskag.unitynetwork.bluenode.socket.blueNodeClient;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import static java.lang.Thread.sleep;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.Routing.*;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;
import kostiskag.unitynetwork.bluenode.socket.GlobalSocketFunctions;
import kostiskag.unitynetwork.bluenode.socket.TCPSocketFunctions;

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
            App.bn.blueNodesTable.lease(node);
        }
    }

    public static int getRemoteRedNodesU(String BlueNodeHostname) {
        String bnaddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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

        TCPSocketFunctions.sendData("BLUENODE " + App.bn.name + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("GET_RED_NODES ", outputWriter, inputReader);
        int count = Integer.parseInt(args[1]);
        for (int i = 0; i < count; i++) {
            args = TCPSocketFunctions.readData(inputReader);
            if (App.bn.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                App.bn.remoteRedNodesTable.lease(args[0], args[1], BlueNodeHostname);
            } else {
                App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
            }
        }
        TCPSocketFunctions.connectionClose(socket);
        return 1;
    }

    public static boolean checkBlueNode(String BlueNodeHostname) {
        String bnaddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return false;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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
        args = TCPSocketFunctions.sendData("BLUENODE " + App.bn.name, outputWriter, inputReader);
        TCPSocketFunctions.sendFinalData("EXIT ", outputWriter);
        TCPSocketFunctions.connectionClose(socket);
        return true;
    }

    public static void removeBlueNodeProjection(int i) {
        String BlueNodeHostname = App.bn.blueNodesTable.getBlueNodeInstance(i).getHostname();
        String BlueNodeAddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(BlueNodeAddress);
        String[] args = null;
        if (IPaddress == null) {
            return;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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
        args = TCPSocketFunctions.sendData("BLUENODE " + App.bn.name, outputWriter, inputReader);
        if (args[0].equals("OK")) {
            TCPSocketFunctions.sendData("RELEASE", outputWriter, inputReader);
        }
        TCPSocketFunctions.connectionClose(socket);
    }

    public static void removeBlueNodeProjection(String BlueNodeHostname) {
        String BlueNodeAddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(BlueNodeAddress);
        String[] args = null;
        if (IPaddress == null) {
            return;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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
        args = TCPSocketFunctions.sendData("BLUENODE " + App.bn.name + " ", outputWriter, inputReader);
        if (args[0].equals("OK")) {
            TCPSocketFunctions.sendFinalData("RELEASE", outputWriter);
        }
        TCPSocketFunctions.connectionClose(socket);
    }

    //-1 host not found for error
    //0 for not found remote rednode
    //1 for found rednote
    public static int checkRemoteRedNode(String vaddress) {
        String BlueNodeHostname = App.bn.remoteRedNodesTable.getRedRemoteAddress(vaddress).getBlueNodeName();
        String bnaddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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

        TCPSocketFunctions.sendData("BLUENODE " + App.bn.name + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("CHECK " + vaddress + " ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);

        if (args[1].equals("ONLINE")) {
            return 1;
        } else {
            App.bn.ConsolePrint(pre + "USER IS NOT CONNECTED TO BN " + BlueNodeHostname);
            return 0;
        }
    }

    public static boolean checkRemoteRedNodeAbsolute(String vaddress, String BlueNodeHostname) {
        String bnaddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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

        TCPSocketFunctions.sendData("BLUENODE " + App.bn.name + " ", outputWriter, inputReader);
        TCPSocketFunctions.sendData("CHECK " + vaddress + " ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);

        if (args[1].equals("ONLINE")) {
            return true;
        } else {
            App.bn.ConsolePrint(pre + "USER IS NOT KNOWN TO BN " + BlueNodeHostname);
            return false;
        }
    }

    public static int UPing(String BlueNodeHostname) {
        String bnaddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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

        TCPSocketFunctions.sendData("BLUENODE " + App.bn.name + " ", outputWriter, inputReader);
        TCPSocketFunctions.sendFinalData("UPING ", outputWriter);
        byte[] payload = ("00002 " + App.bn.name + " [UPING PACKET]").getBytes();
        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
        App.bn.blueNodesTable.getBlueNodeInstanceByHn(RemoteHostname).getQueueMan().offer(data);
        
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
        String bnaddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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
        TCPSocketFunctions.sendData("BLUENODE " + App.bn.name + " ", outputWriter, inputReader);

        App.bn.dping = false;
        TCPSocketFunctions.sendData("DPING ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);
        
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueNodeClientFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (App.bn.dping) {
            return 1;
        } else {
            return 0;
        }
    }

    public static boolean isSameHostname(String targetHostname) {
        if (App.bn.name.equals(targetHostname)) {                            
            App.bn.ConsolePrint(pre + "BLUE NODES HAVE THE SAME NAME, QUITING");
            return true;
        } else {            
            return false;
        }
    }

    public static String getRedHostname(String BlueNodeHostname, String RedNodeAddress) {
        String bnaddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return "";
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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
        args = TCPSocketFunctions.sendData("BLUENODE " + App.bn.name + " ", outputWriter, inputReader);
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
        String bnaddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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

        TCPSocketFunctions.sendData("BLUENODE " + App.bn.name + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("EXCHANGE_RED_NODES ", outputWriter, inputReader);
        int count = Integer.parseInt(args[1]);
        for (int i = 0; i < count; i++) {
            args = TCPSocketFunctions.readData(inputReader);
            if (App.bn.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                App.bn.remoteRedNodesTable.lease(args[0], args[1], BlueNodeHostname);
            } else {
                App.bn.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
            }
        }
        TCPSocketFunctions.readData(inputReader);

        GlobalSocketFunctions.sendLocalRedNodes(outputWriter);
        TCPSocketFunctions.connectionClose(socket);
        return 1;
    }

    public static int feedReturnRoute(String BlueNodeHostname, String vaddress) {
        String bnaddress = App.bn.blueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, App.bn.authPort);
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

        TCPSocketFunctions.sendData("BLUENODE " + App.bn.name + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("FEED_RETURN_ROUTE " + vaddress + " ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);

        if (args[0].equals("OK")) {
            return 1;
        } else {
            App.bn.ConsolePrint(pre + "USER IS NOT CONNECTED TO BN " + BlueNodeHostname);
            return 0;
        }
    }
}
