package net.passengerDB.iii.project.waterstation.databaseinterface.tables;

import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.Location;

public final class TableLocations extends SimpleIdToStrTable<Location> {

	public static final TableLocations INSTANCE;
	
	static {
		INSTANCE = new TableLocations();
	}
	
	private TableLocations() {}

	@Override
	public String getTableName() {
		return "Locations";
	}

	@Override
	public String getIdColumnName() {
		return "locationID";
	}

	@Override
	public String getDataColumnName() {
		return "name";
	}

	@Override
	public Location newCorrespondingObject(int id, String data) {
		return new Location(id, data);
	}

}
