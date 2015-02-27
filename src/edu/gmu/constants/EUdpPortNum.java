package edu.gmu.constants;

public enum EUdpPortNum {
	COMM(7000);
	
	private int portNum;
	EUdpPortNum(int portNum){
		this.portNum = portNum;
	}
	public int getPortNum(){
		return portNum;
	}
}
