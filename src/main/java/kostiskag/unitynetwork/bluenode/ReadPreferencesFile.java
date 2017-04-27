package kostiskag.unitynetwork.bluenode;

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

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class ReadPreferencesFile {
    
    public static void ParseConfigFile(InputStream file) throws IOException {
        
        Properties cfg = new java.util.Properties();
        cfg.load(file);
        
        String UseNetwork = cfg.getProperty("Network").replaceAll("\\s+","");
        String UnityTracker = cfg.getProperty("UnityTrackerAddress").replaceAll("\\s+","");
        String UnityTrackerAuthPort = cfg.getProperty("UnityTrackerAuthPort").replaceAll("\\s+",""); 
        String TrackerMaxIdleTimeMin = cfg.getProperty("TrackerMaxIdleTimeMin").replaceAll("\\s+",""); 
        String Name = cfg.getProperty("Name").replaceAll("\\s+","");
        String AuthPort = cfg.getProperty("AuthPort").replaceAll("\\s+","");
        String UseList = cfg.getProperty("UseHostList").replaceAll("\\s+","");
        String udpstart = cfg.getProperty("udpstart").replaceAll("\\s+","");
        String udpend = cfg.getProperty("udpend").replaceAll("\\s+","");
        String RedNodeLimit = cfg.getProperty("RedNodeLimit").replaceAll("\\s+","");
        String UseGUI = cfg.getProperty("UseGUI").replaceAll("\\s+","");
        String ConsoleTraffic = cfg.getProperty("ConsoleTraffic").replaceAll("\\s+","");        
        String Log = cfg.getProperty("Log").replaceAll("\\s+","");
        
        App.network = Boolean.parseBoolean(UseNetwork);
        App.trackerAddress = UnityTracker; 
        App.trackerPort = Integer.parseInt(UnityTrackerAuthPort);  
        App.trackerMaxIdleTimeMin = Integer.parseInt(TrackerMaxIdleTimeMin);  
        App.name = Name;
        App.useList = Boolean.parseBoolean(UseList);
        App.authPort = Integer.parseInt(AuthPort);
        App.startPort = Integer.parseInt(udpstart);
        App.endPort = Integer.parseInt(udpend);
        App.maxRednodeEntries = Integer.parseInt(RedNodeLimit);
        App.gui = Boolean.parseBoolean(UseGUI);
        App.soutTraffic = Boolean.parseBoolean(ConsoleTraffic);        
        App.log = Boolean.parseBoolean(Log);
        
        System.out.println("");
        System.out.println("Network is "+App.network);
        System.out.println("UnityTracker is "+App.trackerAddress);        
        System.out.println("UnityTrackerAuthPort is "+App.trackerPort);        
        System.out.println("Hostname is "+App.name);
        System.out.println("AuthPort is "+App.authPort);
        System.out.println("UseHostList is "+App.useList);
        System.out.println("udpstart is "+App.startPort);
        System.out.println("udpend is "+App.endPort);
        System.out.println("RedNodeLimit is "+App.maxRednodeEntries);
        System.out.println("UseGUI is "+App.gui);
        System.out.println("ConsoleTraffic is "+App.soutTraffic);           
        System.out.println("Log is "+App.log);   
        System.out.println("");                
    }   

    public static void ParseHostClientList(File hostListFile) throws Exception {
        
        BufferedReader br = new BufferedReader(new FileReader(hostListFile));                            
        LinkedList<String> list = new LinkedList<String>();        
        while(br.ready()){
        	String line = br.readLine();
        	if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("\n") && !line.startsWith(" ")) {        		
        		list.add(line);
        	}
        }
        br.close();
        
        int size = list.size();        
        String line;
        String[] validline;
        for (int i=0; i<size; i++){
            line = list.poll();
            validline = line.split("\\s+");
            int address = Integer.parseInt(validline[3]);
            App.accounts.insert(validline[0], validline[1], validline[2], address);				            	            
        }                  
    }
    
    public static void GenerateConfigFile(File file) throws FileNotFoundException, UnsupportedEncodingException {    
    	    PrintWriter writer = new PrintWriter(file, "UTF-8");
    	    writer.print(""
    	    		+ "#####################################\n"
    	    		+ "#   BlueNode Configuration File     #\n"
    	    		+ "#####################################\n"
    	    		+ "\n"
    	    		+ "#\n"
    	    		+ "# Insructions for setting up the config file\n"
    	    		+ "#\n"
    	    		+ "# Do not comment any variable nor remove any from this file as this will result\n"
    	    		+ "# in an application error. Change the value to an appropriate input as described\n"
    	    		+ "# instead. If this file gets messed up, you may delete it and it will be\n"
    	    		+ "# auto-generated from the app.\n"
    	    		+ "#\n"
    	    		+ "\n"
    	    		+ "#\n"
    	    		+ "# Network Type\n"
    	    		+ "#\n"
    	    		+ "# Network = false - for Local Network. The BlueNode may not connect to a tracker and will\n"
    	    		+ "# serve only local connected RedNodes\n"
    	    		+ "# Network = true - for Full Network. The BlueNode will seek a tracker to be a part in\n"
    	    		+ "# a full network with other BlueNodess and remote RedNodess\n"
    	    		+ "#\n"
    	    		+ "Network = false\n"
    	    		+ "\n"
    	    		+ "#\n"
    	    		+ "# variables for FullNetwork\n"
    	    		+ "#\n"
    	    		+ "# if you have selected Local Network these variables will not take any effect\n"
    	    		+ "#\n"
    	    		+ "\n"
    	    		+ "# Provide the central tracker's address\n"
    	    		+ "# with an IP address or with a domain.\n"
    	    		+ "# Provide the tracker's TCP auth port. 8000 is the default.\n"
    	    		+ "UnityTrackerAddress = 192.168.1.1\n"
    	    		+ "UnityTrackerAuthPort = 8000\n"
    	    		+ "\n"
    	    		+ "# This is the network's reverse lookup time in minutes, it has to be double from\n"
    	    		+ "# The tracker's ping time.\n"
    	    		+ "TrackerMaxIdleTimeMin = 2\n"
    	    		+ "\n"
    	    		+ "# Set the Name of this BlueNode\n"
    	    		+ "# In Full Network the BN's name must be registered in the tracker's database\n"
    	    		+ "# Set the TCP auth port. 7000 is the default.\n"
    	    		+ "Name = BlueNode\n"
    	    		+ "AuthPort = 7000\n"
    	    		+ "\n"
    	    		+ "#\n"
    	    		+ "# variables for LocalNetwork\n"
    	    		+ "#\n"
    	    		+ "# if you have selected Full Network these will not take effect\n"
    	    		+ "#\n"
    	    		+ "\n"
    	    		+ "# use list true - false (false means that any client can log in as he states himself\n"
    	    		+ "# true means only a defined user in the file users.list can login\n"
    	    		+ "# holds the list\n"
    	    		+ "UseHostList = false\n"
    	    		+ "\n"
    	    		+ "#\n"
    	    		+ "# Load and Capacity\n"
    	    		+ "#\n"
    	    		+ "# This is the BlueNode's UDP port range\n"
    	    		+ "udpstart = 20000\n"
    	    		+ "udpend = 22000\n"
    	    		+ "\n"
    	    		+ "# Set the upper limit of RNs for this BlueNode\n"
    	    		+ "RedNodeLimit = 20\n"
    	    		+ "\n"
    	    		+ "#\n"
    	    		+ "# Application behaviour\n"
    	    		+ "#\n"
    	    		+ "\n"
    	    		+ "# set GUI or command line\n"
    	    		+ "# with true or false\n"
    	    		+ "UseGUI = true\n"
    	    		+ "\n"
    	    		+ "# Select whether to verbose traffic in command line.\n"
    	    		+ "# By default is disabled as it fills up the terminal\n"
    	    		+ "# and you can allways monitor it in the GUI.\n"
    	    		+ "# It useful if you are under a remote terminal.\n"
    	    		+ "ConsoleTraffic = false\n"
    	    		+ "\n"
    	    		+ "# Logging in bluenode.log\n"
    	    		+ "# use true or false\n"
    	    		+ "Log = true\n"
    	    		+ "");    	    
    	    writer.close();
    }
    
    public static void GenerateHostClientFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
    	PrintWriter writer = new PrintWriter(file, "UTF-8");
	    writer.print("" 
	    		+ "###############################\n"
	    		+ "#   Host-Client List File     #\n"
	    		+ "###############################\n"
	    		+ "\n"
	    		+ "#\n"
	    		+ "# This file will take effect only when in bluenode.conf the following were\n"
	    		+ "# defined:\n"
	    		+ "#\n"
	    		+ "# when a BlueNode runs a Local Network\n"
	    		+ "# Network = false\n"
	    		+ "#\n"
	    		+ "# when a BlueNode has requested to use a user.list\n" 
	    		+ "# UseClientList = true\n"
	    		+ "#\n" 
	    		+ "\n"
	    		+ "#\n"
	    		+ "# In this file you may define a list of allowed host-clients\n" 
	    		+ "# to be authenticated by the Blue Node\n"
	    		+ "#\n"
	    		+ "# Use like:\n"
	    		+ "# Username Password Hostname Virtual_Address_Number\n"
	    		+ "#\n"
	    		+ "# for the Virtual_Address_Number field use an integer starting from number 1, "
	    		+ "# the BlueNode will auto convert the number to its respective IP address\n"
	    		+ "# you may repeat the same Username and Password with a different Hostname\n" 
	    		+ "# and Virtual_Address in case the same user owns more than one devices\n"
	    		+ "#\n"
	    		+ "# ex.\n"
	    		+ "# bob 12345 bob-laptop 2\n"
	    		+ "# bob 12345 bob-mobile 3\n"
	    		+ "#\n"	    		
	    		+ "");    	    
	    writer.close();		
    }
}