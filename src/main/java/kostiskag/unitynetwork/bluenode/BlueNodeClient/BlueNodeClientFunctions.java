/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.BlueNodeClient;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode.*;
import kostiskag.unitynetwork.bluenode.Routing.*;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import kostiskag.unitynetwork.bluenode.Functions.TCPSocketFunctions;
import kostiskag.unitynetwork.bluenode.RunData.Instances.BlueNodeInstance;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            lvl3BlueNode.BlueNodesTable.lease(node);
        }
    }

    public static int getRemoteRedNodesU(String BlueNodeHostname) {
        String bnaddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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

        TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("GET_RED_NODES ", outputWriter, inputReader);
        int count = Integer.parseInt(args[1]);
        for (int i = 0; i < count; i++) {
            args = TCPSocketFunctions.readData(inputReader);
            if (lvl3BlueNode.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                lvl3BlueNode.remoteRedNodesTable.lease(args[0], args[1], BlueNodeHostname);
            } else {
                lvl3BlueNode.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
            }
        }
        TCPSocketFunctions.connectionClose(socket);
        return 1;
    }

    public static boolean checkBlueNode(String BlueNodeHostname) {
        String bnaddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return false;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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
        args = TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname, outputWriter, inputReader);
        TCPSocketFunctions.sendFinalData("EXIT ", outputWriter);
        TCPSocketFunctions.connectionClose(socket);
        return true;
    }

    public static void removeBlueNodeProjection(int i) {
        String BlueNodeHostname = lvl3BlueNode.BlueNodesTable.getBlueNodeInstance(i).getHostname();
        String BlueNodeAddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(BlueNodeAddress);
        String[] args = null;
        if (IPaddress == null) {
            return;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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
        args = TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname, outputWriter, inputReader);
        if (args[0].equals("OK")) {
            TCPSocketFunctions.sendData("RELEASE", outputWriter, inputReader);
        }
        TCPSocketFunctions.connectionClose(socket);
    }

    public static void removeBlueNodeProjection(String BlueNodeHostname) {
        String BlueNodeAddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(BlueNodeAddress);
        String[] args = null;
        if (IPaddress == null) {
            return;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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
        args = TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname + " ", outputWriter, inputReader);
        if (args[0].equals("OK")) {
            TCPSocketFunctions.sendFinalData("RELEASE", outputWriter);
        }
        TCPSocketFunctions.connectionClose(socket);
    }

    //-1 host not found for error
    //0 for not found remote rednode
    //1 for found rednote
    public static int checkRemoteRedNode(String vaddress) {
        String BlueNodeHostname = lvl3BlueNode.remoteRedNodesTable.getRedRemoteAddress(vaddress).getBlueNodeHostname();
        String bnaddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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

        TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("CHECK " + vaddress + " ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);

        if (args[1].equals("ONLINE")) {
            return 1;
        } else {
            lvl3BlueNode.ConsolePrint(pre + "USER IS NOT CONNECTED TO BN " + BlueNodeHostname);
            return 0;
        }
    }

    public static boolean checkRemoteRedNodeAbsolute(String vaddress, String BlueNodeHostname) {
        String bnaddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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

        TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname + " ", outputWriter, inputReader);
        TCPSocketFunctions.sendData("CHECK " + vaddress + " ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);

        if (args[1].equals("ONLINE")) {
            return true;
        } else {
            lvl3BlueNode.ConsolePrint(pre + "USER IS NOT KNOWN TO BN " + BlueNodeHostname);
            return false;
        }
    }

    public static int UPing(String BlueNodeHostname) {
        String bnaddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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

        TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname + " ", outputWriter, inputReader);
        TCPSocketFunctions.sendFinalData("UPING ", outputWriter);
        byte[] payload = ("00002 " + lvl3BlueNode.Hostname + " [UPING PACKET]").getBytes();
        byte[] data = IpPacket.MakeUPacket(payload, null, null, true);
        lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(RemoteHostname).getQueueMan().offer(data);
        
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
        String bnaddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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
        TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname + " ", outputWriter, inputReader);

        lvl3BlueNode.dping = false;
        TCPSocketFunctions.sendData("DPING ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);
        
        try {
            sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlueNodeClientFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (lvl3BlueNode.dping) {
            return 1;
        } else {
            return 0;
        }
    }

    public static boolean isSameHostname(String targetHostname) {
        if (lvl3BlueNode.Hostname.equals(targetHostname)) {                            
            lvl3BlueNode.ConsolePrint(pre + "BLUE NODES HAVE THE SAME NAME, QUITING");
            return true;
        } else {            
            return false;
        }
    }

    public static String getRedHostname(String BlueNodeHostname, String RedNodeAddress) {
        String bnaddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return "";
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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
        args = TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname + " ", outputWriter, inputReader);
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
        String bnaddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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

        TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("EXCHANGE_RED_NODES ", outputWriter, inputReader);
        int count = Integer.parseInt(args[1]);
        for (int i = 0; i < count; i++) {
            args = TCPSocketFunctions.readData(inputReader);
            if (lvl3BlueNode.remoteRedNodesTable.getRedRemoteAddress(args[0]) == null) {
                lvl3BlueNode.remoteRedNodesTable.lease(args[0], args[1], BlueNodeHostname);
            } else {
                lvl3BlueNode.ConsolePrint(pre + "ALLREADY REGISTERED REMOTE RED NODE");
            }
        }
        TCPSocketFunctions.readData(inputReader);

        int size = lvl3BlueNode.localRedNodesTable.getSize();
        TCPSocketFunctions.sendFinalData("SENDING_LOCAL_RED_NODES " + size, outputWriter);
        for (int i = 0; i < size; i++) {
            String vaddress = lvl3BlueNode.localRedNodesTable.getRedNodeInstance(i).getVaddress();
            String hostname = lvl3BlueNode.localRedNodesTable.getRedNodeInstance(i).getHostname();
            TCPSocketFunctions.sendFinalData(vaddress + " " + hostname, outputWriter);
        }
        TCPSocketFunctions.sendFinalData("", outputWriter);
        TCPSocketFunctions.connectionClose(socket);
        return 1;
    }

    public static int feedReturnRoute(String BlueNodeHostname, String vaddress) {
        String bnaddress = lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(BlueNodeHostname).getPhaddress();
        InetAddress IPaddress = TCPSocketFunctions.getAddress(bnaddress);
        if (IPaddress == null) {
            return -1;
        }
        Socket socket = TCPSocketFunctions.absoluteConnect(IPaddress, lvl3BlueNode.authport);
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

        TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname + " ", outputWriter, inputReader);
        args = TCPSocketFunctions.sendData("FEED_RETURN_ROUTE " + vaddress + " ", outputWriter, inputReader);
        TCPSocketFunctions.connectionClose(socket);

        if (args[0].equals("OK")) {
            return 1;
        } else {
            lvl3BlueNode.ConsolePrint(pre + "USER IS NOT CONNECTED TO BN " + BlueNodeHostname);
            return 0;
        }
    }
}
