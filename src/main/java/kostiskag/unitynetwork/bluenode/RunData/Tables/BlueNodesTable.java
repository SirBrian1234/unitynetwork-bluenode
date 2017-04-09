package kostiskag.unitynetwork.bluenode.RunData.Tables;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.RunData.Instances.BlueNodeInstance;

/**
 *
 * @author kostis
 */
public class BlueNodesTable {

    private static String pre = "^REMOTE BLUENODE TABLE ";
    private BlueNodeInstance[] table;
    private int size;
    private int count;
    private BlueNodeInstance temp;

    public BlueNodesTable(int size) {
        this.size = size;

        table = new BlueNodeInstance[size];
        for (int i = 0; i < size; i++) {
            table[i] = null;
        }
        App.bn.ConsolePrint(pre + "INITIALIZED " + size);
    }

    public BlueNodeInstance getBlueNodeInstanceByHn(String hostname) {
        for (int i = 0; i < count; i++) {
            if (table[i].getHostname().equals(hostname)) {
                return table[i];
            }
        }
        return null;
    }

    public BlueNodeInstance getBlueNodeInstance(int place) {
        return table[place];
    }

    public int getId(String hostname) {
        for (int i = 0; i < count; i++) {
            if (hostname.equals(table[i].getHostname())) {
                return i;
            }
        }
        return -1;
    }

    //to lease tha efarmozetai sto telos ths diadikasias assosiate
    public void lease(BlueNodeInstance node) {
        if (count < size) {
            table[count] = node;
            App.bn.ConsolePrint(pre + count + " LEASED " + node.getHostname() + " ~ " + node.getPhaddress() + ":" + node.getDownport() + ":" + node.getUpport());
            count++;
            updateTable();
        } else {
            App.bn.ConsolePrint(pre + "NO MORE SPACE INSIDE REMOTE BLUENODE TABLE");
        }
    }

    private void release(int id) {
        for (int i = 0; i < size; i++) {
            if (i == id) //release                                                
            {
                if (count != 0) {

                    temp = table[count - 1];
                    table[count - 1] = table[i];
                    table[i] = temp;
                    table[count - 1] = null;
                    count--;

                    App.bn.ConsolePrint(pre + "RELEASED ENTRY");
                    updateTable();
                    return;
                }
            }
        }
        App.bn.ConsolePrint(pre + "NO ENTRY FOR " + id + " IN TABLE");
    }

    public boolean checkBlueNode(String hostname) {
        for (int i = 0; i < count; i++) {
            if (hostname.equals(table[i].getHostname())) {
                return true;
            }
        }
        return false;
    }

    public void delete(int[] delTable) {
        App.bn.ConsolePrint(pre + "DELETING " + delTable.length + " BLUE NODES");
        for (int i = delTable.length; i > 0; i--) {
            App.bn.ConsolePrint(pre + "DELETING BLUE NODE " + delTable[i - 1]);
            getBlueNodeInstance(delTable[i - 1]).killtasks();
            getBlueNodeInstance(delTable[i - 1]).getQueueMan().clear();
            release(delTable[i - 1]);
        }
        updateTable();
    }

    public void removeSingle(String hostname) {
        int place = getId(hostname);
        if (place != -1) {
            App.bn.ConsolePrint(pre + "DELETING BLUE NODE " + hostname);
            getBlueNodeInstance(place).killtasks();
            getBlueNodeInstance(place).getQueueMan().clear();
            release(place);
            updateTable();
        }
    }

    public void updateTable() {
        //MainWindow.hostable.
        if (App.bn.gui) {
            int rows = MainWindow.remotebtable.getRowCount();
            for (int i = 0; i < rows; i++) {
                MainWindow.remotebtable.removeRow(0);
            }
            for (int i = 0; i < count; i++) {
                MainWindow.remotebtable.addRow(new Object[]{table[i].getHostname(), table[i].getPhaddress(), table[i].getUpport(), table[i].getDownport()});
            }
        }
    }
}
