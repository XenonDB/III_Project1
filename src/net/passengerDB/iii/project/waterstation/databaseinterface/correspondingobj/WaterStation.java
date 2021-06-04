package net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj;

import java.sql.Date;
import java.util.Optional;

import net.passengerDB.databaseinterface.IUniquidKeyData;

public class WaterStation implements IUniquidKeyData<Integer> {
	
	private final Optional<Integer> id;
	private Location location;
	private Provider provider;
	private WaterType type;
	
	private String name;
	private Date updateDate;
	
	private boolean queryOnly = false;
	
	public WaterStation(Integer id, Location loc, String name, WaterType type, Provider prov, Date date) {
		this.setLocation(loc);
		this.id = Optional.ofNullable(id);
		this.setProvider(prov);
		this.setWaterType(type);
		this.setName(name);
		this.setUpdateDate(date);
	}
	
	public WaterStation(Location loc, String name, WaterType type, Provider prov, Date date) {
		this(null, loc, name, type, prov, date);
	}
	
	/**
	 * 建構一個只用於查詢的資料實例。該實例不可用於修改資料庫
	 * */
	public WaterStation(Integer id) {
		setQueryOnly();
		this.id = Optional.ofNullable(id);
	}
	
	@Override
	public Optional<Integer> getUniquidKey() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Location getLocation() {
		return location;
	}

	/**
	 * 因為資料表該欄有約束，所以不可設為null
	 * */
	public void setLocation(Location location) {
		if(!isQueryOnly() && location == null) throw new IllegalArgumentException("Can't set null location for a water station");
		this.location = location;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public WaterType getWaterType() {
		return type;
	}

	public void setWaterType(WaterType type) {
		this.type = type;
	}

	public boolean isQueryOnly() {
		return queryOnly;
	}

	public void setQueryOnly() {
		this.queryOnly = true;
	}

}
