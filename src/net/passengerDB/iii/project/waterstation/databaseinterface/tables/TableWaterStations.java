package net.passengerDB.iii.project.waterstation.databaseinterface.tables;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.passengerDB.databaseinterface.ISinglePrimaryKeyTable;
import net.passengerDB.iii.project.waterstation.databaseinterface.MSSQLConnectionUtils;
import net.passengerDB.iii.project.waterstation.databaseinterface.Utils;
import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.Location;
import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.Provider;
import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.WaterStation;
import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.WaterType;
import net.passengerDB.iii.project.waterstation.dataparser.CSVParser;
import java.nio.charset.StandardCharsets;


public final class TableWaterStations implements ISinglePrimaryKeyTable<WaterStation, Integer>{

	public static final TableWaterStations INSTANCE;
	
	private static final String[] COLUMN_NAMES = {"stationID","location","name","waterType","provider","updateDate"};
	private static String EXPORT_CSV_HEADER;
	
	static{
		INSTANCE = new TableWaterStations();
	}
	
	private TableWaterStations() {}
	
	/**
	 * 插入一筆新的加水站資料。這筆新的資料的ID可以留空，程序會自動尋找可用的ID分配給它。
	 * 可以使用WaterStation的第二個建構式來自動填入空的ID。
	 * */
	@Override
	public int insertData(WaterStation data) throws SQLException{
		
		if(data == null) throw new IllegalArgumentException("Can't handler a null data.");
		
		if(data.isQueryOnly()) throw new IllegalArgumentException("Query only data can't be used to modify table.");
		
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return 0;
		
		Connection c = oc.get();
		
		data = prepareUpdateData(data);
		
		Location dl = data.getLocation();
		WaterType dt = data.getWaterType();
		Provider dp = data.getProvider();
		
		try(PreparedStatement s = c.prepareStatement(String.format("insert into %s values(?,?,?,?,?,?)", getTableName()));){
			s.setInt(1, data.getUniquidKey().get());
			
			//location不可能為null
			s.setInt(2, dl.getUniquidKey().get());
			
			s.setString(3, data.getName());
			
			if(dt == null) {
				s.setNull(4, Types.INTEGER);
			}else {
				s.setInt(4, dt.getUniquidKey().get());
			}
			if(dp == null) {
				s.setNull(5, Types.INTEGER);
			}else {
				s.setInt(5, dp.getUniquidKey().get());
			}
			
			s.setDate(6, data.getUpdateDate());
			
			
			return s.executeUpdate();
		}
		
		
	}

	@Override
	public List<WaterStation> getAllData() throws SQLException{
		
		LinkedList<WaterStation> result = new LinkedList<WaterStation>();
		
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return result;
		
		Connection c = oc.get();
		
		StringBuilder sql = prepareQuarySQL();
		
		try(PreparedStatement s = c.prepareStatement(String.format(sql.toString(), getTableName()));){
			ResultSet rset = s.executeQuery();
			
			while(rset.next()) {
				result.add(constructResult(rset));
			}
			
			return result;
		}
		
	}

	/**
	 * 根據主索引鍵來更新資料
	 * 傳入的資料的任何一部分如果是null，且該欄位可以被設為null，那麼更新後那欄就會變成null
	 * */
	@Override
	public int updateData(WaterStation data) throws SQLException{
		
		checkingData(data);
		
		if(data.isQueryOnly()) throw new IllegalArgumentException("Query only data can't be used to modify table.");
		
		int key = data.getUniquidKey().get();
		Optional<WaterStation> oldData = getDataByPK(key);
		if(oldData.isEmpty()) return 0;
		
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return 0;
		
		Connection c = oc.get();
		
		data = prepareUpdateData(data);
		
		Location dl = data.getLocation();
		WaterType dt = data.getWaterType();
		Provider dp = data.getProvider();
		
		try(PreparedStatement s = c.prepareStatement(String.format("update %s set location = ?, name = ?, waterType = ?, provider = ?, updateDate = ? where %s = ?", getTableName(), getIdColumnName()))){
			//location不可能是null
			
			s.setInt(1, dl.getUniquidKey().get());
			
			s.setString(2, data.getName());
			
			if(dt == null) {
				s.setNull(3, Types.INTEGER);
			}else {
				s.setInt(3, dt.getUniquidKey().get());
			}
			if(dp == null) {
				s.setNull(4, Types.INTEGER);
			}else {
				s.setInt(4, dp.getUniquidKey().get());
			}
			
			s.setDate(5, data.getUpdateDate());
			
			s.setInt(6, data.getUniquidKey().get());
			
			s.executeUpdate();
		}
		
		cleanUpForeignKeyData(oldData.get());
		
		return 1;
		
	}
	
