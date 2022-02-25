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
		if(s == '"') throw new IllegalArgumentException("Can't set \" as a spliter.");
		this.spliter = s;
		return this;
	}
	
	//根據此篇的基本規則進行解碼https://zh.wikipedia.org/wiki/%E9%80%97%E5%8F%B7%E5%88%86%E9%9A%94%E5%80%BC
	//已修正: BUG 使用split的話，遇到最後一個空執會丟失
	public ArrayList<String> decodeRawString(String raw) {
		
		if(raw == null) return null;
		
		//先使用分割符切割，接著立即處裡分割符和引號被包裹的情況
		//再使用管線操作對每一個元素進行適當的處理
		//注意:輸入中不應該看到引號單獨存在於沒有被包裹的位置裡(例如: 1,2,",5 是非法的)。單個引號在匯出成CSV時會被轉換為「包裹的引號」，即""""
		
		//limit設為-1來避免遇到最後一個空值而丟失null狀況
		//例: "1,2,3,".split(",")的長度只有3
		String[] tmp = raw.split(String.format("[%c]", this.spliter),-1);
		
		//被合併過去的字串，原始位置會變成null
		StringBuilder merge = null;
		for(int i = 0 , target = 0; i < tmp.length ; i++) {
			
			if(tmp[i].length() < 1) continue;
			
			if(merge != null) {
				merge.append(spliter);
				merge.append(tmp[i]);
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
		
		for(String contant : tmp) {
			if(!this.isValidContent(contant)) throw new IllegalArgumentException("Found an invalid CSV format string:["+ contant + "], at raw string:["+raw+']');
		}
		
		//將頭和尾的引號去掉，以及將嵌入的兩個引號置換為1個引號
		return Arrays.stream(tmp)
				.filter(e -> e != null)
				.map(e -> e.replaceAll("^\"|\"$", ""))
				.map(e -> e.replaceAll("\"{2}", "\""))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}
	
	//測試一個包含在CSV字串中的一格內容是否合法。
	//諸如 ..., "Ford" ,... 的CSV記錄被視為非法的，因為引號若要被視為字串的一部分，應該被一組引號包住且以兩個引號("")表示。
	//此種的CSV記錄裡當不會從正常的CSV轉換器輸出。
	private boolean isValidContent(String contant) {
		if(contant == null || contant.length() == 0) return true;
		if(contant.length() == 1) return contant.charAt(0) != '"';
		if(contant.length() > 1 && contant.charAt(0) == '"' && contant.charAt(contant.length()-1) == '"') {
			int continusQuotes = 0;
			for(int i = 1 ; i < contant.length()-2 ; i++) {
				if(contant.charAt(i) == '"') {
					continusQuotes++;
					continue;
				}else {
					if(continusQuotes % 2 == 1) return false;
					continusQuotes = 0;
				}
			}
			return continusQuotes % 2 == 0;
		}
		return contant.indexOf('"') < 0;
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
