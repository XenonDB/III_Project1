package net.passengerDB.databaseinterface;

public enum DataBaseOperation {
	CREATE(true),READ(false),UPDATE(true),DELETE(true);
	
	public final boolean isModifyOperation;
	
	private DataBaseOperation(boolean isModify) {
		isModifyOperation = isModify;
	}
	
}
