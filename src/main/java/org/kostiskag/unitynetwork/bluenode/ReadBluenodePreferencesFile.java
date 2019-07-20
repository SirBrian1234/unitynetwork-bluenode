package org.kostiskag.unitynetwork.bluenode;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.ReadPreferencesFile;

import org.kostiskag.unitynetwork.bluenode.rundata.entry.LocalAccount;


/**
 *
 * @author Konstantinos Kagiampakis
 */
final class ReadBluenodePreferencesFile extends ReadPreferencesFile {
	public final boolean network;
	public final PhysicalAddress trackerAddress;
	public final int trackerPort;
	public final int trackerMaxIdleTimeMin;
	public final String name;
	public final boolean useList;
	public final int authPort;
	public final int startPort;
	public final int endPort;
	public final int maxRednodeEntries;
	public final boolean gui;
	public final boolean soutTraffic;
	public final boolean log;

	public ReadBluenodePreferencesFile(File file) throws IOException {
		super(file);
		this.network = Boolean.parseBoolean(cfg.getProperty("Network").strip());
		this.trackerAddress = PhysicalAddress.valueOf(cfg.getProperty("UnityTrackerAddress").strip());
		this.trackerPort = Integer.parseInt(cfg.getProperty("UnityTrackerAuthPort").strip());
		this.trackerMaxIdleTimeMin = Integer.parseInt( cfg.getProperty("TrackerMaxIdleTimeMin").strip());
		this.name = cfg.getProperty("Name").strip();
		this.useList = Boolean.parseBoolean(cfg.getProperty("UseHostList").strip());
		this.authPort = Integer.parseInt(cfg.getProperty("AuthPort").strip());
		this.startPort = Integer.parseInt(cfg.getProperty("udpstart").strip());
		this.endPort = Integer.parseInt(cfg.getProperty("udpend").strip());
		this.maxRednodeEntries = Integer.parseInt(cfg.getProperty("RedNodeLimit").strip());
		this.gui = Boolean.parseBoolean(cfg.getProperty("UseGUI").strip());
		this.soutTraffic = Boolean.parseBoolean(cfg.getProperty("ConsoleTraffic").strip());
		this.log = Boolean.parseBoolean(cfg.getProperty("Log").strip());
	}

	public static ReadBluenodePreferencesFile ParseConfigFile(File file) throws IOException {
        return new ReadBluenodePreferencesFile(file);
    }

    public static List<LocalAccount> ParseHostClientList(File hostListFile) throws GeneralSecurityException, IOException {
		List<LocalAccount> list = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(hostListFile))) {
			while (br.ready()) {
				String line = br.readLine();
				if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("\n") && !line.startsWith(" ")) {
					String[] validline = line.split("\\s+");
					list.add(new LocalAccount(validline[0], validline[1], validline[2], VirtualAddress.valueOf(validline[3])));
				}
			}
		};
        return list;
    }
    
    public static void GenerateConfigFile(File file) throws IOException {
		ReadPreferencesFile.generateFile(file, () -> String.join("\n",
			"#####################################",
			"#   BlueNode Configuration File     #",
			"#####################################",
			"",
			"#",
			"# Insructions for setting up the config file",
			"#",
			"# Do not comment any variable nor remove any from this file as this will result",
			"# in an application error. Change the value to an appropriate input as described",
			"# instead. If this file gets messed up, you may delete it and it will be",
			"# auto-generated from the app.",
			"#",
			"",
			"#",
			"# Network Type",
			"#",
			"# Network = false - for Local Network. The BlueNode may not connect to a tracker and will",
			"# serve only local connected RedNodes",
			"# Network = true - for Full Network. The BlueNode will seek a tracker to be a part in",
			"# a full network with other BlueNodess and remote RedNodess",
			"#",
			"Network = false",
			"",
			"#",
			"# variables for FullNetwork",
			"#",
			"# if you have selected Local Network these variables will not take any effect",
			"#",
			"",
			"# Provide the central tracker's address",
			"# with an IP address or with a domain.",
			"# Provide the tracker's TCP auth port. 8000 is the default.",
			"UnityTrackerAddress = 192.168.1.1",
			"UnityTrackerAuthPort = 8000",
			"",
			"# This is the network's reverse lookup time in minutes, it has to be double from",
			"# The tracker's ping time.",
			"TrackerMaxIdleTimeMin = 2",
			"",
			"# Set the Name of this BlueNode",
			"# In Full Network the BN's name must be registered in the tracker's database",
			"# Set the TCP auth port. 7000 is the default.",
			"Name = BlueNode",
			"AuthPort = 7000",
			"",
			"#",
			"# variables for LocalNetwork",
			"#",
			"# if you have selected Full Network these will not take effect",
			"#",
			"",
			"# use list true - false (false means that any client can log in as he states himself",
			"# true means only a defined user in the file users.list can login",
			"# holds the list",
			"UseHostList = false",
			"",
			"#",
			"# Load and Capacity",
			"#",
			"# This is the BlueNode's UDP port range",
			"udpstart = 20000",
			"udpend = 22000",
			"",
			"# Set the upper limit of RNs for this BlueNode",
			"RedNodeLimit = 20",
			"",
			"#",
			"# Application behaviour",
			"#",
			"",
			"# set GUI or command line",
			"# with true or false",
			"UseGUI = true",
			"",
			"# Select whether to verbose traffic in command line.",
			"# By default is disabled as it fills up the terminal",
			"# and you can allways monitor it in the GUI.",
			"# It useful if you are under a remote terminal.",
			"ConsoleTraffic = false",
			"",
			"# Logging in bluenode.log",
			"# use true or false",
			"Log = true",
			""));
    }
    
    public static void GenerateHostClientFile(File file) throws IOException {
		ReadPreferencesFile.generateFile(file, () -> String.join("\n",
			"###############################",
			"#   Host-Client List File     #",
			"###############################",
			"",
			"#",
			"# This file will take effect only when in bluenode.conf the following were",
			"# defined:",
			"#",
			"# when a BlueNode runs a Local Network",
			"# Network = false",
			"#",
			"# when a BlueNode has requested to use a user.list" ,
			"# UseClientList = true",
			"#",
			"",
			"#",
			"# In this file you may define a list of allowed host-clients",
			"# to be authenticated by the Blue Node",
			"#",
			"# Use like:",
			"# Username Password Hostname Virtual_Address_Number",
			"#",
			"# for the Virtual_Address_Number field use an integer starting from number 1, ",
			"# the BlueNode will auto convert the number to its respective IP address",
			"# you may repeat the same Username and Password with a different Hostname",
			"# and Virtual_Address in case the same user owns more than one devices",
			"#",
			"# ex.",
			"# bob 12345 bob-laptop 2",
			"# bob 12345 bob-mobile 3",
			"#",
			""));
    }

}