package net.passengerDB.iii.project.waterstation.databaseinterface.tables;

import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.WaterType;


public final class TableWaterType extends SimpleIdToStrTable<WaterType> {

	public static final TableWaterType INSTANCE;
	
	static {
		INSTANCE = new TableWaterType();
	}
	
	private TableWaterType() {}

	@Override
	public String getTableName() {
		return "WaterType";
	}

	@Override
	public String getIdColumnName() {
		return "typeID";
	}

	@Override
	public String getDataColumnName() {
		return "name";
	}

	@Override
	public WaterType newCorrespondingObject(int id, String data) {
		return new WaterType(id, data);
	}

}
