package org.kostiskag.unitynetwork.bluenode.rundata.table;

import static org.junit.Assert.*;

import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import org.junit.Test;

import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;
import org.kostiskag.unitynetwork.common.utilities.CryptoUtilities;
import org.kostiskag.unitynetwork.common.utilities.HashUtilities;

public class AccountTableTest {
	
	@Test
	public void initTest() {
		AccountTable table = new AccountTable();
		try {
			table.insert("pakis", "1234", "pakis-1", 1);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(true);
		System.out.println(table.toString());
	}
	
	@Test
	public void checkUniqueTest() {
		AccountTable table = new AccountTable();
		try {
			table.insert("pakis", "1234", "pakis-1", 1);
			//same ip with the above
			table.insert("makis", "12345", "pakis-2", 1);
			//even if the below statement is correct the catch does not allow it to be executed
			table.insert("makis", "12345", "pakis-4", 3);
		} catch (Exception e) {
			assertTrue(true);
		}
		assertEquals(table.getSize(),1);
		
		
		try {
			table.insert("lakis", "12346", "pakis-3", 4);
			//same hostname with the above
			table.insert("takis", "12347", "pakis-3", 5);
		} catch (Exception e) {
			assertTrue(true);
		}
		assertEquals(table.getSize(),2);
		System.out.println(table.toString());		
	}
	
	@Test
	public void insetCheckTest() {
		AccountTable table = new AccountTable();
		try {
			table.insert("pakis", "1234", "pakis-1", 1);
			table.insert("pakis", "1234", "pakis-2", 3);
			table.insert("pakis", "1234", "pakis-3", 4);
			table.insert("pakis", "1234", "pakis-4", 5);
		} catch (Exception e) {
			assertTrue(false);
		}
		
		try {
			String pass = HashUtilities.SHA256(HashUtilities.SHA256(CryptoUtilities.SALT) +  HashUtilities.SHA256("pakis") + HashUtilities.SHA256(CryptoUtilities.SALT + "1234"));
			assertTrue(table.checkList("pakis-1", "pakis", pass));
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}		
	}
	
	@Test
	public void getvaddrTest() throws UnknownHostException {
		AccountTable table = new AccountTable();
		try {
			table.insert("pakis", "1234", "pakis-1", 2);
			table.insert("pakis", "1234", "pakis-2", 3);
			table.insert("pakis", "1234", "pakis-3", 4);
			table.insert("pakis", "1234", "pakis-4", 5);
		} catch (Exception e) {
			assertTrue(false);
		}
		System.out.println(table.toString());
		try {
			String pass = HashUtilities.SHA256(HashUtilities.SHA256(CryptoUtilities.SALT) +  HashUtilities.SHA256("pakis") + HashUtilities.SHA256(CryptoUtilities.SALT + "1234"));
			assertEquals(table.getVaddrIfExists("pakis-3", "pakis", pass), VirtualAddress.valueOf(4));
		} catch ( GeneralSecurityException e) {
			e.printStackTrace();
		}
	}
}