	/**
	 * 將資料中不完整的部分完善，這同時會影響到外鍵的表格中的資料。亦即可能會在對應的資料表中建立新的外鍵參考。
	 * 會以外鍵的值為主來更新，自行輸入的ID很可能會不起作用。
	 * @throws SQLException 
	 * */
	private WaterStation prepareUpdateData(WaterStation data) throws SQLException {
		Location dl = data.getLocation();
		WaterType dt = data.getWaterType();
		Provider dp = data.getProvider();
		
		if(dl != null) {
			try {
				dl = TableLocations.INSTANCE.getSpecificData(dl).get(0);
			}catch(IndexOutOfBoundsException e) {
				dl = new Location(Utils.findFirstAvailableId(TableLocations.INSTANCE), data.getLocation().getData());
				TableLocations.INSTANCE.insertData(dl);
			}
		}
		if(dt != null) {
			try {
				dt = TableWaterType.INSTANCE.getSpecificData(dt).get(0);
			}catch(IndexOutOfBoundsException e) {
				dt = new WaterType(Utils.findFirstAvailableId(TableWaterType.INSTANCE), data.getWaterType().getData());
				TableWaterType.INSTANCE.insertData(dt);
			}
		}
		if(dp != null) {
			try {
				dp = TableProviders.INSTANCE.getSpecificData(dp).get(0);
			}catch(IndexOutOfBoundsException e) {
				dp = new Provider(Utils.findFirstAvailableId(TableProviders.INSTANCE), data.getProvider().getData());
				TableProviders.INSTANCE.insertData(dp);
			}
		}
		data.setLocation(dl);
		data.setWaterType(dt);
		data.setProvider(dp);
		
		if(data.getUniquidKey().isEmpty()) {
			return new WaterStation(Utils.findFirstAvailableId(this) ,data.getLocation(), data.getName(), data.getWaterType(), data.getProvider(), data.getUpdateDate());
		}
		
		return data;
	}
	

	/**
	 * 根據主索引鍵來刪除資料
	 * */
	@Override
	public int deleteData(WaterStation data) throws SQLException{
		 
		checkingData(data);
		
		int key = data.getUniquidKey().get();
		
		Optional<WaterStation> oldData = getDataByPK(key);
		if(oldData.isEmpty()) return 0;
		
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return 0;
		
		Connection c = oc.get();
		
		
		try(PreparedStatement s = c.prepareStatement(String.format(DELETE_TEMPLATE, getTableName(), getIdColumnName()))){
			s.setInt(1, key);
			
			s.executeUpdate();
		}
		
		cleanUpForeignKeyData(oldData.get());
		
		return 1;
		
	}

	private String getIdColumnName() {
		return "stationID";
	}

