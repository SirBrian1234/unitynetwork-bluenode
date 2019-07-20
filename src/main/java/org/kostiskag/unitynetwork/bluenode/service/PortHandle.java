package org.kostiskag.unitynetwork.bluenode.service;

import org.kostiskag.unitynetwork.common.calculated.NumericConstraints;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 *
 * @author Konstantinos Kagiampakis
 */
public final class PortHandle {

    private static final String pre = "^PORTHANDLE ";
    private static PortHandle PORT_HANDLE;

    private final Deque<Integer> udpAvaillableSourcePorts;
    private final Deque<Integer> udpBurnedSourcePorts;

    public static PortHandle newInstance(int startportInclusive,int endportInclusive) {
        if (PORT_HANDLE == null) {
            PORT_HANDLE = new PortHandle(startportInclusive, endportInclusive);
        }
        return PORT_HANDLE;
    }

    public static PortHandle getInstance() {
        return PORT_HANDLE;
    }

    private PortHandle(int startportInclusive,int endportInclusive) {
        if (!(startportInclusive > 0 && endportInclusive <= NumericConstraints.MAX_ALLOWED_PORT_NUM.size()
                && startportInclusive <= endportInclusive)) {
           throw new IllegalArgumentException(pre+"wrong port range was given");
        }

        List<Integer> range = IntStream.range(startportInclusive,endportInclusive+1).boxed().collect(Collectors.toList());
        Collections.shuffle(range);
        udpAvaillableSourcePorts = new ArrayDeque<>(range);
        udpBurnedSourcePorts = new ArrayDeque<>();
    }

    public synchronized int requestPort() {
        var portToUse = udpAvaillableSourcePorts.pop();
        udpBurnedSourcePorts.push(portToUse);
        return portToUse;
    }

    public synchronized int requestPort(int oldPort) {
        int portToUse = this.requestPort();
        releasePort(oldPort);
        return portToUse;
    }
    
    public synchronized void releasePort(int port) {
        if (udpBurnedSourcePorts.contains(port)) {
            udpBurnedSourcePorts.remove(port);
            udpAvaillableSourcePorts.push(port);
        }
    }
    
    public synchronized boolean checkIfAvailablePort(int port) {
        return udpAvaillableSourcePorts.contains(port);
    }

    public synchronized int getSizeOfAvaillablePorts() {
        return udpAvaillableSourcePorts.size();
    }

}

