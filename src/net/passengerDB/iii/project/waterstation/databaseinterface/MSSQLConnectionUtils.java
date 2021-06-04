package net.passengerDB.iii.project.waterstation.databaseinterface;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class MSSQLConnectionUtils {

	private static boolean initialedMSSQL = false;
	/*
	private static String DBNAME = "WaterStation";
	private static String USER = "sa";//最高權限帳戶
	*/
	private static String CONFIG_FILE_PATH = "";
	private static final String CONFIG_TAG_DBNAME = "DBNAME";
	private static final String CONFIG_TAG_USER = "USER";
	private static final String CONFIG_TAG_PASSWORD = "PASSWORD";
	private static final String CONFIG_TAG_IP = "IP";
	
	private static Connection conn;
	
	public static void registerConfigFile(String path) {
		CONFIG_FILE_PATH = path;
	}
	
	public static Optional<Connection> getConnection() throws SQLException{
		try {
			if(conn != null && !conn.isClosed()) {
				return Optional.of(conn);
			}
			
		} catch (SQLException e) {
			System.out.println("An exception occure when trying reuse connection. Create new connection.");
			e.printStackTrace();
			try {
				conn.close();
			} catch (SQLException e1) {
				System.out.println("An exception occured during trying to close connection.");
				throw e1;
			}
		}
		
		if(CONFIG_FILE_PATH != null) {
			try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE_PATH))){
				
				HashMap<String,String> configs = new HashMap<>();
				String tmp = br.readLine();
				int splitIndex;
				
				while(tmp != null) {
					splitIndex = tmp.indexOf(":");
					
					if(splitIndex > -1) configs.put(tmp.substring(0, splitIndex), tmp.substring(splitIndex+1));
					
					tmp = br.readLine();
				}
				
				conn = connectToMSSQL(configs.get(CONFIG_TAG_IP), configs.get(CONFIG_TAG_DBNAME), configs.get(CONFIG_TAG_USER), configs.get(CONFIG_TAG_PASSWORD));
				
				return Optional.ofNullable(conn);
				
			} catch (FileNotFoundException e) {
				System.err.println("Couldn't load config file from registered path: " + CONFIG_FILE_PATH);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("An unknown error occures during connecting with MSSQL server.");
				e.printStackTrace();
			}
		}
		
		return Optional.empty();
	}
	
	private static void registerMSSQLServerDrive() throws ClassNotFoundException {
		if(initialedMSSQL) return;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			System.out.println("Successful loading MS SQL driver");
			initialedMSSQL = true;
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load MS SQL driver.");
			throw e;
		}
	}
	
	private static Connection connectToMSSQL(String ip, String db, String user, String pass) {
		try {
			registerMSSQLServerDrive();
			return DriverManager.getConnection(String.format("jdbc:sqlserver://%s;databaseName=%s", ip, db), user, pass);
		} catch (Exception e) {
			System.err.println("Unable to connect with MSSQL server.");
			e.printStackTrace();
			return null;
		}
	}
	
	private static boolean freeConnection(Connection c) {
		if(c == null) {
			System.out.println("No connection to free.");
			return false;
		}
		try {
			if(c.isClosed()) {
				System.out.println("Connection has been closed.");
			}
			else {
				System.out.println("Closing database connection.");
				c.close();
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	
}
