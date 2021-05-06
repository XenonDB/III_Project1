package net.passengerDB.iii.project.waterstation.dataparser;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

public abstract class AbstractDataParser<T> {

	protected String encoding;
	protected InputStream source;
	
	public AbstractDataParser(String encoding, InputStream source) {
		this.encoding = encoding;
		this.source = source;
	}
	
	/**
	 * 每一筆資料解析完後，會使用Consumer的apply來將解析後的結果根據定義好的apply收集起來。
	 * apply的第一個變數為欲收集資料的容器，第二個變數為parseData解析好的單筆資料，回傳值為將解析好的資料加入容器並格式化的結果。
	 * 資料全數解析完後，回傳收集好且格式化過的資料。
	 * 每一筆資料的解析結果會使用hashmap保存，記錄資料中的鍵與對應的值。
	 * */
	public abstract Collection<T> parseData(Collection<T> container , Consumer<HashMap<String,String>> collectFunc);
	
	public AbstractDataParser<T> setEncoding(String enc) {
		this.encoding = enc;
		return this;
	}
	
	public AbstractDataParser<T> setSource(InputStream is) {
		this.source = is;
		return this;
	}
	
}
