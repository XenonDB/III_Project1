package net.passengerDB.databaseinterface;

import java.sql.SQLException;
import java.util.List;

public interface ITable<T> {
	
	/**C:創建資料，使用insert into將data插入至table內*/
	public int insertData(T data) throws SQLException;
	
	/**R:讀取資料，請注意這是一次讀取全部資料*/
	public List<T> getAllData() throws SQLException;
	
	/**R:根據傳入資料的特定數值，取得資料庫中對應的完整資料*/
	public List<T> getSpecificData(T data) throws SQLException;
	
	public int getTotalDataAmount() throws SQLException;
	
	/**U:更新資料。使用update指令更新資料。符合傳入的data的特定特徵的資料才會被更新*/
	public int updateData(T data) throws SQLException;
	
	/**D:刪除資料。使用delete指令刪除資料。符合傳入的data的特定特徵的資料才會被刪除*/
	public int deleteData(T data) throws SQLException;
	
	public String getTableName();
	
	default public void exportTable(String[] args) {
		throw new UnsupportedOperationException();
	}
	
}
