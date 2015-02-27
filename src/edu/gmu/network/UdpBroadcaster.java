package edu.gmu.network;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class UdpBroadcaster implements Runnable{

	private MulticastSocket socket;
	private InetAddress group;
	

	public UdpBroadcaster(NetworkInterface ni, int port, String groupName) throws IOException{

		//setting up socket
		
		socket =  new MulticastSocket(port);
		group = InetAddress.getByName(groupName);
		
		//join the group on the network interface
		socket.setNetworkInterface(ni);
		socket.joinGroup(group);
	}

	public void run() {
		
	}

	public void sendPacket(byte[] data, int dstPort) throws IOException{
		//creating and sending packet
		if (!socket.isClosed()){
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, group, dstPort);
			socket.send(sendPacket);
		}
		else{
			System.out.println("The socket is closed.");
		}
	}
	public void close(){
		socket.close();
	}


}
