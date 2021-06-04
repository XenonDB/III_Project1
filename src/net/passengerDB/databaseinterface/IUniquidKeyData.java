package net.passengerDB.databaseinterface;

import java.util.Optional;

public interface IUniquidKeyData<T> {

	/**
	 * 表示一筆擁有獨特欄位內容的記錄。
	 * 對於一個有primary key的table，裡面的每一筆記錄都必須要是UniquidKeyData。
	 * */
	public Optional<T> getUniquidKey();
	
}