	/**
	 * 嘗試將WaterStation實例中參考的外鍵，從其關聯表格中刪除。資料更動後，可能有某些外鍵不再用到，所以嘗試將這些外鍵刪除。
	 * 
	 * 應該有可以只透過SQL指令的方式來刪除，但我不熟悉而且一時想不到...
	 * */
	private void cleanUpForeignKeyData(WaterStation data) {
		
		try {
			TableLocations.INSTANCE.deleteData(data.getLocation());
			Utils.markRecordDeprecated(TableLocations.INSTANCE , data.getLocation().getUniquidKey().get());
		} catch (SQLException e) {
			//547是刪除仍然有被參考的外鍵的例外。這邊是透過檢查是不是出現這個例外來判斷有沒有刪除成功。沒刪除成功的話就不做任何事。
			if(e.getErrorCode() != 547) e.printStackTrace();
		}
		
		try {
			TableWaterType.INSTANCE.deleteData(data.getWaterType());
			Utils.markRecordDeprecated(TableWaterType.INSTANCE , data.getWaterType().getUniquidKey().get());
		} catch (SQLException e) {
			//547是刪除仍然有被參考的外鍵的例外。這邊是透過檢查是不是出現這個例外來判斷有沒有刪除成功。沒刪除成功的話就不做任何事。
			if(e.getErrorCode() != 547) e.printStackTrace();
		}
		
		try {
			TableProviders.INSTANCE.deleteData(data.getProvider());
			Utils.markRecordDeprecated(TableProviders.INSTANCE , data.getProvider().getUniquidKey().get());
		} catch (SQLException e) {
			//547是刪除仍然有被參考的外鍵的例外。這邊是透過檢查是不是出現這個例外來判斷有沒有刪除成功。沒刪除成功的話就不做任何事。
			if(e.getErrorCode() != 547) e.printStackTrace();
		}
		
	}
	
	/**
	 * 根據主索引鍵以外的資料，查詢對應的加水站資料
	 * 這不會利用到任何主鍵來查詢資料，無論是加水站資料的主鍵，或是provider、watertype、location的主鍵。
	 * 傳入的資料可以缺少一部份的資訊。
	 * 使用like %[數據]%來查詢資料。亦即只要某一筆資料的該鍵值，一部份符合條件，則該筆資料就會被查出。
	 * 例：data.setProvider(new Provider("大"))後傳入
	 * 則只要任何一筆資料，它的provider資料行內的字串，包含了"大"這個字，就會被查出。
	 * 
	 * 若無指定任何條件則會回傳空的結果。
	 * 
	 * 不確定一邊串接字串一邊設定statement的變數是否可行，所以先串完後再一口氣設定變數。
	 * */
	@Override
	public List<WaterStation> getSpecificData(WaterStation data) throws SQLException {
		
		if(data == null) throw new IllegalArgumentException("Can't handler a null data.");
		
		LinkedList<WaterStation> result = new LinkedList<WaterStation>();
		
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return result;
		
		Connection c = oc.get();
		
		HashMap<String,Integer> indexMapping = new HashMap<>();
		int index = 1;
		
		StringBuilder sqlStatement = prepareQuarySQL();
		sqlStatement.append("where ");
		
		if(data.getLocation() != null) {
			indexMapping.put(TableLocations.INSTANCE.getTableName(), index);
			index++;
			sqlStatement.append(String.format("%s.%s like ? ", TableLocations.INSTANCE.getTableName(), TableLocations.INSTANCE.getDataColumnName()));
		}
		if(data.getWaterType() != null) {
			if(index > 1) sqlStatement.append("and ");
			indexMapping.put(TableWaterType.INSTANCE.getTableName(), index);
			index++;
			sqlStatement.append(String.format("%s.%s like ? ", TableWaterType.INSTANCE.getTableName(), TableWaterType.INSTANCE.getDataColumnName()));
		}
		if(data.getProvider() != null) {
			if(index > 1) sqlStatement.append("and ");
			indexMapping.put(TableProviders.INSTANCE.getTableName(), index);
			index++;
			sqlStatement.append(String.format("%s.%s like ? ", TableProviders.INSTANCE.getTableName(), TableProviders.INSTANCE.getDataColumnName()));
		}
		
		if(index == 1) return result;
		
		try(PreparedStatement ps = c.prepareStatement(sqlStatement.toString());){
			indexMapping.forEach((tableName,i) -> {setStatementParam(ps, tableName, i, data);});
			
			ResultSet resSet = ps.executeQuery();
			
			while(resSet.next()) {
				result.add(constructResult(resSet));
			}
		}
		
		return result;
		
	}
	
