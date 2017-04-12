package kostiskag.unitynetwork.bluenode.trackClient;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.functions.TCPSocketFunctions;

/**
 *
 * @author kostis
 */
public class TrackingRedNodeFunctions {

    public static String lease(String Hostname, String Username, String Password) {
        InetAddress addr = TCPSocketFunctions.getAddress(App.trackerAddress);
        int port = App.trackerPort;
        Socket socket = TCPSocketFunctions.absoluteConnect(addr, port);
        BufferedReader reader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter writer = TCPSocketFunctions.makeWriteWriter(socket);
        String args[] = TCPSocketFunctions.readData(reader);                
        args = TCPSocketFunctions.sendData("BLUENODE " + App.name, writer, reader);
        if (args[0].equals("OK")) {

            args = TCPSocketFunctions.sendData("LEASE RN " +Hostname+" "+Username+" "+Password, writer, reader);
            TCPSocketFunctions.connectionClose(socket);

            if (args[0].equals("LEASED")) {
                return args[1];
            } else {
                return args[0];
            }
        } else {
            return args[0];
        }
    }    

    public static void release(String Hostname) {
        InetAddress addr = TCPSocketFunctions.getAddress(App.trackerAddress);
        int port = App.trackerPort;
        Socket socket = TCPSocketFunctions.absoluteConnect(addr, port);
        BufferedReader reader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter writer = TCPSocketFunctions.makeWriteWriter(socket);
        String args[] = TCPSocketFunctions.readData(reader);
        args = TCPSocketFunctions.sendData("BLUENODE " + App.name, writer, reader);
        if (args[0].equals("OK")) {
            TCPSocketFunctions.sendFinalData("RELEASE RN "+Hostname, writer);
        }
    }
    
    public static String checkOnlineByHn(String hostanme) {
        InetAddress addr = TCPSocketFunctions.getAddress(App.trackerAddress);
        int port = App.trackerPort;
        Socket socket = TCPSocketFunctions.absoluteConnect(addr, port);
        BufferedReader reader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter writer = TCPSocketFunctions.makeWriteWriter(socket);
        String args[] = TCPSocketFunctions.readData(reader);
        args = TCPSocketFunctions.sendData("BLUENODE " + App.name, writer, reader);
        if (args[0].equals("OK")) {
            args = TCPSocketFunctions.sendData("CHECKRN " + hostanme, writer, reader);
            if (args[0].equals("OFFLINE")) {
                return null;
            } else {
                return args[1];
            }
        } else {
            return null;
        }
    }
    
    public static String checkOnlineByAddr(String vaddress) {
        InetAddress addr = TCPSocketFunctions.getAddress(App.trackerAddress);
        int port = App.trackerPort;
        Socket socket = TCPSocketFunctions.absoluteConnect(addr, port);
        BufferedReader reader = TCPSocketFunctions.makeReadWriter(socket);
        PrintWriter writer = TCPSocketFunctions.makeWriteWriter(socket);
        String args[] = TCPSocketFunctions.readData(reader);
        args = TCPSocketFunctions.sendData("BLUENODE " + App.name, writer, reader);
        if (args[0].equals("OK")) {            
            args = TCPSocketFunctions.sendData("CHECKRNA " + vaddress, writer, reader);           
            if (args[0].equals("OFFLINE")) {
                return null;
            } else {
                return args[1];
            }
        } else {
            return null;
        }
    }
    
}
