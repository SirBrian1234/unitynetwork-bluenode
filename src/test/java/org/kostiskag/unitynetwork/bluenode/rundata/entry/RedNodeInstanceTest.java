package org.kostiskag.unitynetwork.bluenode.rundata.entry;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;

public class RedNodeInstanceTest {

	@BeforeClass
	public static void before() {
		AppLogger.newInstance(false,false,false);
	}

	@Test
	public void test() {
		assertTrue(true);
	}

}
