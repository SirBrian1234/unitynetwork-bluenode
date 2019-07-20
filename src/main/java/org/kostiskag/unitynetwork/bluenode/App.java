package org.kostiskag.unitynetwork.bluenode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Date;

import javax.swing.UIManager;

import org.kostiskag.unitynetwork.common.utilities.FixedDate;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

import org.kostiskag.unitynetwork.bluenode.rundata.table.AccountTable;


/**
 * This class keeps the application's main method. 
 * It runs basic tests in order to determine if the application can run as intended 
 * and initialize a BlueNode object with predefined settings.
 * 
 * @author Konstantinos Kagiampakis
 */
final class App extends Thread {

	private static final String pre = "^App ";

	// file names
	private enum FileNames {
		CONFIG_FILE("bluenode.conf"),
		HOST_LIST_FILE("host.list"),
		LOG_FILE("bluenode.log"),
		KEY_PAIR_FILE("public_private.keypair"),
		TRACKER_KEY_FILE("tracker_public.key");

		private File f;

		FileNames(String name) {
			this.f = new File(name);
		}

		public File getFile() {
			return f;
		}
	}

	public static synchronized void writeToLogFile(String message) {
		try (FileWriter fw = new FileWriter(FileNames.LOG_FILE.getFile(), true)) {
			fw.append(message + "\n");
		} catch (IOException ex) {
			System.out.println(ex.getLocalizedMessage());
		}
	}

	public static synchronized void writeTrackerPublicKey(PublicKey trackerKey) throws IOException {
		CryptoUtilities.objectToFile(trackerKey, FileNames.TRACKER_KEY_FILE.getFile());
	}

	/**
	 * The app's main method here.
	 * we do not use args anymore... we use bluenode.conf. You may set the initial settings in the file.
	 * inside this you can't use consoleprint as it is not ready yet, use system.out instead
	 * grave mistakes are being punished with System.exit(1) as die() in not ready yet
	 * 
	 * @param argv
	 */
	public static void main(String... argv) {
		System.out.println(pre+"@Started at "+Thread.currentThread().getName());
		
		if (argv.length > 1) {
			System.out.println(pre+"The application does not support any arguments. In order to provide settings edit the file:"+FileNames.CONFIG_FILE.toString()+".");
			System.exit(1);
		}
		
		//loading .conf file if exists or generating a new file and load the default settings if non existing.
		System.out.println(pre+"Checking configuration file " + FileNames.CONFIG_FILE + "...");
		ReadBluenodePreferencesFile prefs = null;
		File configFile = FileNames.CONFIG_FILE.getFile();
		if (configFile.exists()) {
			try {
				prefs = ReadBluenodePreferencesFile.ParseConfigFile(configFile);
			} catch (IOException e) {
				System.out.println(pre+"File "+configFile+" could not be loaded.");
				System.exit(1);
			}
		} else {
			System.out.println(pre+"File "+FileNames.CONFIG_FILE.toString()+" not found in the dir. Generating new file with the default settings.");
     		try {
     			ReadBluenodePreferencesFile.GenerateConfigFile(configFile);
				prefs = ReadBluenodePreferencesFile.ParseConfigFile(configFile);
			} catch (IOException e) {
				System.out.println(pre+"File "+configFile+" could not be loaded.");
				System.exit(1);
			}
		}
		
		// 3. rsa key pair
		KeyPair keys = null;
		File keyPairFile = FileNames.KEY_PAIR_FILE.getFile();
		if (keyPairFile.exists()) {
			// the tracker has key pair
			System.out.println(pre+"Loading RSA key pair from file...");
			try {
				keys = CryptoUtilities.fileToObject(keyPairFile);
				System.out.println(pre +
						"Your public key is:\n" + CryptoUtilities.bytesToBase64String(keys.getPublic().getEncoded()));
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
				keys = CryptoUtilities.generateRSAkeyPair();
				// and storing
				System.out.println(pre + "Generating key file...");
				CryptoUtilities.objectToFile(keys, keyPairFile);
				System.out.println(pre +
						"Your public key is:\n" + CryptoUtilities.bytesToBase64String(keys.getPublic().getEncoded()));
			} catch (GeneralSecurityException | IOException e) {
				System.out.println(pre+"could not generate an RSA keypair");
				System.out.println(e.getMessage());
				System.exit(1);
			}
		}
		
		// tracker's public
		PublicKey trackerKey = null;
		File trackerPublic = FileNames.TRACKER_KEY_FILE.getFile();
		if (trackerPublic.exists()) {
			try {
				trackerKey = CryptoUtilities.fileToObject(trackerPublic);
			} catch (GeneralSecurityException | IOException e) {
				System.out.println(pre+"could not read public key");
				System.out.println(e.getMessage());
				System.exit(1);
			}
		} 

		// generating hostlist file if not existing
	    System.out.println(pre+"Checking file " + FileNames.HOST_LIST_FILE.toString() + "...");
		AccountTable accounts = null;
		File hostFile = FileNames.HOST_LIST_FILE.getFile();
		if (configFile.exists()) {
			//loading accounts
			System.out.println(pre+"File "+configFile+" exists in the dir.");
			if (!prefs.network && prefs.useList) {
				//reading file
				System.out.println(pre+"Reading accounts from file.");
				accounts = new AccountTable();
				try {
					ReadBluenodePreferencesFile.ParseHostClientList(hostFile);
				} catch (GeneralSecurityException | IOException e) {
					System.out.println(hostFile+" was wrongly formated. Please edit or delete the file.");
					System.exit(1);
				}
				System.out.println(pre+"hostlist loaded with "+accounts.getSize()+" host-clients.");
			}
		} else {
			//generating
			System.out.println(pre+hostFile+" file not found in the dir. Generating new file with the default settings");
			try {
				ReadBluenodePreferencesFile.GenerateHostClientFile(hostFile);
			} catch (Exception e) {
				System.out.println(pre+"File "+hostFile+" could not be generated.");
				System.exit(1);
			}
		}

		// test and init gui if enabled
		if (prefs.gui) {
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
		if (prefs.log) {
			writeToLogFile("-------------"+ FixedDate.getFullTimestamp(new Date())+"-------------");
		}

		// init a bluenode object
		System.out.println(pre+"starting Bluenode instance...");
		Bluenode.newInstance(prefs, accounts, keys, trackerKey);
	}
}
