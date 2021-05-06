package net.passengerDB.iii.project.waterstation.dataparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

public class CSVParser<T> extends AbstractDataParser<T> {

	private String[] tags;
	private char spliter;
	
	public CSVParser(String encoding, InputStream source) {
		super(encoding, source);
	}
	
	@Override
	public Collection<T> parseData(Collection<T> container, Consumer<HashMap<String, String>> collectFunc) {
		
		try(InputStreamReader isr = new InputStreamReader(source, encoding)) {
			
			BufferedReader br = new BufferedReader(isr);
			String tmp = br.readLine();
			while(tmp != null) {
				System.out.println(tmp);
				tmp = br.readLine();
			}
			System.out.println(i);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public CSVParser<T> setSpliter(char s) {
		this.spliter = s;
		return this;
	}
	
	private String[] decodeRawString(String raw) {
		
		if(raw == null) return null;
		
		String[] tmp = raw.split("[,]");
		int len = tmp.length;
		
		boolean flag = false;
		StringBuilder merage = null;
		for(int i = 0 , target = 0; i < tmp.length ; i++) {
			
			if(tmp[i].length() < 2) continue;
			
			if(tmp[i].charAt(0) == '"') {
				flag = true;
				merage = new StringBuilder(tmp[i].substring(1));
				target = i;
			}
			else if(tmp[i].charAt(tmp[i].length() - 1) == '"') {
				merage.append(tmp[i]);
				len--;
				
				tmp[target] = merage.toString().str;
				
				merage = null;
			}
			else if(merage != null) {
				merage.append(tmp[i]);
				len--;
			}
			
		}
		
		
		return null;
	}
	
}
