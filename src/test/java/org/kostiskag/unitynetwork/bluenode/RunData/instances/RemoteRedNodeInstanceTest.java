package org.kostiskag.unitynetwork.bluenode.RunData.instances;

import static org.junit.Assert.*;

import org.junit.Test;

public class RemoteRedNodeInstanceTest {

	@Test
	public void test() {
		RemoteRedNodeInstance rn = new RemoteRedNodeInstance("ouiou", "wapaf", null);
		assertEquals(rn.getHostname(), "ouiou");
		assertEquals(rn.getVaddress(), "wapaf");		
		System.out.println(rn.getTime());
		String time = rn.getTime();
		assertTrue(time.equals(rn.getTime()));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rn.updateTime();
		assertTrue(!time.equals(rn.getTime()));
	}

}
