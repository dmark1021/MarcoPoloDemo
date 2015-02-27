package edu.gmu.network;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import edu.gmu.constants.EUdpGroupName;
import edu.gmu.constants.EUdpPortNum;

public class UdpCommPackage extends Observable implements Observer{
	final private UdpBroadcaster broadcaster;
	final private UdpReceiver receiver;
	final private NetworkInterface ni;

	public UdpCommPackage () throws Exception{
		/*this sets up UdpReceivers and UdpBroadcasters 
		 */

		//set properties
		System.setProperty("java.net.preferIPv4Stack", "true");

		List<NetworkInterface> nics = new NetworkInterfaceHandler().getNics();
		if(nics.size() <= 0){
			throw new IOException("No multicast network interfaces are available.");
		}

		//gets the first network interface, though any should work
		ni = nics.get(0);

		//sets up the receiver
		new Thread(
				receiver = new UdpReceiver(
						ni,
						EUdpPortNum.COMM.getPortNum(),
						EUdpGroupName.COMM_GROUP.getGroupName())
				).start();
		receiver.addObserver(this);

		//sets up the broadcaster
		new Thread (
				broadcaster = new UdpBroadcaster(
						ni,
						EUdpPortNum.COMM.getPortNum(),
						EUdpGroupName.COMM_GROUP.getGroupName())
				).start();

		nics=null;

	}

	public void update(Observable receiver, Object datagramPacket) {
		//handles updates from the receivers

		//pass the packet to observers
		setChanged();
		notifyObservers(datagramPacket);

	}

	public void sendPayload(byte[] payload) throws Exception{
		//sends a broadcast to listening receivers

		//broadcasts information to the receiver
		broadcaster.sendPacket(payload, EUdpPortNum.COMM.getPortNum());
	}
	
	public NetworkInterface getNetworkInterface(){
		return ni;
	}

}
