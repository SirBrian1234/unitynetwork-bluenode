package kostiskag.unitynetwork.bluenode.Routing;

import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

/**
 * An object of this class takes a packet from a queue and forwards it to 
 * a red node's queue or a blue node's queue. When a packet has a not known destination
 * the FlyRegister is called.
 * 
 * @author kostis
 */
public class Router extends Thread {

    public final String pre = "^Router ";
    
    public Router() {
    	
    }

    @Override
    public void run() {
        App.bn.ConsolePrint(pre + "started routing at thread " + Thread.currentThread().getName());

        while (true) {
            byte[] data;
            try {
                data = App.bn.manager.poll();
            } catch (java.lang.NullPointerException ex1) {
                continue;
            } catch (java.util.NoSuchElementException ex) {
                continue;
            }
            
            /* from this point on a packet needs to be routed... now lets check 
             * if destination is registered check vaddress table and sent to 
             * specific udp vaddr - addr:udp then when you get the stuff send it
             */

            String version = IpPacket.getVersion(data);
            String sourcevaddress;
            String destvaddress;

            if (version.equals("45") || version.equals("1") || version.equals("2")) {
                if (version.equals("45")) {
                	sourcevaddress = IpPacket.getSourceAddress(data).getHostAddress();
                	destvaddress = IpPacket.getDestAddress(data).getHostAddress();
                    App.bn.TrafficPrint(pre + "IP " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", 3, 0);
                } else {
                    destvaddress = IpPacket.getUDestAddress(data).getHostAddress();
                    sourcevaddress = IpPacket.getUSourceAddress(data).getHostAddress();
                    App.bn.TrafficPrint(pre + version + " " + sourcevaddress + " -> " + destvaddress + " " + data.length + "B", 3, 0);
                }

                if (App.bn.localRedNodesTable.checkOnlineByVaddress(destvaddress)) {
                    //load the packet data to local red node's queue
                    App.bn.localRedNodesTable.getRedNodeInstanceByAddr(destvaddress).getQueueMan().offer(data);
                    App.bn.TrafficPrint(pre+"LOCAL DESTINATION", 3, 0);
                } else if (App.bn.joined) {
                    if (App.bn.blueNodesTable.checkRemoteRedNodeByVaddress(destvaddress)) {
                    	//load the packet to remote blue node's queue
                        BlueNodeInstance bn;
						try {
							bn = App.bn.blueNodesTable.getBlueNodeInstanceByRRNVaddr(destvaddress);
							bn.getQueueMan().offer(data);
	                        App.bn.TrafficPrint(pre +"REMOTE DESTINATION -> " + bn.getName(), 3, 1);
						} catch (Exception e) {
							e.printStackTrace();
						}                        
                    } else {
                    	//lookup via tracker fro a bluenode with this rrd
                        App.bn.TrafficPrint(pre +"NOT KNOWN RRN WITH "+destvaddress+" SEEKING TARGET BN", 3, 1);
                        App.bn.flyreg.seekDest(sourcevaddress, destvaddress);
                    }
                } else {
                    App.bn.TrafficPrint(pre +"NOT IN THIS BN " + destvaddress, 3, 1);
                }
            } else {
                System.err.println(pre+"wrong header packet detected in router " + Thread.currentThread());
            }
        }
    }
}