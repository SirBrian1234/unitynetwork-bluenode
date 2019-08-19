package org.kostiskag.unitynetwork.bluenode.rundata.entry;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;

import static org.junit.Assert.assertTrue;

public class BlueNodeTest {

	@BeforeClass
	public static void before() {
		AppLogger.newInstance(false, null,false,false);
	}

	@Test
	public void test() {
		assertTrue(true);
	}

}
