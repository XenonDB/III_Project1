package net.passengerDB.iii.project.waterstation.dataparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

public class CSVParser<T> extends AbstractDataParser<T> {

	private String[] tags;
	private char spliter;
	
	public CSVParser(String encoding, InputStream source) {
		super(encoding, source);
		setSpliter(',');
	}
	
	@Override
	public Collection<T> parseData(Collection<T> container) {
		
		try(InputStreamReader isr = new InputStreamReader(source, encoding)) {
			
			BufferedReader br = new BufferedReader(isr);
			String tmp = br.readLine();
			while(tmp != null) {
				
				tmp = br.readLine();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return container;
	}

	public CSVParser<T> setSpliter(char s) {
		this.spliter = s;
		return this;
	}
	
	//根據此篇的基本規則進行解碼https://zh.wikipedia.org/wiki/%E9%80%97%E5%8F%B7%E5%88%86%E9%9A%94%E5%80%BC
	private ArrayList<String> decodeRawString(String raw) {
		
		if(raw == null) return null;
		
		//先使用分割符切割，接著立即處裡分割符和引號被包裹的情況
		//再使用管線操作對每一個元素進行適當的處理
		//注:這不處理空白出現在被包裹的值之外的狀況。也就是說諸如 ..., "Ford" ,...會被分割為「 "Ford" 」而不是「Ford」或「"Ford"」
		//此解析器不會特別去處理非法的CSV格式
		String[] tmp = raw.split(String.format("[%c]", this.spliter));
		int len = tmp.length;
		
		//被合併過去的字串，原始位置會變成null
		StringBuilder merage = null;
		for(int i = 0 , target = 0; i < tmp.length ; i++) {
			
			if(tmp[i].length() < 2) continue;
			
			if(merage != null) {
				merage.append(tmp[i]);
				len--;
				tmp[i] = null;
				continue;
			}
			
			if(isWrapperStart(tmp[i])) {
				merage = new StringBuilder(tmp[i].substring(1));
				target = i;
			}
			if(isWrapperEnd(tmp[i])) {
				merage.append(tmp[i]);
				len--;
				tmp[i] = null;
				
				tmp[target] = merage.toString();
				
				merage = null;
			}
			
		}
		
		ArrayList<String> result = new ArrayList<>(len);
		Arrays.stream(tmp).forEach((e) -> {if(e != null) result.add(e);});
		
		//將頭和尾的引號去掉
		result.parallelStream().forEach((e) -> e.replaceAll("^\"|\"$", ""));
		//將兩個引號置換為1個引號
		result.parallelStream().forEach((e) -> e.replaceAll("\"{2}", "\""));
		
		return result;
	}
	
	private boolean isWrapperStart(String str) {
		return str.charAt(0) == this.spliter && str.charAt(str.length()-1) != this.spliter;
	}
	
	private boolean isWrapperEnd(String str) {
		return str.charAt(0) != this.spliter && str.charAt(str.length()-1) == this.spliter;
	}
}
