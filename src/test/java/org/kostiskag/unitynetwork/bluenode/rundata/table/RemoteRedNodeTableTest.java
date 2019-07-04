package org.kostiskag.unitynetwork.bluenode.rundata.table;

import static org.junit.Assert.*;

import org.junit.Test;

public class RemoteRedNodeTableTest {

	@Test
	public void initTest() {
		RemoteRedNodeTable table = new RemoteRedNodeTable(null, false, false);
		assertEquals(table.getSize(),0);
		table.lease("ouiou", "wapaf");
		table.lease("ouiou2", "wapaf2");
		table.lease("ouiou3", "wapaf3");
		table.lease("ouiou4", "wapaf4");
		assertEquals(table.getSize(),4);
	}

	@Test
	public void getByHost() {
		RemoteRedNodeTable table = new RemoteRedNodeTable(null, false, false);
		assertEquals(table.getSize(),0);
		table.lease("ouiou", "wapaf");
		table.lease("ouiou2", "wapaf2");
		table.lease("ouiou3", "wapaf3");
		table.lease("ouiou4", "wapaf4");
		try {
			assertEquals(table.getByHostname("ouiou2").getVaddress(),"wapaf2");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void getByVaddr() {
		RemoteRedNodeTable table = new RemoteRedNodeTable(null, false, false);
		assertEquals(table.getSize(),0);
		table.lease("ouiou", "wapaf");
		table.lease("ouiou2", "wapaf2");
		table.lease("ouiou3", "wapaf3");
		table.lease("ouiou4", "wapaf4");
		try {
			assertEquals(table.getByVaddress("wapaf4").getHostname(),"ouiou4");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void releaseByHost() {
		RemoteRedNodeTable table = new RemoteRedNodeTable(null, false, false);
		assertEquals(table.getSize(),0);
		table.lease("ouiou", "wapaf");
		table.lease("ouiou2", "wapaf2");
		table.lease("ouiou3", "wapaf3");
		table.lease("ouiou4", "wapaf4");
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
