package kostiskag.unitynetwork.bluenode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.swing.UIManager;

import kostiskag.unitynetwork.bluenode.RunData.tables.AccountsTable;
import kostiskag.unitynetwork.bluenode.functions.GetTime;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

/**
 * This class keeps the application's main method. 
 * It runs basic tests in order to determine if the application can run as intended 
 * and initialize a BlueNode object with predefined settings.
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
	public static final int min_str_addr_len = "0.0.0.0".length();
	// network
	public static final int virtualNetworkAddressCapacity = (int) (Math.pow(2,24) - 2); //this is our 10.0.0.0/8 network
	public static final int systemReservedAddressNumber = 1; //The number of reserved IP addresses from the system 	
	// file names
	public static final String configFileName = "bluenode.conf";
	public static final String hostlistFileName = "host.list";
	public static final String logFileName = "bluenode.log";
	public static final String keyPairFileName = "public_private.keypair";
	public static final String trackerPublicKeyFileName = "tracker_public.key";
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
	public static KeyPair bluenodeKeys;
	public static PublicKey trackerPublicKey;
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
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * The app's main method here.
	 * we do not use args anymore... we use bluenode.conf. You may set the initial settings in the file.
	 * inside this you can't use consoleprint as it is not ready yet, use system.out instead
	 * grave mistakes are being punished with System.exit(1) as die() in not ready yet
	 * 
	 * @param argv
	 */
	public static void main(String argv[]) {
		System.out.println(pre+"@Started at "+Thread.currentThread().getName());
		
		if (argv.length > 1) {
			System.out.println(pre+"The application does not support any arguments. In order to provide settings edit the file:"+configFileName+".");
			System.exit(1);
		}
		
		//loading .conf file if exists or generating a new file and load the default settings if non existing.
		System.out.println(pre+"Checking configuration file " + configFileName + "...");
		File file = new File(configFileName);
		InputStream filein = null;
		if (file.exists()) {
			try {
				filein = new FileInputStream(file);
				ReadPreferencesFile.ParseConfigFile(filein);
				filein.close();
			} catch (Exception e) {
				System.out.println(pre+"File "+configFileName+" could not be loaded.");
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			System.out.println(pre+"File "+configFileName+" not found in the dir. Generating new file with the default settings.");			
     		try {
     			ReadPreferencesFile.GenerateConfigFile(file);
				filein = new FileInputStream(file);
				ReadPreferencesFile.ParseConfigFile(filein);
				filein.close();
			} catch (Exception e) {
				System.out.println(pre+"File "+configFileName+" could not be loaded.");
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		// 3. rsa key pair
		File keyPairFile = new File(keyPairFileName);
		if (keyPairFile.exists()) {
			// the tracker has key pair
			System.out.println(pre+"Loading RSA key pair from file...");
			try {
				bluenodeKeys = (KeyPair) CryptoUtilities.fileToObject(keyPairFile);
				System.out.println(pre +
						"Your public key is:\n" + CryptoUtilities.bytesToBase64String(bluenodeKeys.getPublic().getEncoded()));
			} catch (GeneralSecurityException | IOException e) {
				System.out.println(pre+"could not load the RSA keypair from file");
				System.out.println(e.getMessage());
				System.exit(1);
			}

		} else {
			// the tracker does not have a public private key pair
			// generating...
			try {
				System.out.println(pre + "Generating RSA key pair...");
				bluenodeKeys = CryptoUtilities.generateRSAkeyPair();
				// and storing
				System.out.println(pre + "Generating key file...");
				CryptoUtilities.objectToFile(bluenodeKeys, keyPairFile);
				System.out.println(pre +
						"Your public key is:\n" + CryptoUtilities.bytesToBase64String(bluenodeKeys.getPublic().getEncoded()));
			} catch (GeneralSecurityException | IOException e) {
				System.out.println(pre+"could not generate an RSA keypair");
				System.out.println(e.getMessage());
				System.exit(1);
			}
		}
		
		// tracker's public
		File trackerPublic = new File(trackerPublicKeyFileName);
		if (trackerPublic.exists()) {
			try {
				trackerPublicKey = (PublicKey) CryptoUtilities.fileToObject(trackerPublic);
			} catch (GeneralSecurityException | IOException e) {
				System.out.println(pre+"could not read public key");
				System.out.println(e.getMessage());
				System.exit(1);
			}
		} 

		// generating hostlist file if not existing
	    System.out.println(pre+"Checking file " + hostlistFileName + "...");
		file = new File(hostlistFileName);
		filein = null;
		if (!file.exists()) {
			//generating
			System.out.println(pre+hostlistFileName+" file not found in the dir. Generating new file with the default settings");			
     		try {
     			ReadPreferencesFile.GenerateHostClientFile(file);				
			} catch (Exception e) {
				System.out.println(pre+"File "+hostlistFileName+" could not be generated.");
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			//loading accounts
			System.out.println(pre+"File "+hostlistFileName+" exists in the dir.");			
			if (!network && useList) {
				//reading file
				System.out.println(pre+"Reading accounts from file.");
				accounts = new AccountsTable();
				try {
					ReadPreferencesFile.ParseHostClientList(new File(hostlistFileName));
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(hostlistFileName+" was wrongly formated. Please edit or delete the file.");
					System.exit(1);
				}
				System.out.println(pre+"hostlist loaded with "+accounts.getSize()+" host-clients.");       
			}
		}

		// test and init gui if enabled
		if (gui) {
			System.out.println(pre+"checking gui libraries...");
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch (Exception e) {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				} catch (Exception ex) {
					try {
						UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
					} catch (Exception ex1) {
						System.out.println(pre+"although asked for gui there are no supported libs on this machine");
						System.out.println(pre+"fix it or disable gui from the bluenode.conf");
						System.exit(1);
					}
				}
			}
		}
		
		// init bluenode.log
		System.out.println(pre+"initializing log file...");
		if (log) {
			FileWriter fw;
			try {
				fw = new FileWriter(App.logFile, false);
				fw.write("-------------"+GetTime.getFullTimestamp()+"-------------\n");
				fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		// init a bluenode object
		System.out.println(pre+"starting a BlueNode instance...");
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
				accounts,
				bluenodeKeys,
				trackerPublicKey);
		bn.run();
	}
}
