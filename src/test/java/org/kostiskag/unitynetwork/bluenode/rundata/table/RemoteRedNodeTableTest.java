package org.kostiskag.unitynetwork.bluenode.rundata.table;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.PortHandle;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.concurrent.locks.Lock;

public class RemoteRedNodeTableTest {

	@BeforeClass
	public static void before() {
		AppLogger.newInstance(false, null,false,false);
		PortHandle.newInstance(10,100);
	}

	@Test
	public void initTest() throws UnknownHostException, GeneralSecurityException, InterruptedException, IllegalAccessException {
		RemoteRedNodeTable table = new RemoteRedNodeTable(new BlueNode("Pakis",null, "1.2.3.4", 0), Collections.emptyList(), true, false);

		Lock lock = null;
		try {
			lock = table.aquireLock();

			assertEquals(table.getSize(lock),0);
			table.lease(lock, "ouiou", "10.0.0.200");
			table.lease(lock, "ouiou2", "10.0.0.201");
			table.lease(lock, "ouiou3", "10.0.0.202");
			table.lease(lock, "ouiou4", "10.0.0.203");
			assertEquals(table.getSize(lock),4);

		} catch (InterruptedException e) {

		} finally {
			lock.unlock();

		}
	}

	@Test
	public void getByHost() throws UnknownHostException, GeneralSecurityException, InterruptedException, IllegalAccessException {
		RemoteRedNodeTable table = new RemoteRedNodeTable(new BlueNode("Pakis",null, "1.2.3.4", 0), Collections.emptyList(), true, false);
		Lock lock = null;
		try {
			lock = table.aquireLock();

			assertEquals(table.getSize(lock),0);
			table.lease(lock, "ouiou", "10.0.0.200");
			table.lease(lock, "ouiou2", "10.0.0.201");
			table.lease(lock, "ouiou3", "10.0.0.202");
			table.lease(lock, "ouiou4", "10.0.0.203");
			assertEquals(table.getSize(lock),4);

			assertEquals(table.getOptionalNodeEntry(lock,"ouiou2").get().getAddress().asString(),"10.0.0.201");

		} catch (InterruptedException e) {

		} finally {
			lock.unlock();

		}
	}
	
	@Test
	public void getByVaddr() throws UnknownHostException, GeneralSecurityException, InterruptedException, IllegalAccessException{
		RemoteRedNodeTable table = new RemoteRedNodeTable(new BlueNode("Pakis",null, "1.2.3.4", 0), Collections.emptyList(), true, false);

		Lock lock = null;
		try {
			lock = table.aquireLock();

			assertEquals(table.getSize(lock),0);
			table.lease(lock, "ouiou", "10.0.0.200");
			table.lease(lock, "ouiou2", "10.0.0.201");
			table.lease(lock, "ouiou3", "10.0.0.202");
			table.lease(lock, "ouiou4", "10.0.0.203");
			assertEquals(table.getSize(lock),4);

			assertEquals(table.getOptionalNodeEntry(lock, VirtualAddress.valueOf("10.0.0.203")).get().getHostname(),"ouiou4");

		} catch (InterruptedException e) {

		} finally {
			lock.unlock();
		}
	}
	
	@Test
	public void releaseByHost() throws UnknownHostException, GeneralSecurityException, InterruptedException, IllegalAccessException {
		RemoteRedNodeTable table = new RemoteRedNodeTable(new BlueNode("Pakis",null, "1.2.3.4", 0), Collections.emptyList(), true, false);
		Lock lock = null;
		try {
			lock = table.aquireLock();

			assertEquals(table.getSize(lock),0);
			table.lease(lock, "ouiou", "10.0.0.200");
			table.lease(lock, "ouiou2", "10.0.0.201");
			table.lease(lock, "ouiou3", "10.0.0.202");
			table.lease(lock, "ouiou4", "10.0.0.203");
			assertEquals(table.getSize(lock),4);

			table.release(lock, "ouiou3");

			assertEquals(table.getSize(lock),3);
			assertFalse(table.getOptionalNodeEntry(lock, "ouiou3").isPresent());

		} catch (InterruptedException e) {

		} finally {
			lock.unlock();

		}
	}
}
