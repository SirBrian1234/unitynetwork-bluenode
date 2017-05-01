package kostiskag.unitynetwork.bluenode.blueThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.gui.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.packets.IPv4Packet;
import kostiskag.unitynetwork.bluenode.Routing.packets.UnityPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

/**
 * 
 * @author kostis
 */
public class BlueSendClient extends Thread {

	private final String pre;
	private final BlueNodeInstance blueNode;
	private final InetAddress blueNodePhAddress;
	private final int downport;
	DatagramSocket clientSocket;
	private Boolean didTrigger = false;
	private AtomicBoolean kill = new AtomicBoolean(false);

	/**
	 * First the class must find all the valuable information to open the socket
	 * we do this on the constructor so that the running time will be charged on
	 * the AuthService Thread remember = BlueDownServiceClient is a client
	 * thread to BlueUpServiceserver, therefore he is the one to SEND.
	 */
	public BlueSendClient(BlueNodeInstance blueNode, int downport) {
		this.blueNode = blueNode;
		this.pre = "^BlueDownServiceClient " + blueNode.getName() + " ";
		this.blueNodePhAddress = blueNode.getPhaddress();		
		this.downport = downport;
	}

	public int getDownport() {
		return downport;
	}

	public BlueNodeInstance getBlueNode() {
		return blueNode;
	}

	public boolean isKilled() {
		return kill.get();
	}

	@Override
	public void run() {
		App.bn.ConsolePrint(pre + "STARTED AT " + Thread.currentThread().getName() + " ON PORT " + downport);

		clientSocket = null;
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException ex) {
			ex.printStackTrace();
			blueNode.getQueueMan().clear();
			App.bn.ConsolePrint(pre + "FORCE ENDED");
			return;
		}

		while (!kill.get()) {

			byte[] data = null;
			try {
				data = blueNode.getQueueMan().poll();
			} catch (java.lang.NullPointerException ex1) {
				continue;
			} catch (java.util.NoSuchElementException ex) {
				continue;
			}
			DatagramPacket sendUDPPacket = new DatagramPacket(data, data.length, blueNodePhAddress, downport);
			try {
				clientSocket.send(sendUDPPacket);
				if (UnityPacket.isUnity(data)) {
					if (UnityPacket.isKeepAlive(data)) {
						// keep alive
						App.bn.TrafficPrint(pre +"KEEP ALIVE SENT", 0, 1);
					} else if (UnityPacket.isUping(data)) {
                        //blue node uping!
                        App.bn.TrafficPrint(pre + "UPING SENT", 1, 1);
                    } else if (UnityPacket.isDping(data)) {
						// blue node dping!
						App.bn.TrafficPrint(pre + "DPING SENT", 1, 1);
					} else if (UnityPacket.isAck(data)) {
						try {
							App.bn.TrafficPrint(pre + "ACK-> "+UnityPacket.getDestAddress(data)+" SENT", 2, 1);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (UnityPacket.isMessage(data)) {
						App.bn.TrafficPrint(pre + "MESSAGE SENT", 3, 1);
					}
				}
				if (App.bn.gui && didTrigger == false) {
					MainWindow.jCheckBox6.setSelected(true);
					didTrigger = true;
				}
			} catch (java.net.SocketException ex1) {
				App.bn.ConsolePrint(pre + "SOCKET ERROR");
			} catch (IOException ex2) {
				App.bn.ConsolePrint(pre + "IO ERROR");
			}
		}

		blueNode.getQueueMan().clear();
		App.bn.ConsolePrint(pre + "ENDED");
	}

	public void kill() {
		kill.set(true);
		clientSocket.close();		
	}
}
