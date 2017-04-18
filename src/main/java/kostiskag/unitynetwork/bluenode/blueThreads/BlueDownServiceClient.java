package kostiskag.unitynetwork.bluenode.blueThreads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import kostiskag.unitynetwork.bluenode.App;
import kostiskag.unitynetwork.bluenode.GUI.MainWindow;
import kostiskag.unitynetwork.bluenode.Routing.IpPacket;
import kostiskag.unitynetwork.bluenode.RunData.instances.BlueNodeInstance;

/**
 * 
 * @author kostis
 */
public class BlueDownServiceClient extends Thread {

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
	public BlueDownServiceClient(BlueNodeInstance blueNode, int downport) {
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
				String version = IpPacket.getVersion(data);
				if (version.equals("0")) {
					byte[] payload = IpPacket.getPayloadU(data);
					String receivedMessage = new String(payload);
					String args[] = receivedMessage.split("\\s+");
					if (args.length > 1) {
						if (args[0].equals("00000")) {
							// keep alive
							App.bn.TrafficPrint(pre + version + " [KEEP ALIVE]", 0, 1);
						} else if (args[0].equals("00002")) {
							// blue node uping!
							blueNode.setUping(true);
							App.bn.TrafficPrint(pre + "UPING LEAVES", 1, 1);
						} else if (args[0].equals("00003")) {
							// blue node dping!
							App.bn.dping = true;
							App.bn.TrafficPrint(pre + "DPING LEAVES", 1, 1);
						}
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
