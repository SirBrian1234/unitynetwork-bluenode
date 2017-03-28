package kostiskag.unitynetwork.bluenode.Functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author kostis
 */
public class TCPSocketFunctions {
    //socket stuff
    public static String pre = "^SOCKET ";       

    public static InetAddress getAddress(String PhAddress) {
        InetAddress IPaddress = null;
        try {
            IPaddress = InetAddress.getByName(PhAddress);
        } catch (UnknownHostException ex) {
            App.ConsolePrint(pre + "WRONG ADDRESS GIVEN");            
            return null;
        }
        return IPaddress;
    }
    
    public static Socket absoluteConnect(InetAddress IPaddress, int authPort) {
        Socket socket = null;
        try {
            socket = new Socket(IPaddress, authPort);
            socket.setSoTimeout(3000);
        } catch (java.net.NoRouteToHostException ex) {
            App.ConsolePrint(pre + "NO ROUTE");
            return null;
        } catch (java.net.ConnectException ex) {
            App.ConsolePrint(pre + "CONNECTION REFUSED");
            return null;
        } catch (java.net.SocketTimeoutException ex) {
            App.ConsolePrint(pre + "CONNECTION TIMED OUT");
            return null;
        } catch (IOException ex) {
            App.ConsolePrint(pre + "CONNECTION ERROR");
            ex.printStackTrace();
            return null;
        }
        return socket;
    }
    
    public static BufferedReader makeReadWriter(Socket socket) {
        BufferedReader inputReader=null;
        try {
            inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }        
        return inputReader;
    }

    public static PrintWriter makeWriteWriter(Socket socket) {
        PrintWriter outputWriter=null;
        try {
            outputWriter = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(TCPSocketFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return outputWriter;
    }

    public static String[] sendData(String data,PrintWriter outputWriter,BufferedReader inputReader) {
        outputWriter.println(data);
        String receivedMessage = null;
        String[] args = null;
        try {
            receivedMessage = inputReader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(TCPSocketFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        args = receivedMessage.split("\\s+");
        return args;
    }

    public static void sendFinalData(String data,PrintWriter outputWriter) {
        outputWriter.println(data);
    }

    public static String[] readData(BufferedReader inputReader) {
        String receivedMessage = null;
        String[] args = null;
        try {
            receivedMessage = inputReader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(TCPSocketFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }                
        args = receivedMessage.split("\\s+");
        return args;
    }

    public static void connectionClose(Socket socket) {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPSocketFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
}
