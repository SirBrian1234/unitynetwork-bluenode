/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.TrackClient;

import kostiskag.unitynetwork.bluenode.Functions.TCPSocketFunctions;
import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author kostis
 */
public class TrackingBlueNodeFunctions {

    private static String pre = "^BN Functions ";
    
    public static int lease(String BlueNodeHostname, int authport) {
        InetAddress addr = TCPSocketFunctions.getAddress(lvl3BlueNode.Taddr);
        int port = lvl3BlueNode.Tport;
        Socket socket = TCPSocketFunctions.absoluteConnect(addr, port);
        if (socket == null) {
            return -2;
        }
        BufferedReader reader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter writer = TCPSocketFunctions.makeWriteWriter(socket);
        String args[] = TCPSocketFunctions.readData(reader);
        args = TCPSocketFunctions.sendData("BLUENODE " + BlueNodeHostname, writer, reader);

        if (args[0].equals("OK")) {
            args = TCPSocketFunctions.sendData("LEASE BN " + authport, writer, reader);


            if (args[0].equals("LEASED")) {
                lvl3BlueNode.echoAddress = args[1];
                lvl3BlueNode.ConsolePrint(pre + "ECHO ADDRESS IS " + lvl3BlueNode.echoAddress);
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public static boolean release() {
        InetAddress addr = TCPSocketFunctions.getAddress(lvl3BlueNode.Taddr);
        int port = lvl3BlueNode.Tport;
        Socket socket = TCPSocketFunctions.absoluteConnect(addr, port);
        if (socket == null) {
            return false;
        }
        BufferedReader reader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter writer = TCPSocketFunctions.makeWriteWriter(socket);
        String args[] = TCPSocketFunctions.readData(reader);
        args = TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname, writer, reader);
        if (args[0].equals("OK")) {
            args = TCPSocketFunctions.sendData("RELEASE BN", writer, reader);
            if (args[0].equals("RELEASED")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static String getPhysical(String BNHostname) {
        InetAddress addr = TCPSocketFunctions.getAddress(lvl3BlueNode.Taddr);
        int port = lvl3BlueNode.Tport;
        Socket socket = TCPSocketFunctions.absoluteConnect(addr, port);
        if (socket == null) {
            return null;
        }
        BufferedReader reader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter writer = TCPSocketFunctions.makeWriteWriter(socket);
        String args[] = TCPSocketFunctions.readData(reader);
        args = TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname, writer, reader);
        if (args[0].equals("OK")) {
            args = TCPSocketFunctions.sendData("GETPH " + BNHostname, writer, reader);
            if (!args[0].equals("NOT_FOUND")) {
                return args[0];
            } else {
                return null;
            }
        }
        return null;
    }

    static boolean update() {
        InetAddress addr = TCPSocketFunctions.getAddress(lvl3BlueNode.Taddr);
        int port = lvl3BlueNode.Tport;
        Socket socket = TCPSocketFunctions.absoluteConnect(addr, port);
        if (socket == null) {
            return false;
        }
        BufferedReader reader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter writer = TCPSocketFunctions.makeWriteWriter(socket);
        String args[] = TCPSocketFunctions.readData(reader);
        args = TCPSocketFunctions.sendData("BLUENODE " + lvl3BlueNode.Hostname, writer, reader);
        if (args[0].equals("OK")) {
            args = TCPSocketFunctions.sendData("UPDATE", writer, reader);
            if (args[0].equals("OK")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
