package kostiskag.unitynetwork.bluenode.RunData.tables;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.RunData.instances.RedRemoteAddress;
import kostiskag.unitynetwork.bluenode.functions.getTime;

/**
 * Here we keep all the remote red nodes the table associates a red node with
 * his host blue node
 * 
 * @author kostis
 */
public class RedRemoteAddressTable {

    private static String pre = "^REMOTE REDNODE TABLE ";
    private RedRemoteAddress[] table;
    private int size;
    private int count;
    private RedRemoteAddress temp;

    public RedRemoteAddressTable(int size) {
        this.size = size;
        table = new RedRemoteAddress[size];
        for (int i = 0; i < size; i++) {
            table[i] = new RedRemoteAddress("none", "none", "none", "none");
        }
        App.bn.ConsolePrint(pre + "INITIALIZED " + size);
    }

    public RedRemoteAddress getRedRemoteAddress(int i) {
        return table[i];
    }

    public RedRemoteAddress getRedRemoteAddress(String vAddress) {
        for (int i = 0; i < count; i++) {
            if (vAddress.equals(table[i].getVAddress())) {
                return table[i];
            }
        }
        App.bn.ConsolePrint(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");
        return null;
    }

    public void lease(String Hostname, String vAddress, String BlueNodeHostname) {
        if (count < size) {
            table[count].init(vAddress, Hostname, BlueNodeHostname, getTime.getSmallTimestamp());
            App.bn.ConsolePrint(pre + count + " LEASED " + vAddress + " KNOWN AS " + Hostname + " ON BLUE NODE " + BlueNodeHostname);
            count++;
            updateTable();
        } else {
            App.bn.ConsolePrint(pre + "NO MORE SPACE INSIDE REMOTETABLE");
        }
    }

    public void release(int id) {
        if (id >= 0 && id < count) {

            temp = table[count - 1];
            table[count - 1] = table[id];
            table[id] = temp;
            table[count - 1].init("none", "none", "none", "none");
            count--;

            App.bn.ConsolePrint(pre + "RELEASED ENTRY "+id);            
        }       
    }

    public void releaseByAddr(String vAddress) {
        for (int i = 0; i < count; i++) {
            if (vAddress.equals(table[i].getVAddress())) //release                                                
            {
                if (count != 0) {

                    temp = table[count - 1];
                    table[count - 1] = table[i];
                    table[i] = temp;
                    table[count - 1].init("none", "none", "none", "none");
                    count--;

                    App.bn.ConsolePrint(pre + "RELEASED ENTRY");
                    return;
                }
            }
        }
        App.bn.ConsolePrint(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");
    }

    public Boolean checkAssociated(String vAddress) {
        for (int i = 0; i < count; i++) {
            if (vAddress.equals(table[i].getVAddress())) {
                return true;
            }
        }
        return false;
    }

    public void delete(int[] delTable) {
        App.bn.ConsolePrint(pre + "DELETING " + delTable.length + " REMOTE RED NODES");
        for (int i = delTable.length; i > 0; i--) {
            String address = table[delTable[i - 1]].getVAddress();
            App.bn.ConsolePrint(pre + "DELETING " + address);
            releaseByAddr(address);
        }
        updateTable();
    }

    public void updateTable() {
        //MainWindow.hostable.
        if (App.bn.gui) {
            int rows = MainWindow.remotetable.getRowCount();
            for (int i = 0; i < rows; i++) {
                MainWindow.remotetable.removeRow(0);
            }
            for (int i = 0; i < count; i++) {
                MainWindow.remotetable.addRow(new Object[]{table[i].getVAddress(), table[i].getHostname(), table[i].getBlueNodeName(), table[i].getTime()});
            }
        }
    }

    public void removeAssociations(String BlueNodeHostname) {
        App.bn.ConsolePrint(pre + "REMOVING BLUENODE "+BlueNodeHostname+" ASSOCIATIONS");
        String address = null;
        for (int i = 0; i < count; i++) {
            if (table[i].getBlueNodeName().equals(BlueNodeHostname)) {
                address = table[i].getVAddress();
                App.bn.ConsolePrint(pre + "REMOVING " + address + " ~ " + table[i].getBlueNodeName());
                release(i);
            }
        }
        updateTable();
    }
}
