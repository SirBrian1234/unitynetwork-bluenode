package kostiskag.unitynetwork.bluenode.Routing;

import kostiskag.unitynetwork.bluenode.BlueNode.*;

/*
 *
 * @author kostis
 *
 * This class takes a packet and forwards it to a users queue or a bluenodes
 * queue to be raedy to be sent
 */
public class PacketToHandle extends Thread {

    public String pre = "^ROUTE ";
    public String destvaddress;
    public byte[] data;
    public static Boolean didTrigger = false;
    String sentence;
    private String sourcevaddress;
    String version = null;

    public PacketToHandle() {
    }

    @Override
    public void run() {
        lvl3BlueNode.ConsolePrint(pre + "started routing at thread " + Thread.currentThread().getName());

        while (true) {
            /*
             * ok you got something... now lets check if destination is
             * registered check vaddress table and sent to specific udp vaddr -
             * addr:udp then when you get the stuff send it
             */

            try {
                data = lvl3BlueNode.manager.poll();
            } catch (java.lang.NullPointerException ex1) {
                continue;
            } catch (java.util.NoSuchElementException ex) {
                continue;
            }

            version = IpPacket.getVersion(data);

            if (version.equals("45") || version.equals("1") || version.equals("2")) {
                if (version.equals("45")) {
                    this.destvaddress = IpPacket.getDestAddress(data).getHostAddress();
                    this.sourcevaddress = IpPacket.getSourceAddress(data).getHostAddress();
                    lvl3BlueNode.TrafficPrint(pre + "IP " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", 3, 0);
                } else {
                    this.destvaddress = IpPacket.getUDestAddress(data).getHostAddress();
                    this.sourcevaddress = IpPacket.getUSourceAddress(data).getHostAddress();
                    lvl3BlueNode.TrafficPrint(pre + version + " " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", 3, 0);
                }

                if (lvl3BlueNode.localRedNodesTable.checkOnline(destvaddress) == true) {
                    //load the packet data to target users lifo
                    lvl3BlueNode.localRedNodesTable.getRedNodeInstanceByAddr(destvaddress).getQueueMan().offer(data);
                    lvl3BlueNode.TrafficPrint(pre + "LOCAL DESTINATION", 3, 0);
                } else if (lvl3BlueNode.joined) {
                    if (lvl3BlueNode.remoteRedNodesTable.checkAssociated(destvaddress) == true) {
                        String hostname = lvl3BlueNode.remoteRedNodesTable.getRedRemoteAddress(destvaddress).getBlueNodeHostname();
                        lvl3BlueNode.BlueNodesTable.getBlueNodeInstanceByHn(hostname).getQueueMan().offer(data);
                        lvl3BlueNode.TrafficPrint(pre + "REMOTE DESTINATION -> " + hostname, 3, 1);
                    } else {
                        lvl3BlueNode.TrafficPrint(pre + "NOT KNOWN BN WITH " + destvaddress, 3, 1);
                        lvl3BlueNode.flyreg.seekDest(sourcevaddress, destvaddress);
                    }
                } else {
                    lvl3BlueNode.TrafficPrint(pre + "NOT IN THIS BN " + destvaddress, 3, 1);
                }
            } else {
                System.err.println("wrong header packet detected in router " + Thread.currentThread());
            }
        }
    }
}