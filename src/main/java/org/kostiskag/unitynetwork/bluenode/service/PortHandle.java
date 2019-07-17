package org.kostiskag.unitynetwork.bluenode.service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.kostiskag.unitynetwork.bluenode.App;
import org.kostiskag.unitynetwork.bluenode.AppLogger;

/**
 *
 * @author Konstantinos Kagiampakis
 */
public class PortHandle {

    public final String pre = "^PORTHANDLE ";
    public final int startport;
    public final int endport;

    public Deque<Integer> udpAvaillableSourcePorts;
    public Deque<Integer> udpBurnedSourcePorts;

    public PortHandle(int startportInclusive,int endportInclusive) {
        this.startport = startportInclusive;
        this.endport = endportInclusive;

        List<Integer> range = IntStream.range(startportInclusive,endportInclusive+1).boxed().collect(Collectors.toList());
        Collections.shuffle(range);
        udpAvaillableSourcePorts = new ArrayDeque<>(range);
        udpBurnedSourcePorts = new ArrayDeque<>();
    }

    public int requestPort() {
        var portToUse = udpAvaillableSourcePorts.pop();
        udpBurnedSourcePorts.push(portToUse);
        return portToUse;
    }

    public int requestPort(int oldPort) {
        int portToUse = this.requestPort();
        releasePort(oldPort);
        return portToUse;
    }
    
    public void releasePort(int port) {
        udpBurnedSourcePorts.remove(port);
        udpAvaillableSourcePorts.push(port);
    }
    
    public boolean checkIfAvailablePort(int port){
        return udpAvaillableSourcePorts.contains(port);
    }

    public int getSizeOfAvaillablePorts() {
        return udpAvaillableSourcePorts.size();
    }

    public int getStartport() {
        return startport;
    }

    public int getEndport() {
        return endport;
    }

}

