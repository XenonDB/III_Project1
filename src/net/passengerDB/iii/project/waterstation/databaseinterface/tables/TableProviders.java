package net.passengerDB.iii.project.waterstation.databaseinterface.tables;

import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.Provider;


public final class TableProviders extends SimpleIdToStrTable<Provider> {

	public static final TableProviders INSTANCE;
	
	static {
		INSTANCE = new TableProviders();
	}
	
	private TableProviders() {}

	@Override
	public String getTableName() {
		return "Providers";
	}

	@Override
	public String getIdColumnName() {
		return "providerID";
	}

	@Override
	public String getDataColumnName() {
		return "name";
	}

	@Override
	public Provider newCorrespondingObject(int id, String data) {
		return new Provider(id, data);
	}

}