	private StringBuilder prepareQuarySQL() {
		StringBuilder sqlStatement = new StringBuilder(String.format("select * from %s left outer join %s on location = %s ", getTableName(), TableLocations.INSTANCE.getTableName(), TableLocations.INSTANCE.getIdColumnName()));
		sqlStatement.append(String.format("left outer join %s on waterType = %s ", TableWaterType.INSTANCE.getTableName(), TableWaterType.INSTANCE.getIdColumnName()));
		sqlStatement.append(String.format("left outer join %s on provider = %s ", TableProviders.INSTANCE.getTableName(), TableProviders.INSTANCE.getIdColumnName()));
		return sqlStatement;
	}
	
	private void setStatementParam(PreparedStatement ps, String tableName, int index, WaterStation data) {
		try {
			if(tableName == TableLocations.INSTANCE.getTableName()) {
				ps.setString(index, "%" + data.getLocation().getData() + "%");
			}else if(tableName == TableWaterType.INSTANCE.getTableName()) {
				ps.setString(index, "%" + data.getWaterType().getData() + "%");
			}else if(tableName == TableProviders.INSTANCE.getTableName()) {
				ps.setString(index, "%" + data.getProvider().getData() + "%");
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Optional<WaterStation> getDataByPK(Integer primaryKey) throws SQLException{
		Optional<Connection> oc = MSSQLConnectionUtils.getConnection();
		if(oc.isEmpty()) return Optional.empty();
		
		Connection c = oc.get();
		
		try(PreparedStatement s = c.prepareStatement("exec getWaterStationById @id = ?")){
			s.setInt(1,primaryKey);
			
			ResultSet rset = s.executeQuery();
			
			if(!rset.next()) return Optional.empty();
			
			return Optional.of(constructResult(rset));
		}
		
	}

	private WaterStation constructResult(ResultSet rset) throws SQLException {
		
		String dataL = rset.getString(8);
		String dataT = rset.getString(10);
		String dataP = rset.getString(12);
		
		Location rl = dataL == null ? null : new Location(rset.getInt(7),dataL);
		WaterType rt = dataT == null ? null : new WaterType(rset.getInt(9),dataT);
		Provider rp = dataP == null ? null : new Provider(rset.getInt(11),dataP);
		
		return new WaterStation(rset.getInt(1),rl,rset.getString(3),rt,rp,rset.getDate(6));
	}
	
	private void checkingData(WaterStation data) {
		if(data == null) throw new IllegalArgumentException("Can't handler a null data.");
		
		if(data.getUniquidKey().isEmpty()) throw new IllegalArgumentException("Data must have primary key to be handled.");
	}
	
	@Override
	public String getTableName() {
		return "WaterStations";
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
	
	/**指定第一個參數為輸出路徑來輸出CSV檔案
	 * 路徑必須是「完整的含檔案名稱的路徑」
	 * 檔案的編碼格式為UTF-8無BOM
	 * */
	public void exportTable(String[] args, List<WaterStation> allData) {
		CSVParser parser = new CSVParser();
		
		try(FileWriter fw = new FileWriter(args[0], StandardCharsets.UTF_8)) {
			
			fw.write(getCSVHeader() + "\n");
			allData.forEach(new Consumer<WaterStation>(){

				@Override
				public void accept(WaterStation e) {
					try {
						
						fw.write(parser.exportToCSVRecord(new String[] {
								e.getUniquidKey().get().toString(), 
								e.getLocation() != null ? e.getLocation().getData() : "null",
								e.getName() != null ? e.getName() : "null",
								e.getWaterType() != null ? e.getWaterType().getData() : "null",
								e.getProvider() != null ? e.getProvider().getData() : "null",
								e.getUpdateDate() != null ? e.getUpdateDate().toString() : "null"}) + "\n");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
			});
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	@Override
	public void exportTable(String[] args) {
		try {
			exportTable(args, getAllData());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static String getCSVHeader() {
		
		if(EXPORT_CSV_HEADER != null) return EXPORT_CSV_HEADER;
		
		StringBuilder tmp = new StringBuilder(COLUMN_NAMES[0]);
		for(int i = 1 ; i < COLUMN_NAMES.length ; i++) {
			tmp.append(',');
			tmp.append(COLUMN_NAMES[i]);
		}
		EXPORT_CSV_HEADER = tmp.toString();
		
		return EXPORT_CSV_HEADER;
	}
	
	
}
