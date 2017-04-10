package kostiskag.unitynetwork.bluenode;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import kostiskag.unitynetwork.bluenode.RunData.Tables.AccountsTable;

public class BlueNodeTest {
	
	@Test
	public void evaluatesExpression() {
		assertTrue(true);
	}

	@Ignore
	public void initBlueNode() {		
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
		AccountsTable accounts = null;

		String name = "pakis1";
		int authPort = 7000;
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
				accounts);
		App.bn = bn;
		bn.run();
	}
}