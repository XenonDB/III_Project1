package net.passengerDB.iii.project.waterstation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.passengerDB.iii.project.waterstation.databaseinterface.MSSQLConnectionUtils;
import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.Location;
import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.Provider;
import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.WaterStation;
import net.passengerDB.iii.project.waterstation.databaseinterface.correspondingobj.WaterType;
import net.passengerDB.iii.project.waterstation.databaseinterface.tables.TableWaterStations;
import net.passengerDB.iii.project.waterstation.databaseinterface.tables.TableWaterType;
import net.passengerDB.iii.project.waterstation.dataparser.CSVParser;

public class Main {
	
	static String URL1 = "https://www.k12ea.gov.tw/Tw/common/Downloader?id=07d0bb71-802b-4f43-9e05-ba09121f9c33";
	static String URL2 = "https://odws.hccg.gov.tw/001/Upload/25/opendata/9059/125/39702d7d-8e4b-4061-87df-786dc8ed85bf.csv";
	static String URL3 = "https://datacenter.taichung.gov.tw/swagger/OpenData/5251d204-d909-41ce-8251-482b5c335f60";
	static String URL4 = "https://data.tainan.gov.tw/dataset/fb79b9f2-c57d-42fc-bbd3-df0351c627b5/resource/30cb04fd-08c5-4a98-b2f0-da5cf4a261b0/download/watersource.csv";
	
