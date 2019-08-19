package org.kostiskag.unitynetwork.bluenode.rundata.table;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kostiskag.unitynetwork.bluenode.AppLogger;
import org.kostiskag.unitynetwork.bluenode.gui.MainWindow;
import org.kostiskag.unitynetwork.bluenode.rundata.entry.BlueNode;
import org.kostiskag.unitynetwork.bluenode.service.PortHandle;

public class BlueNodeTableTest {

    static BlueNodeTable table;

    @BeforeClass
    public static void before() {
        AppLogger.newInstance(false, null,false,false);
        table = BlueNodeTable.newInstance(null);
    }

    @Test
    public void test() {

    }
}
