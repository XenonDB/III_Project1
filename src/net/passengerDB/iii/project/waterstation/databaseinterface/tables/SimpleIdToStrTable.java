package net.passengerDB.iii.project.waterstation.databaseinterface.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import net.passengerDB.databaseinterface.DataBaseOperation;
import net.passengerDB.databaseinterface.ISinglePrimaryKeyTable;
import net.passengerDB.iii.project.waterstation.databaseinterface.MSSQLConnectionUtils;
import net.passengerDB.iii.project.waterstation.databaseinterface.Utils;
import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.SimpleIdToStrRecord;

public abstract class SimpleIdToStrTable<T extends SimpleIdToStrRecord> implements ISinglePrimaryKeyTable<T, Integer> {
	
	/**
	 * 根據傳入資料(data)的主索引鍵來操作資料
	 * */
	private int modifyData(T data, DataBaseOperation type) throws SQLException {
		
		if(data == null) throw new IllegalArgumentException("Can't handler a null data.");
		
		if(data.getUniquidKey().isEmpty()) {
			if(type != DataBaseOperation.CREATE) throw new IllegalArgumentException("Data must have primary key to be handled.");
			
			data = newCorrespondingObject(Utils.findFirstAvailableId(this), data.getData());
		}
		
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return 0;
		
		Connection c = oc.get();
		
		PreparedStatement s = null;
		try {
			switch(type) {
			case DELETE:
				s = c.prepareStatement(String.format(DELETE_TEMPLATE, getTableName(), getIdColumnName()));
				s.setInt(1, data.getUniquidKey().get());
				break;
			case CREATE:
				s = c.prepareStatement(String.format("insert into %s values (?,?)", getTableName()));
				s.setInt(1, data.getUniquidKey().get());
				s.setString(2, data.getData());
				break;
			case UPDATE:
				s = c.prepareStatement(String.format("update %s set %s = ? where %s = ?", getTableName(), getDataColumnName(), getIdColumnName()));
				s.setInt(2, data.getUniquidKey().get());
				s.setString(1, data.getData());
				break;
			default:
				return 0;
			}
		
			return s.executeUpdate();
		}finally {
			if(s != null) s.close();
		}
		
	}
	
	/**
	 * 根據傳入資料(data)的主索引鍵來操作資料
	 * */
	@Override
	public int insertData(T data) throws SQLException {
		return modifyData(data, DataBaseOperation.CREATE);
	}

	@Override
	public List<T> getAllData() throws SQLException {
		
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return new LinkedList<>();
		
		Connection c = oc.get();
		
		try(Statement s = c.createStatement()){
			ResultSet rset = s.executeQuery(String.format("select * from %s", getTableName()));
			LinkedList<T> result = new LinkedList<>();
			
			while(rset.next()) {
				result.add(newCorrespondingObject(rset.getInt(1),rset.getString(2)));
			}
			
			return result;
		}
		
	}

	@Override
	public int getTotalDataAmount() throws SQLException {
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return 0;
		
		Connection c = oc.get();
		
		try(Statement s = c.createStatement()){
			ResultSet rset = s.executeQuery(String.format("select count(*) from %s", getTableName()));
			rset.next();
			
			return rset.getInt(1);
		}
	}
	
	/**
	 * 根據data的內容(字串)反向查詢PK
	 * 若要根據PK查詢，請使用getDataByPK
	 * */
	@Override
	public List<T> getSpecificData(T data) throws SQLException {
		if(data == null) throw new IllegalArgumentException("Can't find record with null data.");
		
		//因為物件本身的setData設計，不會出現data.getData()得到null的情況
		
		List<T> result = getSpecificData(data, false);
		
		//儘管不是用PK找，但在資料表設計上，這個欄位應該也是跟PK一樣有唯一的一欄
		assert result.size() == 1;
		
		return result;
		
	}
	
	private List<T> getSpecificData(T data, boolean usePK) throws SQLException {
		
		LinkedList<T> result = new LinkedList<>();
		
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return result;
		
		Connection c = oc.get();
		
		String target = usePK ? getIdColumnName() : getDataColumnName();
		
		try(PreparedStatement s = c.prepareStatement(String.format("select * from %s where %s = ?", getTableName(), target))){
			if(usePK) {
				s.setInt(1,data.getUniquidKey().get());
			} else {
				s.setString(1,data.getData());
			}
			
			ResultSet rset = s.executeQuery();
			while(rset.next()) {
				result.add(newCorrespondingObject(rset.getInt(1), rset.getString(2)));
			}
			
			return result;
		}
		
		
	}
	
	/**
	 * 根據傳入資料(data)的主索引鍵來操作資料
	 * */
	@Override
	public int updateData(T data) throws SQLException {
		return modifyData(data, DataBaseOperation.UPDATE);
	}

	/**
	 * 根據傳入資料(data)的主索引鍵來操作資料
	 * */
	@Override
	public int deleteData(T data) throws SQLException {
		return modifyData(data, DataBaseOperation.DELETE);
	}

	@Override
	public Optional<T> getDataByPK(Integer primaryKey) throws SQLException {
		
		List<T> result = getSpecificData(newCorrespondingObject(primaryKey, ""), true);
		
		if(result.size() == 0) return Optional.empty();
		
		//理論上result的size應該要==1，因為透過PK來取只會取到唯一的記錄。
		assert result.size() == 1;
		
		return Optional.of(result.get(0));
		
	}
	
	public abstract String getIdColumnName();
	
	public abstract String getDataColumnName();
	
	public abstract T newCorrespondingObject(int id, String data);

}
