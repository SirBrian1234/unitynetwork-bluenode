package org.kostiskag.unitynetwork.bluenode.service;

import java.util.Stack;
import org.kostiskag.unitynetwork.bluenode.App;

/**
 *
 * @author Konstantinos Kagiampakis
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
         App.bn.ConsolePrint(pre + "USING A NEW PORT " + portToUse);
         tableAdd(portToUse);
         return portToUse;
    }

    public int requestNewPort(int oldPort) {
        int portToUse;
        
            do {
                portToUse = startport + (int)(Math.random() * ((endport - startport) + 1));
            } while (checkTable(portToUse) && portToUse != oldPort);            
            App.bn.ConsolePrint(pre + "USING A NEW PORT " + portToUse);
            tableAdd(portToUse);
            return portToUse;
    }
    
    public void releasePort(int port) {
        for (int i=0; i<count; i++){
            if (ports[i] == port){                
                ports[i]= ports[count-1];
                ports[count-1]=0;
                count--;
                App.bn.ConsolePrint(pre + "PORT " + port +" RELEASED");
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