	public static void main(String[] args) throws SQLException {
		
		//指定程式設定檔來連接資料庫
		File workingPath = new File(args[0]);
		
		MSSQLConnectionUtils.registerConfigFile(args[0]);
		
		//從指定網址中匯入並格式化資料
		loadDataFromWebIntoDataBaseDemo1();
		loadDataFromWebIntoDataBaseDemo2();
		loadDataFromWebIntoDataBaseDemo3();
		
		//對水源類型資料表插入一筆記錄
		TableWaterType.INSTANCE.insertData(new WaterType("2ㄏ"));
		
		//從加水站資料表中取出ID為12號的記錄，修改它並再更新回去
		
		WaterStation data = TableWaterStations.INSTANCE.getDataByPK(12).get();
		data.setWaterType(new WaterType(3,""));
		data.setProvider(null);
		TableWaterStations.INSTANCE.updateData(data);
		
		
		//插入一筆全新的加水站資料
		System.out.println(TableWaterStations.INSTANCE.insertData(new WaterStation(new Location("太平洋"), null, null, null, null)));
		
		//刪除ID為2及1029的加水站資料
		TableWaterStations.INSTANCE.deleteData(new WaterStation(2));
		TableWaterStations.INSTANCE.deleteData(new WaterStation(1029));
		
		//匯出加水站資料表為CSV檔案
		exportDataFromDataBaseDemo1(workingPath.getParent());
		
	}
	/*
	public static void test() {
		
		Optional<URL> source = Optional.empty();
		//https://www.k12ea.gov.tw/Tw/common/Downloader?id=07d0bb71-802b-4f43-9e05-ba09121f9c33
		//https://odws.hccg.gov.tw/001/Upload/25/opendata/9059/125/39702d7d-8e4b-4061-87df-786dc8ed85bf.csv
		try {
			source = Optional.ofNullable(new URL(URL2));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		
		CSVParser p = new CSVParser();
		
		try(InputStreamReader isr = new InputStreamReader(source.get().openStream(), "MS950")) {
			
			BufferedReader br = new BufferedReader(isr);
			String tmp = br.readLine();
			ArrayList<String> tmp2;
			while(tmp != null) {
				System.out.println(tmp);
				tmp2 = p.decodeRawString(tmp);
				System.out.println(tmp2);
				System.out.println(tmp2.size());
				Thread.sleep(1000L);
				tmp = br.readLine();
			}
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	*/
	public static void loadDataFromWebIntoDataBaseDemo1() {
		//資料表中沒有記錄更新日期，因此從網站上自行確認更新日期
		Date updateDate = getSpecificDate(1911+110,5,3);
		
		Optional<URL> source = Optional.empty();
		try {
			source = Optional.ofNullable(new URL(URL2));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		
		CSVParser p = new CSVParser();
		
		try(InputStreamReader isr = new InputStreamReader(source.get().openStream(), "MS950")) {
			
			BufferedReader br = new BufferedReader(isr);
			
			//略過CSV檔第一行，因為它是表格標頭:)，不是數據
			String tmp = br.readLine();
			tmp = br.readLine();
			
			ArrayList<String> tmp2;
			
			WaterStation record;
			
			while(tmp != null) {
				tmp2 = p.decodeRawString(tmp);
				
				System.out.println(tmp);
				
				record = new WaterStation(new Location(tmp2.get(3)), tmp2.get(1), new WaterType(tmp2.get(4)), new Provider(tmp2.get(2)), updateDate);
				try {
					TableWaterStations.INSTANCE.insertData(record);
				} catch (SQLException e) {
					System.out.println(tmp);
					e.printStackTrace();
				}
				
				tmp = br.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void loadDataFromWebIntoDataBaseDemo2() {
		//資料表中沒有記錄更新日期，因此從網站上自行確認更新日期
		Date updateDate = getSpecificDate(1911+110,5,6);
		
		Optional<URL> source = Optional.empty();
		try {
			source = Optional.ofNullable(new URL(URL3));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		
		CSVParser p = new CSVParser();
		
		try(InputStreamReader isr = new InputStreamReader(source.get().openStream(), "UTF-8")) {
			
			BufferedReader br = new BufferedReader(isr);
			
			//略過CSV檔第一行，因為它是表格標頭:)，不是數據
			String tmp = br.readLine();
			tmp = br.readLine();
			
			ArrayList<String> tmp2;
			
			WaterStation record;
			
			while(tmp != null) {
				tmp2 = p.decodeRawString(tmp);
				
				System.out.println(tmp);
				
				record = new WaterStation(new Location(tmp2.get(2)+tmp2.get(3)+tmp2.get(4)), tmp2.get(1), null, null, updateDate);
				try {
					TableWaterStations.INSTANCE.insertData(record);
				} catch (SQLException e) {
					System.out.println(tmp);
					e.printStackTrace();
				}
				
				tmp = br.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void loadDataFromWebIntoDataBaseDemo3() {
		//資料表中沒有記錄更新日期，因此從網站上自行確認更新日期
		Date updateDate = getSpecificDate(1911+110,6,1);
		
		Optional<URL> source = Optional.empty();
		try {
			source = Optional.ofNullable(new URL(URL4));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		
		CSVParser p = new CSVParser();
		
		try(InputStreamReader isr = new InputStreamReader(source.get().openStream(), "MS950")) {
			
			BufferedReader br = new BufferedReader(isr);
			
			//略過CSV檔第一行，因為它是表格標頭:)，不是數據
			String tmp = br.readLine();
			tmp = br.readLine();
			
			ArrayList<String> tmp2;
			
			WaterStation record;
			
			while(tmp != null) {
				tmp2 = p.decodeRawString(tmp);
				
				System.out.println(tmp);
				
				record = new WaterStation(new Location(tmp2.get(2)), tmp2.get(1), new WaterType(tmp2.get(3)), null, updateDate);
				try {
					TableWaterStations.INSTANCE.insertData(record);
				} catch (SQLException e) {
					System.out.println(tmp);
					e.printStackTrace();
				}
				
				tmp = br.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void exportDataFromDataBaseDemo1(String workingPath) {
		
		TableWaterStations.INSTANCE.exportTable(new String[] {workingPath + "\\out.csv"});
		
		///////////////////////////////////////////
		
		WaterStation queryData = new WaterStation(null);
		queryData.setProvider(new Provider("天悅淨水企業社"));
		
		List<WaterStation> data;
		try {
			data = TableWaterStations.INSTANCE.getSpecificData(queryData);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		TableWaterStations.INSTANCE.exportTable(new String[] {workingPath + "\\out2.csv"}, data);
		
		///////////////////////////////////////////
		
		queryData = new WaterStation(null);
		queryData.setProvider(new Provider("水源淨水有限公司"));
		queryData.setWaterType(new WaterType("自用水"));
		
		try {
			data = TableWaterStations.INSTANCE.getSpecificData(queryData);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		TableWaterStations.INSTANCE.exportTable(new String[] {workingPath + "\\out3.csv"}, data);
		
		//////////////////////////////////////////
		
		queryData = new WaterStation(null);
		queryData.setLocation(new Location("北大"));
		
		try {
			data = TableWaterStations.INSTANCE.getSpecificData(queryData);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		TableWaterStations.INSTANCE.exportTable(new String[] {workingPath + "\\out4.csv"}, data);
		
		/////////////////////////////////////////
		
		queryData = new WaterStation(null);
		queryData.setLocation(new Location("大"));
		queryData.setProvider(new Provider("水"));
		
		try {
			data = TableWaterStations.INSTANCE.getSpecificData(queryData);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		TableWaterStations.INSTANCE.exportTable(new String[] {workingPath + "\\out5.csv"}, data);
		
		/////////////////////////////////////////
		
		queryData = new WaterStation(null);
		queryData.setName("水");
		
		try {
			data = TableWaterStations.INSTANCE.getSpecificData(queryData);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		TableWaterStations.INSTANCE.exportTable(new String[] {workingPath + "\\out6.csv"}, data);
		
		/////////////////////////////////////////
		
		queryData = new WaterStation(null);
		queryData.setName("水");
		queryData.setUpdateDate(getSpecificDate(1911+110,5,6));
		
		try {
			data = TableWaterStations.INSTANCE.getSpecificData(queryData);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		TableWaterStations.INSTANCE.exportTable(new String[] {workingPath + "\\out7.csv"}, data);
		
	}
	
	//回傳表示西元年、月、日的日期。直接輸入對應的年分和日期即可，不需做額外運算
	public static Date getSpecificDate(int year, int month, int day) {
		return new Date(year - 1900,month-1,day);
	}
}
