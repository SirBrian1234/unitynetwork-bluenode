package kostiskag.unitynetwork.bluenode.scenarios;

import java.security.KeyPair;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.BlueNode;
import kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;
import kostiskag.unitynetwork.bluenode.RunData.tables.AccountsTable;
import kostiskag.unitynetwork.bluenode.functions.CryptoMethods;

public class MultipleBlueNodesWithRedNodesScenario {
	
	public static void main(String[] args) {
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
		AccountsTable accounts = null;
		KeyPair keys = CryptoMethods.generateRSAkeyPair();

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
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		/*
		LocalRedNodeInstance rn = new LocalRedNodeInstance("ouiou1", "10.0.0.1");
		try {
			bn.localRedNodesTable.lease(rn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
				e1.printStackTrace();
			}
		}
		*/			
	}
}
