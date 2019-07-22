package org.kostiskag.unitynetwork.bluenode.rundata.entry;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

public class RemoteRedNodeTest {

	@BeforeClass
	public static void before() {
		AppLogger.newInstance(false, null,false,false);
	}

	@Test
	public void test() throws GeneralSecurityException, UnknownHostException, IllegalAccessException{
		RemoteRedNode rn = RemoteRedNode.newInstance("ouiou", "10.0.0.200", new BlueNode("Pakis"));
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
