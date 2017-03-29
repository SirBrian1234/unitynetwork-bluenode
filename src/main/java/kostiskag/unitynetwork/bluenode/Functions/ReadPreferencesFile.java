package kostiskag.unitynetwork.bluenode.Functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Properties;
import kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author kostis
 */
public class ReadPreferencesFile {
    
    public static void ParseFile(InputStream file) throws IOException {
        
        Properties cfg = new java.util.Properties();
        cfg.load(file);
        
        String UseNetwork = cfg.getProperty("network");
        String UnityTracker = cfg.getProperty("UnityTracker");
        String UnityTrackerAuthPort = cfg.getProperty("UnityTrackerAuthPort");        
        String UseList = cfg.getProperty("uselist");
        String Hostname = cfg.getProperty("Hostname");
        String AuthPort = cfg.getProperty("AuthPort");
        String udpstart = cfg.getProperty("udpstart");
        String udpend = cfg.getProperty("udpend");
        String RedNodeLimit = cfg.getProperty("RedNodeLimit");
        String UseGUI = cfg.getProperty("UseGUI");
        String ConsoleTraffic = cfg.getProperty("ConsoleTraffic");
        String AutoLogin = cfg.getProperty("AutoLogin");
        String Log = cfg.getProperty("log");
        
        App.network = Boolean.parseBoolean(UseNetwork);
        App.Taddr = UnityTracker; 
        App.Hostname = Hostname;
        App.Tport = Integer.parseInt(UnityTrackerAuthPort);        
        App.UseList = Boolean.parseBoolean(UseList);
        App.authport = Integer.parseInt(AuthPort);
        App.startport = Integer.parseInt(udpstart);
        App.endport = Integer.parseInt(udpend);
        App.hostnameEntries = Integer.parseInt(RedNodeLimit);
        App.gui = Boolean.parseBoolean(UseGUI);
        App.soutTraffic = Boolean.parseBoolean(ConsoleTraffic);
        App.autologin =  Boolean.parseBoolean(AutoLogin);
        App.log = Boolean.parseBoolean(Log);
        
        System.out.println("");
        System.out.println("UnityTracker is "+App.Taddr);        
        System.out.println("UnityTrackerAuthPort is "+App.Tport);        
        System.out.println("Hostname is "+App.Hostname);
        System.out.println("AuthPort is "+App.authport);
        System.out.println("udpstart is "+App.startport);
        System.out.println("udpend is "+App.endport);
        System.out.println("RedNodeLimit is "+App.hostnameEntries);
        System.out.println("UseGUI is "+App.gui);
        System.out.println("ConsoleTraffic is "+App.soutTraffic);
        System.out.println("AutoLogin is "+App.autologin);        
        System.out.println("Log is "+App.log);   
        System.out.println("");                
    }   

    public static void ParseList(File userlist) throws IOException {
        if (userlist == null || App.accounts == null){
            System.err.println("Rarselist error called with non init accounts or file error");
            App.die();
        }
            
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(userlist));            
                    
            LinkedList<String> list = new LinkedList<String>();
            
            while(br.ready()){            
                list.add(br.readLine());
            }            
            
            int size = list.size();
            String line;
            String[] validline;
            for (int i=0; i<size; i++){
                line = list.poll();
                if (!line.startsWith("#")){
                    validline = line.split("\\s+");
                    App.accounts.insert(validline[0], validline[1], validline[2], validline[3]);
                    System.out.println(validline[0]+ validline[1]+ validline[2]+ validline[3]);
                }
            }            
            System.out.println("userlist loaded");
            
        } catch (FileNotFoundException ex) {
            System.err.println("read userlist error the file by this far should have been valid. this may be a bug");
            App.die();
        }
    }
    
    public static void GenerateFile(File file) throws FileNotFoundException, UnsupportedEncodingException {    
    	    PrintWriter writer = new PrintWriter(file, "UTF-8");
    	    writer.print(""
    	    		+ "###############################\n"
    	    		+ "#   Blue Node Config File     #\n"
    	    		+ "###############################\n"
    	    		+ "\n"
    	    		+ "# please do not comment any variable nor remove any. this will result in error\n"
    	    		+ "# instead only change the value to an appropriate input as described\n"
    	    		+ "\n"
    	    		+ "# use unity network true ~ false (false means a standalone working BN, true means\n"
    	    		+ "# that the BN works on a unity network with a tracker and other BNs)\n"
    	    		+ "network = false\n"
    	    		+ "\n"
    	    		+ "# if you used network, lets define the central tracker\n"
    	    		+ "# with an ip address or with a hostname or domain\n"
    	    		+ "# and the central auth port of the tracker 8000 is default\n"
    	    		+ "UnityTracker = localhost\n"
    	    		+ "UnityTrackerAuthPort = 8000\n"
    	    		+ "\n"
    	    		+ "# choose to autologin to the network\n"
    	    		+ "# by default is disabled because you can click it from the GUI\n"
    	    		+ "AutoLogin = true\n"
    	    		+ "\n"
    	    		+ "# then set the hostname of the BN\n"
    	    		+ "# hostname must be registered with central authority if you use one\n"
    	    		+ "# and the local auth port 7000 default\n"
    	    		+ "Hostname = BlueNode\n"
    	    		+ "AuthPort = 7000\n"
    	    		+ "\n"
    	    		+ "# use list true ~ false (false means any client can log in as he states himself\n"
    	    		+ "# true means only a user in users.list can login) the file users.list\n"
    	    		+ "# holds the list\n"
    	    		+ "uselist = false\n"
    	    		+ "\n"
    	    		+ "# now give a udprange\n"
    	    		+ "# for the RN tunnels where the packets will be forwarded\n"
    	    		+ "udpstart = 20000\n"
    	    		+ "udpend = 22000\n"
    	    		+ "\n"
    	    		+ "# set the limit of RNs for this BN\n"
    	    		+ "RedNodeLimit = 20\n"
    	    		+ "\n"
    	    		+ "# set GUI or command line\n"
    	    		+ "# with true or false\n"
    	    		+ "UseGUI = true\n"
    	    		+ "\n"
    	    		+ "# choose to verbose traffic in command line\n"
    	    		+ "# by default is disabled because you can monitor it\n"
    	    		+ "# in GUI but it useful if you are under remote terminal\n"
    	    		+ "ConsoleTraffic = false\n"
    	    		+ "\n"
    	    		+ "# logging in bluenode.log\n"
    	    		+ "# true ~ false\n"
    	    		+ "log = true\n"
    	    		+ "");    	    
    	    writer.close();
    }
}