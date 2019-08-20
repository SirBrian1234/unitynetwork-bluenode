package org.kostiskag.unitynetwork.bluenode.rundata.table;

import java.net.UnknownHostException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.PortHandle;

public class BlueNodeTableTest {

    static BlueNodeTable table;

    @BeforeClass
    public static void before() {
        PortHandle.newInstance(10,100);
        AppLogger.newInstance(false, null,false,false);
        table = BlueNodeTable.newInstance(null);
    }

    @Test
    public void test() throws IllegalAccessException, InterruptedException, UnknownHostException {
        table.leaseBlueNode(new BlueNode("Pakis",null, "9.9.9.9", 200));
        table.leaseBlueNode(new BlueNode("Lakis",null, "9.9.9.8", 200));
        table.leaseBlueNode(new BlueNode("Takis",null, "9.9.9.7", 200));



    }
}
