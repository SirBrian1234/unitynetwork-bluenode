package org.kostiskag.unitynetwork.bluenode.RunData.tables;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Ignore;
import org.junit.Test;

import org.kostiskag.unitynetwork.bluenode.RunData.instances.LocalRedNodeInstance;

public class LocalRedNodesTableTest {

	@Test
	public void initTest() {
		LocalRedNodeTable table = new LocalRedNodeTable(2, false, false);
		LocalRedNodeInstance rn1 = new LocalRedNodeInstance();
		LocalRedNodeInstance rn2 = new LocalRedNodeInstance();
		LocalRedNodeInstance rn3 = new LocalRedNodeInstance();
		try {
			table.lease(rn1);
			table.lease(rn2);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void maxCapTest() {
		LocalRedNodeTable table = new LocalRedNodeTable(2, false, false);
		LocalRedNodeInstance rn1 = new LocalRedNodeInstance();
		LocalRedNodeInstance rn2 = new LocalRedNodeInstance();
		LocalRedNodeInstance rn3 = new LocalRedNodeInstance();
		try {
			table.lease(rn1);
			table.lease(rn2);
			table.lease(rn3);
		} catch (Exception e) {
			
		}
		assertEquals(table.getSize(),2);
	}

	@Test
	public void getByHnTest() {
		LocalRedNodeTable table = new LocalRedNodeTable(10, false, false);
		LocalRedNodeInstance rn1 = new LocalRedNodeInstance("pakis-laptop","10.0.0.1");
		LocalRedNodeInstance rn2 = new LocalRedNodeInstance("pakis-laptop2","10.0.0.2");
		LocalRedNodeInstance rn3 = new LocalRedNodeInstance("pakis-laptop3","10.0.0.3");
		try {
			table.lease(rn1);
			table.lease(rn2);
			table.lease(rn3);
		} catch (Exception e) {
			assertTrue(false);
		}
		assertEquals(table.getRedNodeInstanceByHn("pakis-laptop3").getVaddress(),"10.0.0.3");
		assertEquals(table.getRedNodeInstanceByHn("pakis-laptop2").getVaddress(),"10.0.0.2");
		assertEquals(table.getRedNodeInstanceByHn("pakis-laptop").getVaddress(),"10.0.0.1");
	}
	
	@Test
	public void getByVaddrTest() {
		LocalRedNodeTable table = new LocalRedNodeTable(10, false, false);
		LocalRedNodeInstance rn1 = new LocalRedNodeInstance("pakis-laptop","10.0.0.1");
		LocalRedNodeInstance rn2 = new LocalRedNodeInstance("pakis-laptop2","10.0.0.2");
		LocalRedNodeInstance rn3 = new LocalRedNodeInstance("pakis-laptop3","10.0.0.3");
		try {
			table.lease(rn1);
			table.lease(rn2);
			table.lease(rn3);
		} catch (Exception e) {
			assertTrue(false);
		}
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.3").getHostname(), "pakis-laptop3");
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.2").getHostname(), "pakis-laptop2");
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.1").getHostname(), "pakis-laptop");
	}
	
	@Ignore
	public void releaseByHnTest() {
		LocalRedNodeTable table = new LocalRedNodeTable(10, false, false);
		LocalRedNodeInstance rn1 = new LocalRedNodeInstance("pakis-laptop","10.0.0.1");
		LocalRedNodeInstance rn2 = new LocalRedNodeInstance("pakis-laptop2","10.0.0.2");
		LocalRedNodeInstance rn3 = new LocalRedNodeInstance("pakis-laptop3","10.0.0.3");
		try {
			table.lease(rn1);
			table.lease(rn2);
			table.lease(rn3);
		} catch (Exception e) {
			assertTrue(false);
		}
		assertEquals(table.getSize(),3);
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.3").getHostname(), "pakis-laptop3");
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.2").getHostname(), "pakis-laptop2");
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.1").getHostname(), "pakis-laptop");
		try {
			table.releaseByHostname("pakis-laptop");			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			table.releaseByHostname("pakis-laptop3");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(table.getSize(),2);
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.2").getHostname(), "pakis-laptop2");		
	}
	
	@Test
	public void checkOnlineTest() {
		LocalRedNodeTable table = new LocalRedNodeTable(10, false, false);
		LocalRedNodeInstance rn1 = new LocalRedNodeInstance("pakis-laptop","10.0.0.1");
		LocalRedNodeInstance rn2 = new LocalRedNodeInstance("pakis-laptop2","10.0.0.2");
		LocalRedNodeInstance rn3 = new LocalRedNodeInstance("pakis-laptop3","10.0.0.3");
		try {
			table.lease(rn1);
			table.lease(rn2);
			table.lease(rn3);
		} catch (Exception e) {
			assertTrue(false);
		}
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.3").getHostname(), "pakis-laptop3");
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.2").getHostname(), "pakis-laptop2");
		assertEquals(table.getRedNodeInstanceByAddr("10.0.0.1").getHostname(), "pakis-laptop");
		assertTrue(table.checkOnlineByVaddress("10.0.0.2"));
		assertTrue(table.checkOnlineByVaddress("10.0.0.3"));
		assertTrue(table.checkOnlineByVaddress("10.0.0.1"));
		assertTrue(!table.checkOnlineByVaddress("10.0.0.4"));
		assertTrue(!table.checkOnlineByVaddress("10.0.0.5"));
	}
	
	@Test
	public void buildStringTest() {
		LocalRedNodeTable table = new LocalRedNodeTable(10, false, false);
		LocalRedNodeInstance rn1 = new LocalRedNodeInstance("pakis-laptop","10.0.0.1");
		LocalRedNodeInstance rn2 = new LocalRedNodeInstance("pakis-laptop2","10.0.0.2");
		LocalRedNodeInstance rn3 = new LocalRedNodeInstance("pakis-laptop3","10.0.0.3");
		try {
			table.lease(rn1);
			table.lease(rn2);
			table.lease(rn3);
		} catch (Exception e) {
			assertTrue(false);
		}
		
		LinkedList<String> list = table.buildAddrHostStringList();
		assertEquals(list.get(0), "pakis-laptop 10.0.0.1");
		assertEquals(list.get(1), "pakis-laptop2 10.0.0.2");
		assertEquals(list.get(2), "pakis-laptop3 10.0.0.3");
		assertEquals(list.size(), 3);
	}
}
