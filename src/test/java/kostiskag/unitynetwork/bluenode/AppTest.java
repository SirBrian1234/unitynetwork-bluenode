package kostiskag.unitynetwork.bluenode;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AppTest {
	@Test
	public void evaluatesExpression() {
		assertTrue(true);
	}

	@Test
	public void initBN() {
		App.network = true;
		App.trackerAddress = "127.0.0.1";
		App.trackerPort = 8000;
		App.useList = false;
		App.startPort = 20000;
		App.endPort = 22000;
		App.maxRednodeEntries = 30;
		App.gui = true;
		App.soutTraffic = true;
		App.log = false;

		App.name = "pakis1";
		App.authPort = 7000;
		App app = new App();
		
		App.name = "pakis2";
		App.authPort = 7001;
		App app1 = new App();
		
		App.name = "pakis3";
		App.authPort = 7002;
		App app2 = new App();
		
		App.name = "pakis4";
		App.authPort = 7003;
		App app3 = new App();
		assertTrue(true);
	}
}