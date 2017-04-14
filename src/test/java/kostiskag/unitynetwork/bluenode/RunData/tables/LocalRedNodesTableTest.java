package kostiskag.unitynetwork.bluenode.RunData.tables;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Ignore;
import org.junit.Test;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.RedNodeInstance;

public class LocalRedNodesTableTest {

	@Test
	public void initTest() {
		LocalRedNodesTable table = new LocalRedNodesTable(2, false, false);
		RedNodeInstance rn1 = new RedNodeInstance();
		RedNodeInstance rn2 = new RedNodeInstance();
		RedNodeInstance rn3 = new RedNodeInstance();
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
		LocalRedNodesTable table = new LocalRedNodesTable(2, false, false);
		RedNodeInstance rn1 = new RedNodeInstance();
		RedNodeInstance rn2 = new RedNodeInstance();
		RedNodeInstance rn3 = new RedNodeInstance();
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
		LocalRedNodesTable table = new LocalRedNodesTable(10, false, false);
		RedNodeInstance rn1 = new RedNodeInstance("pakis-laptop","pakis","1234","10.0.0.1","192.168.1.1");
		RedNodeInstance rn2 = new RedNodeInstance("pakis-laptop2","pakis","1234","10.0.0.2","192.168.1.1");
		RedNodeInstance rn3 = new RedNodeInstance("pakis-laptop3","pakis","1234","10.0.0.3","192.168.1.1");
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
		LocalRedNodesTable table = new LocalRedNodesTable(10, false, false);
		RedNodeInstance rn1 = new RedNodeInstance("pakis-laptop","pakis","1234","10.0.0.1","192.168.1.1");
		RedNodeInstance rn2 = new RedNodeInstance("pakis-laptop2","pakis","1234","10.0.0.2","192.168.1.1");
		RedNodeInstance rn3 = new RedNodeInstance("pakis-laptop3","pakis","1234","10.0.0.3","192.168.1.1");
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
		LocalRedNodesTable table = new LocalRedNodesTable(10, false, false);
		RedNodeInstance rn1 = new RedNodeInstance("pakis-laptop","pakis","1234","10.0.0.1","192.168.1.1");
		RedNodeInstance rn2 = new RedNodeInstance("pakis-laptop2","pakis","1234","10.0.0.2","192.168.1.1");
		RedNodeInstance rn3 = new RedNodeInstance("pakis-laptop3","pakis","1234","10.0.0.3","192.168.1.1");
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
		LocalRedNodesTable table = new LocalRedNodesTable(10, false, false);
		RedNodeInstance rn1 = new RedNodeInstance("pakis-laptop","pakis","1234","10.0.0.1","192.168.1.1");
		RedNodeInstance rn2 = new RedNodeInstance("pakis-laptop2","pakis","1234","10.0.0.2","192.168.1.1");
		RedNodeInstance rn3 = new RedNodeInstance("pakis-laptop3","pakis","1234","10.0.0.3","192.168.1.1");
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
		assertTrue(table.checkOnline("10.0.0.2"));
		assertTrue(table.checkOnline("10.0.0.3"));
		assertTrue(table.checkOnline("10.0.0.1"));
		assertTrue(!table.checkOnline("10.0.0.4"));
		assertTrue(!table.checkOnline("10.0.0.5"));
	}
	
	@Test
	public void buldStringTest() {
		LocalRedNodesTable table = new LocalRedNodesTable(10, false, false);
		RedNodeInstance rn1 = new RedNodeInstance("pakis-laptop","pakis","1234","10.0.0.1","192.168.1.1");
		RedNodeInstance rn2 = new RedNodeInstance("pakis-laptop2","pakis","1234","10.0.0.2","192.168.1.1");
		RedNodeInstance rn3 = new RedNodeInstance("pakis-laptop3","pakis","1234","10.0.0.3","192.168.1.1");
		try {
			table.lease(rn1);
			table.lease(rn2);
			table.lease(rn3);
		} catch (Exception e) {
			assertTrue(false);
		}
		
		LinkedList<String> list = table.buildAddrHostStringList();
		assertEquals(list.get(0), "10.0.0.1 pakis-laptop");
		assertEquals(list.get(1), "10.0.0.2 pakis-laptop2");
		assertEquals(list.get(2), "10.0.0.3 pakis-laptop3");
		assertEquals(list.size(), 3);
	}
}
