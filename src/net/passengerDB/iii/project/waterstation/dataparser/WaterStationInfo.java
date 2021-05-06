package net.passengerDB.iii.project.waterstation.dataparser;

import java.util.HashMap;

public class WaterStationInfo {

	public final String address;
	public final String name;
	
	private HashMap<String,String> additionalData;
	
	public WaterStationInfo(String add, String n) {
		this.address = add;
		this.name = n;
	}
	
}
