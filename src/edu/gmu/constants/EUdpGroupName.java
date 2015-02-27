package edu.gmu.constants;

public enum EUdpGroupName {
	COMM_GROUP("224.0.0.1");

	private String groupName;
	EUdpGroupName(String groupName){
		this.groupName = groupName;
	}
	public String getGroupName(){
		return groupName;
	}
}
