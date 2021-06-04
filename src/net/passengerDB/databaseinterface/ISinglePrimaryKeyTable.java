package net.passengerDB.databaseinterface;

import java.sql.SQLException;
import java.util.Optional;

public interface ISinglePrimaryKeyTable<T extends IUniquidKeyData<U>,U> extends ITable<T> {

	public static final String DELETE_TEMPLATE = "delete from %s where %s = ?";
	
	public Optional<T> getDataByPK(U primaryKey) throws SQLException;
	
}
