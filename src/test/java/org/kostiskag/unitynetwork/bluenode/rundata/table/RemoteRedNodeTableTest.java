package org.kostiskag.unitynetwork.bluenode.rundata.table;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

public class RemoteRedNodeTableTest {

	@BeforeClass
	public static void before() {
		AppLogger.newInstance(false, null,false,false);
	}

	@Test
	public void initTest() throws UnknownHostException, GeneralSecurityException {
		RemoteRedNodeTable table = new RemoteRedNodeTable(new BlueNode("Pakis"), false, false);
		assertEquals(table.getSize(),0);
		table.lease("ouiou", "10.0.0.200");
		table.lease("ouiou2", "10.0.0.201");
		table.lease("ouiou3", "10.0.0.202");
		table.lease("ouiou4", "10.0.0.203");
		assertEquals(table.getSize(),4);
	}

	@Test
	public void getByHost() throws UnknownHostException, GeneralSecurityException {
		RemoteRedNodeTable table = new RemoteRedNodeTable(new BlueNode("Pakis"), false, false);
		assertEquals(table.getSize(),0);
		table.lease("ouiou", "10.0.0.203");
		table.lease("ouiou2", "10.0.0.202");
		table.lease("ouiou3", "10.0.0.201");
		table.lease("ouiou4", "10.0.0.200");
		try {
			assertEquals(table.getByHostname("ouiou2").getAddress().asString(),"10.0.0.202");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void getByVaddr() throws UnknownHostException, GeneralSecurityException{
		RemoteRedNodeTable table = new RemoteRedNodeTable(new BlueNode("Pakis"), false, false);
		assertEquals(table.getSize(),0);
		table.lease("ouiou", "10.0.0.200");
		table.lease("ouiou2", "10.0.0.201");
		table.lease("ouiou3", "10.0.0.202");
		table.lease("ouiou4", "10.0.0.203");
		try {
			assertEquals(table.getByVaddress("10.0.0.203").getHostname(),"ouiou4");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void releaseByHost() throws UnknownHostException, GeneralSecurityException {
		RemoteRedNodeTable table = new RemoteRedNodeTable(new BlueNode("Pakis"), false, false);
		assertEquals(table.getSize(),0);
		table.lease("ouiou", "10.0.0.200");
		table.lease("ouiou2", "10.0.0.201");
		table.lease("ouiou3", "10.0.0.202");
		table.lease("ouiou4", "10.0.0.203");
		assertEquals(table.getSize(),4);
		
		try {
			table.releaseByHostname("ouiou3");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		assertEquals(table.getSize(),3);
		try {
			table.getByHostname("ouiou3");
		} catch (Exception e) {
			assertTrue(true);
		}
		
	}
}
