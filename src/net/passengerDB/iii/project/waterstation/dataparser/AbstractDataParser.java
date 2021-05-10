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
	 * parseData的變數為欲收集資料的容器。
	 * 資料全數解析完後，回傳收集好且格式化過的資料。
	 * */
	public abstract Collection<T> parseData(Collection<T> container);
	
	public AbstractDataParser<T> setEncoding(String enc) {
		this.encoding = enc;
		return this;
	}
	
	public AbstractDataParser<T> setSource(InputStream is) {
		this.source = is;
		return this;
	}
	
}
