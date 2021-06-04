package net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj;

import java.util.Optional;

import net.passengerDB.databaseinterface.IUniquidKeyData;

public class SimpleIdToStrRecord implements IUniquidKeyData<Integer> {

	private final Optional<Integer> id;
	private String data;
	
	public SimpleIdToStrRecord(Integer id, String data) {
		this.setData(data);
		this.id = Optional.ofNullable(id);
	}
	
	public SimpleIdToStrRecord(String data) {
		this(null, data);
	}
	
	@Override
	public Optional<Integer> getUniquidKey() {
		return id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		if(data == null) throw new IllegalArgumentException("Can't pass null data into a record");
		this.data = data;
	}
}
