package edu.gmu.network;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Observable;

public class UdpReceiver extends Observable implements Runnable{

	private MulticastSocket socket;
	private InetAddress group;
	private DatagramPacket packet;

	public UdpReceiver (NetworkInterface ni, int port, String groupName) throws IOException{

		//setting up socket
		socket =  new MulticastSocket(port);
		group = InetAddress.getByName(groupName);

		//setup the socket to join the specified network interface and multicast socket group
		socket.setNetworkInterface(ni);
		socket.joinGroup(group);

		//sets the packet byte buffer equal to the MTU of the network
		byte[] buf = new byte[ni.getMTU()];
		packet = new DatagramPacket(buf, buf.length);
	}

	public void run() {
		
		//main loop
		while (!socket.isClosed()){
			try {
				
				//try to receive a packet on the network interface
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			} //waiting to receive packet

			//setChanged and notify any observers with the packet data
			this.setChanged();
			this.notifyObservers(packet);
		}


	}

	public void close(){
		socket.close();
	}

}
