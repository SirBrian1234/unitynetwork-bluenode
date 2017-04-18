package kostiskag.unitynetwork.bluenode.Routing;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

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
        App.bn.ConsolePrint(pre + "started routing at thread " + Thread.currentThread().getName());

        while (true) {
            /*
             * ok you got something... now lets check if destination is
             * registered check vaddress table and sent to specific udp vaddr -
             * addr:udp then when you get the stuff send it
             */

            try {
                data = App.bn.manager.poll();
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
                    App.bn.TrafficPrint(pre + "IP " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", 3, 0);
                } else {
                    this.destvaddress = IpPacket.getUDestAddress(data).getHostAddress();
                    this.sourcevaddress = IpPacket.getUSourceAddress(data).getHostAddress();
                    App.bn.TrafficPrint(pre + version + " " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", 3, 0);
                }

                if (App.bn.blueNodesTable.checkRemoteRedNodeByVaddress(destvaddress)) {
                    //load the packet data to target users lifo
                    App.bn.localRedNodesTable.getRedNodeInstanceByAddr(destvaddress).getQueueMan().offer(data);
                    App.bn.TrafficPrint(pre + "LOCAL DESTINATION", 3, 0);
                } else if (App.bn.joined) {
                    if (App.bn.blueNodesTable.checkRemoteRedNodeByVaddress(destvaddress)) {
                        BlueNodeInstance bn;
						try {
							bn = App.bn.blueNodesTable.getBlueNodeInstanceByRRNVaddr(destvaddress);
							bn.getQueueMan().offer(data);
	                        App.bn.TrafficPrint(pre + "REMOTE DESTINATION -> " + bn.getName(), 3, 1);
						} catch (Exception e) {
							e.printStackTrace();
						}                        
                    } else {
                        App.bn.TrafficPrint(pre + "NOT KNOWN BN WITH " + destvaddress, 3, 1);
                        App.bn.flyreg.seekDest(sourcevaddress, destvaddress);
                    }
                } else {
                    App.bn.TrafficPrint(pre + "NOT IN THIS BN " + destvaddress, 3, 1);
                }
            } else {
                System.err.println("wrong header packet detected in router " + Thread.currentThread());
            }
        }
    }
}