/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kostiskag.unitynetwork.bluenode.Functions;

import kostiskag.unitynetwork.bluenode.BlueNode.lvl3BlueNode;
import java.util.Stack;

/**
 *
 * @author kostis
 */
public class PortHandle {

    public String pre = "^PORTHANDLE ";
    public Stack<Integer> udpSourcePorts;
    public int startport;
    public int endport;
    public int[] ports;
    public int count=0;
    
    public PortHandle(int startport,int endport) {        
        this.startport = startport;
        this.endport = endport;
        ports = new int[endport-startport];
    }

    public int requestPort() {
        int portToUse;
        do {
            portToUse = startport + (int)(Math.random() * ((endport - startport) + 1));
         }
         while (checkTable(portToUse));
         lvl3BlueNode.ConsolePrint(pre + "USING A NEW PORT " + portToUse);
         tableAdd(portToUse);
         return portToUse;
    }

    public int requestNewPort(int oldPort) {
        int portToUse;
        
            do {
                portToUse = startport + (int)(Math.random() * ((endport - startport) + 1));
            } while (checkTable(portToUse) && portToUse != oldPort);            
            lvl3BlueNode.ConsolePrint(pre + "USING A NEW PORT " + portToUse);
            tableAdd(portToUse);
            return portToUse;
    }
    
    public void releasePort(int port) {
        for (int i=0; i<count; i++){
            if (ports[i] == port){                
                ports[i]= ports[count-1];
                ports[count-1]=0;
                count--;
                lvl3BlueNode.ConsolePrint(pre + "PORT " + port +" RELEASED");
            }                                
        }            
    }
    
    public boolean checkTable(int port){
        for (int i=0; i<count; i++){
            if (ports[i] == port){
                return true;
            }
        }
        return false;
    }

    public void tableAdd(int portToUse) {
        count++; 
        ports[count-1] = portToUse;               
    }
}

