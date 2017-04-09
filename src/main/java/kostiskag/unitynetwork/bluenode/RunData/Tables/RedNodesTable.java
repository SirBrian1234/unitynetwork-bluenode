package kostiskag.unitynetwork.bluenode.RunData.Tables;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.RunData.Instances.RedNodeInstance;

/**
 *
 * @author kostis
 */
public class RedNodesTable {

    private static String pre = "^ADDRTABLE ";
    private RedNodeInstance[] table;
    private RedNodeInstance temp;
    private int size;
    private int count;    

    public RedNodesTable(int size) {
        this.size = size;
        table = new RedNodeInstance[size];
        for (int i = 0; i < size; i++) {
            table[i] = new RedNodeInstance();
        }
        App.bn.ConsolePrint(pre + "INITIALIZED " + size);
    }

    public RedNodeInstance getRedNodeInstanceByHn(String hostname) {
        for (int i = 0; i < size; i++) {
            if (hostname.equals(table[i].getHostname())) {
                return table[i];
            }
        }
        App.bn.ConsolePrint(pre + "NO ENTRY FOR " + hostname + " IN TABLE");
        return null;
    }
    
    public RedNodeInstance getRedNodeInstanceByAddr(String vaddress) {
        for (int i = 0; i < size; i++) {
            if (vaddress.equals(table[i].getVaddress())) {
                return table[i];
            }
        }
        App.bn.ConsolePrint(pre + "NO ENTRY FOR " + vaddress + " IN TABLE");
        return null;
    }        

    public RedNodeInstance getRedNodeInstance(int id) {
        if (table.length > id) {
            return table[id];
        } else {
            App.bn.ConsolePrint(pre + "NO ENTRY " + id + " IN TABLE");
            return null;
        }
    }

    public int lease(RedNodeInstance auth) {
        if (count < size) {
            table[count] = auth;
            App.bn.ConsolePrint(pre + count + " LEASED " + auth.getVaddress() + " ~ " + auth.getPhAddress());
            count++;
            return count;
        } else {
            App.bn.ConsolePrint(pre + "NO MORE SPACE INSIDE ADDRESSTABLE");
            return -1;
        }
    }   

    public void release(String vAddress) {
        for (int i = 0; i < count; i++) {
            if (vAddress.equals(table[i].getVaddress())) //release                                                
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
        App.bn.ConsolePrint(pre + "NO ENTRY FOR " + vAddress + " IN TABLE");
    }

    public Boolean checkOnline(String vAddress) {
        for (int i = 0; i < count; i++) {
            if (vAddress.equals(table[i].getVaddress())) {
                return true;
            }
        }
        return false;
    }

    public void delete(int[] delTable) {
        App.bn.ConsolePrint(pre + "FORCE DELETING " + delTable.length + " LOCAL RED NODES");
        for (int i = delTable.length; i > 0; i--) {
            String address = getRedNodeInstance(delTable[i - 1]).getVaddress();
            App.bn.ConsolePrint(pre + "DELETING " + address);
            getRedNodeInstanceByAddr(address).forceDelete();
            release(address);
        }
        updateTable();
    }
    
    // in case of network fall we have to release all the connected red nodes
    public void releaseAll() {
    	for (int i=0; i<count; i++) {
    		table[i].exit();
    		table[i] = null;    		
    	}
    	count = 0;
    	updateTable();
    }

    public void updateTable() {
        if (App.bn.gui == true) {
            int rows = MainWindow.hostable.getRowCount();
            for (int i = 0; i < rows; i++) {
                MainWindow.hostable.removeRow(0);
            }
            for (int i = 0; i < count; i++) {
                MainWindow.hostable.addRow(new Object[]{table[i].getVaddress(), table[i].getHostname(), table[i].getUsername(), table[i].getPhAddress(), table[i].getUp().getUpport(), table[i].getDown().getDownport()});
            }
        }
    }    
    
    public int getSize() {
        return count;
    }
}
