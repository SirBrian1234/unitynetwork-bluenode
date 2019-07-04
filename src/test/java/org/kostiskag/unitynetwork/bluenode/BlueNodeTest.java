package org.kostiskag.unitynetwork.bluenode;

import static org.junit.Assert.assertTrue;

import java.security.GeneralSecurityException;
import java.security.KeyPair;

import org.junit.Ignore;
import org.junit.Test;

import org.kostiskag.unitynetwork.bluenode.rundata.table.AccountTable;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;

public class BlueNodeTest {
	
	@Test
	public void evaluatesExpression() {
		assertTrue(true);
	}

	@Ignore
	public void initBlueNode() throws GeneralSecurityException {
		boolean network = true;
		String trackerAddress = "127.0.0.1";
		int trackerPort = 8000;
		int trackerTime = 2;
		boolean useList = false;
		int startPort = 20000;
		int endPort = 22000;
		int maxRednodeEntries = 30;
		boolean gui = true;
		boolean soutTraffic = true;
		boolean log = false;
		AccountTable accounts = null;
		KeyPair keys = CryptoUtilities.generateRSAkeyPair();

		String name = "pakis1";
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
		bn.run();*/
	}
}