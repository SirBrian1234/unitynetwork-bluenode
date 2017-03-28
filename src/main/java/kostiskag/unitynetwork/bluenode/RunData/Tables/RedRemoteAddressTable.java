/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.RunData.Tables;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.RunData.Instances.RedRemoteAddress;
import kostiskag.unitynetwork.bluenode.Functions.getTime;

/**
 *
 * @author kostis
 *
 * Here we keep all the remote red nodes the table associates a red node with
 * his host blue node
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
        lvl3BlueNode.ConsolePrint(pre + "INITIALIZED " + size);
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
        lvl3BlueNode.ConsolePrint(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");
        return null;
    }

    public void lease(String vAddress, String Hostname, String BlueNodeHostname) {
        if (count < size) {
            table[count].init(vAddress, Hostname, BlueNodeHostname, getTime.getSmallTimestamp());
            lvl3BlueNode.ConsolePrint(pre + count + " LEASED " + vAddress + " KNOWN AS " + Hostname + " ON BLUE NODE " + BlueNodeHostname);
            count++;
            updateTable();
        } else {
            lvl3BlueNode.ConsolePrint(pre + "NO MORE SPACE INSIDE REMOTETABLE");
        }
    }

    public void release(int id) {
        if (id >= 0 && id < count) {

            temp = table[count - 1];
            table[count - 1] = table[id];
            table[id] = temp;
            table[count - 1].init("none", "none", "none", "none");
            count--;

            lvl3BlueNode.ConsolePrint(pre + "RELEASED ENTRY "+id);            
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

                    lvl3BlueNode.ConsolePrint(pre + "RELEASED ENTRY");
                    return;
                }
            }
        }
        lvl3BlueNode.ConsolePrint(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");
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
        lvl3BlueNode.ConsolePrint(pre + "DELETING " + delTable.length + " REMOTE RED NODES");
        for (int i = delTable.length; i > 0; i--) {
            String address = table[delTable[i - 1]].getVAddress();
            lvl3BlueNode.ConsolePrint(pre + "DELETING " + address);
            releaseByAddr(address);
        }
        updateTable();
    }

    public void updateTable() {
        //MainWindow.hostable.
        if (lvl3BlueNode.gui) {
            int rows = MainWindow.remotetable.getRowCount();
            for (int i = 0; i < rows; i++) {
                MainWindow.remotetable.removeRow(0);
            }
            for (int i = 0; i < count; i++) {
                MainWindow.remotetable.addRow(new Object[]{table[i].getVAddress(), table[i].getHostname(), table[i].getBlueNodeHostname(), table[i].getTime()});
            }
        }
    }

    public void removeAssociations(String BlueNodeHostname) {
        lvl3BlueNode.ConsolePrint(pre + "REMOVING BLUENODE "+BlueNodeHostname+" ASSOCIATIONS");
        String address = null;
        for (int i = 0; i < count; i++) {
            if (table[i].getBlueNodeHostname().equals(BlueNodeHostname)) {
                address = table[i].getVAddress();
                lvl3BlueNode.ConsolePrint(pre + "REMOVING " + address + " ~ " + table[i].getBlueNodeHostname());
                release(i);
            }
        }
        updateTable();
    }
}