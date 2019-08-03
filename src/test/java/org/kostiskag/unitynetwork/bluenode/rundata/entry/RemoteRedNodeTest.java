package org.kostiskag.unitynetwork.bluenode.rundata.entry;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.service.PortHandle;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

public class RemoteRedNodeTest {

	@BeforeClass
	public static void before() {
		AppLogger.newInstance(false, null,false,false);
		PortHandle.newInstance(10, 100);
	}

	@Test
	public void test() throws GeneralSecurityException, UnknownHostException, IllegalAccessException, InterruptedException{
		RemoteRedNode rn = RemoteRedNode.newInstance("ouiou", "10.0.0.200", new BlueNode("Pakis",null, "1.2.3.4", 0));
		assertEquals(rn.getHostname(), "ouiou");
		assertEquals(rn.getAddress().asString(), "10.0.0.200");
		System.out.println(rn.getTimestamp());
		String time = rn.getTimestamp().asDate().toString();
		assertTrue(time.equals(rn.getTimestamp().asDate().toString()));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rn.updateTimestamp();
		assertTrue(!time.equals(rn.getTimestamp()));
	}

}
