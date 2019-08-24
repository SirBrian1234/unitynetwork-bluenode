package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.net.UnknownHostException;
import java.util.concurrent.locks.Lock;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.PortHandle;
import org.kostiskag.unitynetwork.common.address.PhysicalAddress;
import org.kostiskag.unitynetwork.common.address.VirtualAddress;

public class BlueNodeTableTest {

    static BlueNodeTable table;

    @BeforeClass
    public static void before() {
        PortHandle.newInstance(10,100);
        AppLogger.newInstance(false, null,false,false);
        table = BlueNodeTable.newInstance((bnObj) -> System.out.println(bnObj), (rrnObj)-> System.out.println(rrnObj));
    }

    @Test
    public void test() throws IllegalAccessException, InterruptedException, UnknownHostException {
        Lock lock = null;
        try {
            lock = table.aquireLock();
            table.leaseBlueNode(lock, new BlueNode("Pakis",null, PhysicalAddress.valueOf("9.9.9.9"), 200));
            table.leaseBlueNode(lock, new BlueNode("Lakis",null, PhysicalAddress.valueOf("9.9.9.8"), 200));
            table.leaseBlueNode(lock, new BlueNode("Takis",null, PhysicalAddress.valueOf("9.9.9.7"), 200));
        } finally {
            lock.unlock();
        }
    }

    @Test
    public void leaseTest() {
    }
}
