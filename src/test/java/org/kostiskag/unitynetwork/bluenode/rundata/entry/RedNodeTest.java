package org.kostiskag.unitynetwork.bluenode.rundata.entry;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;

public class RedNodeTest {

	@BeforeClass
	public static void before() {
		AppLogger.newInstance(false, null,false,false);
	}

	@Test
	public void test() {
		assertTrue(true);
	}

}
