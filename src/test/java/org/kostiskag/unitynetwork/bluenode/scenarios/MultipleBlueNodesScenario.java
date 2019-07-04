package org.kostiskag.unitynetwork.bluenode.scenarios;

import java.security.GeneralSecurityException;
import java.security.KeyPair;

import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.bluenode.rundata.table.AccountTable;

public class MultipleBlueNodesScenario   {
	
	public static void main(String[] args) throws GeneralSecurityException {
		boolean network = true;
		String trackerAddress = "127.0.0.1";
		int trackerPort = 8000;
		int trackerTime = 2;
		boolean useList = false;
		int startPort = 20000;
		int endPort = 22000;
		int maxRednodeEntries = 30;
		boolean gui = false;
		boolean soutTraffic = false;
		boolean log = false;
		AccountTable accounts = null;
		KeyPair keys = CryptoUtilities.generateRSAkeyPair();

		String name = "Pakis1";
		int authPort = 7000;
		/*
		BlueNode bn = new BlueNode(
				network, 
				trackerAddress, 
				trackerPort,
				trackerTime,
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
				keys);
		App.bn = bn;
		bn.start();
		*/
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*
		for (int i=2; i<10; i++) {
			name = "Pakis"+i;
			authPort = 7000+i;
			BlueNode a = new BlueNode(
					network, 
					trackerAddress, 
					trackerPort, 
					trackerTime,
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
					keys);
			a.start();
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		*/			
	}
}
