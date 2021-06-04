package net.passengerDB.iii.project.waterstation.dataparser;

import java.util.ArrayList;
import java.util.Arrays;

public class CSVParser {

	private char spliter;
	
	public CSVParser() {
		this(',');
	}
	
	public CSVParser(char spliter) {
		setSpliter(spliter);
	}
	

	public CSVParser setSpliter(char s) {
		this.spliter = s;
		return this;
	}
	
	//根據此篇的基本規則進行解碼https://zh.wikipedia.org/wiki/%E9%80%97%E5%8F%B7%E5%88%86%E9%9A%94%E5%80%BC
	//已修正: BUG 使用split的話，遇到最後一個空執會丟失
	public ArrayList<String> decodeRawString(String raw) {
		
		if(raw == null) return null;
		
		//先使用分割符切割，接著立即處裡分割符和引號被包裹的情況
		//再使用管線操作對每一個元素進行適當的處理
		//注:這不處理空白出現在被包裹的值之外的狀況。也就是說諸如 ..., "Ford" ,...會被分割為「 "Ford" 」而不是「Ford」或「"Ford"」
		//此解析器不會特別去處理非法的CSV格式
		//注意:輸入中不應該看到引號單獨存在於沒有被包裹的位置裡(例如: 1,2,",5 是非法的)。單個引號在匯出成CSV時會被轉換為「包裹的引號」，即""""
		String[] tmp = raw.split(String.format("[%c]", this.spliter));
		int len = tmp.length;
		
		//被合併過去的字串，原始位置會變成null
		StringBuilder merge = null;
		for(int i = 0 , target = 0; i < tmp.length ; i++) {
			
			if(tmp[i].length() < 1) continue;
			
			if(merge != null) {
				merge.append(spliter);
				merge.append(tmp[i]);
				len--;
				if(isWrapperEnd(tmp[i])) {
					tmp[target] = merge.toString();
					merge = null;
				}
				tmp[i] = null;
			}
			else if(isWrapperStart(tmp[i])) {
				merge = new StringBuilder(tmp[i]);
				target = i;
			}
			
		}
		
		ArrayList<String> result = new ArrayList<>(len);
		Arrays.stream(tmp).forEach((e) -> {if(e != null) result.add(e);});
		
		//避免遇到最後一個空值而丟失null狀況
		//例: "1,2,3,".split(",")的長度只有3
		if(raw.charAt(raw.length()-1) == this.spliter) result.add("");
		
		//將頭和尾的引號去掉，以及將嵌入的兩個引號置換為1個引號
		return result.parallelStream()
			.map((e) -> e.replaceAll("^\"|\"$", ""))
			.map((e) -> e.replaceAll("\"{2}", "\""))
			.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		
	}
	
	//會判斷是否只是一個單個引號的字串或引號為開頭/結尾的字串
	private boolean isWrapperStart(String str) {
		return str.charAt(0) == '"' && str.length() > 1 && str.charAt(str.length()-1) != '"';
	}
	
	private boolean isWrapperEnd(String str) {
		return str.charAt(0) != '"' && str.length() > 1 && str.charAt(str.length()-1) == '"';
	}
	
	//想嘗試用stream做平行化處理，但還不太熟悉的樣子。
	public String exportToCSVRecord(String[] eles) {
		ArrayList<String> tmpResult = Arrays.stream(eles).parallel()
		.map((e) -> {
			
			String tmp = e;
			
			boolean flag = false;
			if(tmp.indexOf(this.spliter) > -1) {
				flag = true;
			}
			if(tmp.indexOf('"') > -1) {
				flag = true;
				tmp = tmp.replaceAll("\"", "\"\"");
			}
			if(flag) {
				tmp = "\"" + tmp + "\"";
			}
			return tmp;
		})
		.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		
		StringBuilder sb = new StringBuilder(tmpResult.get(0));
		
		for(int i = 1 ; i < tmpResult.size() ; i++) {
			sb.append(this.spliter);
			sb.append(tmpResult.get(i));
		}
		
		return sb.toString();
		
	}
	
}
