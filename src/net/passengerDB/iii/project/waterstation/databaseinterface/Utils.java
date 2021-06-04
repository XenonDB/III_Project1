package net.passengerDB.iii.project.waterstation.databaseinterface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import net.passengerDB.databaseinterface.ISinglePrimaryKeyTable;
import net.passengerDB.databaseinterface.IUniquidKeyData;

public class Utils {

	
	/**
	 * 這些方法其實不太嚴謹
	 * */
	public static int findFirstAvailableId(ISinglePrimaryKeyTable<? extends IUniquidKeyData<Integer>,Integer> table) throws SQLException {
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) throw new SQLException("Unable to connect with SQL server for finding available id.");
		
		Connection c = oc.get();
		
		PreparedStatement ps = null;
		try {
			ps = c.prepareStatement("select top(1) record from DeprecatedRecord where tableName = ?");
			ps.setString(1, table.getTableName());
			
			ResultSet rset = ps.executeQuery();
			if(rset.next()) {
				
				int id = rset.getInt(1);
				
				ps.close();
				ps = c.prepareStatement("delete from DeprecatedRecord where tableName = ?, record = ?");
				ps.setString(1, table.getTableName());
				ps.setInt(2, id);
				
				assert ps.executeUpdate() == 1;
				
				return id;
			}else {
				return table.getTotalDataAmount() + 1;
			}
		}finally {
			if(ps != null) ps.close();
		}
	}
	
	public static int markRecordDeprecated(ISinglePrimaryKeyTable<?,?> table, int id) throws SQLException {
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) throw new SQLException("Unable to connect with SQL server during marking deprecated.");
		
		Connection c = oc.get();
		
		try(PreparedStatement ps = c.prepareStatement("insert into DeprecatedRecord values(?,?)");){
			ps.setString(1, table.getTableName());
			ps.setInt(2, id);
			
			return ps.executeUpdate();
		}
	}
	
}
