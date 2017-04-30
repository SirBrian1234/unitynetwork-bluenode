package kostiskag.unitynetwork.bluenode.functions;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import kostiskag.unitynetwork.bluenode.functions.HashFunctions;

public class HashFunctionsTest {

	@Test
	public void sha256test() {
		try {
			System.out.println(HashFunctions.SHA256("banana"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			assertTrue(false);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			assertTrue(false);
		}		
	}
	
	@Test
	public void bytesToIntTest() {
		//generate pseudo-random bytes
		//bytes[] bnum = new byte[4];
		//SecureRandom ranGen = new SecureRandom();
		//ranGen.nextBytes(bnum);
		
		byte[] bnum = new byte[] {(byte) 0x00};
		assertEquals(HashFunctions.bytesToUnsignedInt(bnum), 0);
		System.out.println(HashFunctions.bytesToHexStr(bnum)+" is "+HashFunctions.bytesToUnsignedInt(bnum)+"");
		
		bnum = new byte[] {(byte) 0xff};
		assertEquals(HashFunctions.bytesToUnsignedInt(bnum), 255);
		System.out.println(HashFunctions.bytesToHexStr(bnum)+" is "+HashFunctions.bytesToUnsignedInt(bnum)+"");
		
    	bnum =  new byte[] {0x00, 0x0a};
    	assertEquals(HashFunctions.bytesToUnsignedInt(bnum), 10);
		System.out.println(HashFunctions.bytesToHexStr(bnum)+" is "+HashFunctions.bytesToUnsignedInt(bnum)+"");
		
		bnum =  new byte[] {0x00, 0x35};
		assertEquals(HashFunctions.bytesToUnsignedInt(bnum), 53);
		System.out.println(HashFunctions.bytesToHexStr(bnum)+" is "+HashFunctions.bytesToUnsignedInt(bnum)+"");		
		
		bnum = new byte[] {(byte) 0x0e};
		System.out.println(HashFunctions.bytesToHexStr(bnum)+" is "+HashFunctions.bytesToUnsignedInt(bnum)+"");		
	}
	
	@Test
	public void intToBytesTest() {
		int num = 53;
		byte[] numbutes = HashFunctions.UnsignedIntTo4Bytes(num);
		System.out.println(num+" -> "+HashFunctions.bytesToHexStr(numbutes));
		assertEquals(HashFunctions.bytesToHexStr(numbutes), "00000035");		
		
		numbutes = HashFunctions.UnsignedIntTo2Bytes(num);
		System.out.println(num+" -> "+HashFunctions.bytesToHexStr(numbutes));
		assertEquals(HashFunctions.bytesToHexStr(numbutes), "0035");
		
		numbutes = HashFunctions.UnsignedIntToByteArray(num);
		System.out.println(num+" -> "+HashFunctions.bytesToHexStr(numbutes));
		assertEquals(HashFunctions.bytesToHexStr(numbutes), "35");
		
	}

	@Test
	public void bytesTohexToBytesTest() {
		byte[] data = new byte[] {(byte) 0x35, (byte) 0xbf};
		String datastr = HashFunctions.bytesToHexStr(data);
		byte[] dataStrToBytes = HashFunctions.hexStrToBytes(datastr);
		System.out.println(new String(data)+" "+datastr+" "+new String(dataStrToBytes));
		assertEquals(data[0], dataStrToBytes[0]);
		assertEquals(data[1], dataStrToBytes[1]);
	}

	@Test
	public void buildByteTest() {
		byte b = Byte.parseByte("00000000", 2);
		assertEquals(b, (byte) 0x00);
		
		b = HashFunctions.buildByteFromBits("00000001");
		assertEquals(b, (byte) 0x01);
		b = HashFunctions.buildByteFromBits("00001111");
		assertEquals(b, (byte) 0x0f);
		
		b = HashFunctions.buildByteFromBits("11110000");
		assertEquals(b, (byte) 0xf0);
		b = HashFunctions.buildByteFromBits("11111111");
		assertEquals(b, (byte) 0xff);
		
		b = HashFunctions.buildByteFromBits("11110001");
		assertEquals(b, (byte) 0xf1);
	}

}
