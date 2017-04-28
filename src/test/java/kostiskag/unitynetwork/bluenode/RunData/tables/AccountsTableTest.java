package kostiskag.unitynetwork.bluenode.RunData.tables;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.Ignore;
import org.junit.Test;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.tables.AccountsTable;
import kostiskag.unitynetwork.bluenode.functions.HashFunctions;

public class AccountsTableTest {
	
	@Test
	public void initTest() {
		AccountsTable table = new AccountsTable();
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
		AccountsTable table = new AccountsTable();
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
		AccountsTable table = new AccountsTable();
		try {
			table.insert("pakis", "1234", "pakis-1", 1);
			table.insert("pakis", "1234", "pakis-2", 3);
			table.insert("pakis", "1234", "pakis-3", 4);
			table.insert("pakis", "1234", "pakis-4", 5);
		} catch (Exception e) {
			assertTrue(false);
		}
		
		try {
			String pass = HashFunctions.SHA256(HashFunctions.SHA256(App.SALT) +  HashFunctions.SHA256("pakis") + HashFunctions.SHA256(App.SALT + "1234"));
			assertTrue(table.checkList("pakis-1", "pakis", pass));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
	}
	
	@Test
	public void getvaddrTest() {
		AccountsTable table = new AccountsTable();
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
			String pass = HashFunctions.SHA256(HashFunctions.SHA256(App.SALT) +  HashFunctions.SHA256("pakis") + HashFunctions.SHA256(App.SALT + "1234"));
			assertEquals(table.getVaddrIfExists("pakis-3", "pakis", pass), "10.0.0.5");
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}