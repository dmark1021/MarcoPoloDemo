package edu.gmu.network;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

public class NetworkInterfaceHandler {
	
	private ArrayList<NetworkInterface> nics;
	public NetworkInterfaceHandler(){
		nics = new ArrayList<NetworkInterface>();
		MulticastSocket socket;
		try{
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();     
			     
			while (nis.hasMoreElements()) {         
				NetworkInterface ni = (NetworkInterface) nis.nextElement();

				//checks for active multicast nic
				if (
						!ni.isLoopback() && 
						!ni.isPointToPoint() && 
						!ni.isVirtual() && 

						ni.isUp() && 
						ni.supportsMulticast()) {                 
					
					//test nics for exceptions
					try{
						socket = new MulticastSocket();
						socket.setNetworkInterface(ni);
						
						//if no exception, add to the arraylist
						nics.add(ni);
					}
					catch (Exception e){
						//do nothing
					}
				}                             
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public ArrayList<NetworkInterface> getNics(){
		return nics;
	}

}
