package kostiskag.unitynetwork.bluenode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

import kostiskag.unitynetwork.bluenode.RunData.tables.AccountsTable;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class App extends Thread {

	private static final String pre = "^App ";
	//sizes
	public static final int max_int_str_len = 32;
	public static final int max_str_len_small_size = 128;
	public static final int max_str_len_large_size = 256;
	public static final int max_str_addr_len = "255.255.255.255".length();
	// network
	public static final int virtualNetworkAddressCapacity = (int) (Math.pow(2,24) - 2);
	public static final int systemReservedAddressNumber = 2; 	
	// file names
	public static final String configFileName = "bluenode.conf";
	public static final String hostlistFileName = "host.list";
	public static final String logFileName = "bluenode.log";
	// files
	public static final File logFile =  new File(logFileName);
	// user
	public static final String SALT = "=UrBN&RLJ=dBshBX3HFn!S^Au?yjqV8MBx7fMyg5p6U8T^%2kp^X-sk9EQeENgVEj%DP$jNnz&JeF?rU-*meW5yFkmAvYW_=mA+E$F$xwKmw=uSxTdznSTbunBKT*-&!";
	// bluenode settings
	public static boolean network;
	public static String trackerAddress; 
	public static int trackerPort;    
	public static int trackerMaxIdleTimeMin;
	public static String name;
	public static boolean useList;
	public static int authPort;
	public static int startPort;
	public static int endPort;
	public static int maxRednodeEntries;
	public static boolean gui;
	public static boolean soutTraffic;        
	public static boolean log;
	public static AccountsTable accounts;	
	// bluenode
	public static BlueNode bn;
	
	public static synchronized void writeToLogFile(String message) {
		if (log) {
			FileWriter fw;
			try {
				fw = new FileWriter(logFile, true);
				fw.append(message + "\n");
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	private static void loadUserList() {
		accounts = new AccountsTable();
		try {
			ReadPreferencesFile.ParseHostClientList(new File(hostlistFileName));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(hostlistFileName+" was wrongly formated. Please update the file.");
			System.exit(1);
		}
		System.out.println(accounts.toString());
	
	}
	
	/**
	 * The app's main method here.
	 * we do not use args anymore... we use bluenode.conf
	 * inside this you can't use consoleprint as it is not ready yet, use system.out instead
	 * grave mistakes are being punished with System.exit(1) as die() in not ready yet
	 * 
	 * @param argv
	 */
	public static void main(String argv[]) {
		System.out.println("@Started main at " + Thread.currentThread().getName());
		
		InputStream filein = null;
		System.out.println("Checking configuration file " + configFileName + "...");
		File file = new File(configFileName);
		if (file.exists()) {
			try {
				filein = new FileInputStream(file);
				ReadPreferencesFile.ParseConfigFile(filein);
				filein.close();
			} catch (Exception e) {
				System.err.println("File "+configFileName+" could not be loaded");
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			System.out.println(configFileName+" file not found in the dir. Generating new file with the default settings");			
     		try {
     			ReadPreferencesFile.GenerateConfigFile(file);
				filein = new FileInputStream(file);
				ReadPreferencesFile.ParseConfigFile(filein);
				filein.close();
			} catch (Exception e) {
				System.err.println("File "+configFileName+" could not be loaded");
				e.printStackTrace();
				System.exit(1);
			}
		}

	    filein = null;
		System.out.println("Checking file " + hostlistFileName + "...");
		file = new File(hostlistFileName);
		if (!file.exists()) {
			System.out.println(hostlistFileName+" file not found in the dir. Generating new file with the default settings");			
     		try {
     			ReadPreferencesFile.GenerateHostClientFile(file);				
			} catch (Exception e) {
				System.err.println("File "+hostlistFileName+" could not be generated");
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			System.out.println(hostlistFileName+" exists in the dir");
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
		
		// 0. init bluenode.log
		if (log) {
			FileWriter fw;
			try {
				fw = new FileWriter(App.logFile, false);
				fw.write("---------------------------------------------------------------\n");
				fw.close();
			} catch (IOException ex) {
				Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		System.out.println("starting BlueNode...");
		bn = new BlueNode(
				network, 
				trackerAddress, 
				trackerPort, 
				trackerMaxIdleTimeMin,
				name, 
				useList, 
				authPort, 
				startPort, 
				endPort, 
				maxRednodeEntries, 
				gui, 
				soutTraffic, 
				log, 
				accounts);
		bn.run();
	}
}
