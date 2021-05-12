package net.passengerDB.iii.project.waterstation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

import net.passengerDB.iii.project.waterstation.dataparser.CSVParser;

public class Main {
	
	static String URL1 = "https://www.k12ea.gov.tw/Tw/common/Downloader?id=07d0bb71-802b-4f43-9e05-ba09121f9c33";
	static String URL2 = "https://odws.hccg.gov.tw/001/Upload/25/opendata/9059/125/39702d7d-8e4b-4061-87df-786dc8ed85bf.csv";

	
	public static void main(String[] args) {
		
		test();
		//System.out.println("\"gggh\"".replaceAll("^\"|\"$", ""));
	}

	public static void test() {
		
		Optional<URL> source = Optional.empty();
		//https://www.k12ea.gov.tw/Tw/common/Downloader?id=07d0bb71-802b-4f43-9e05-ba09121f9c33
		//https://odws.hccg.gov.tw/001/Upload/25/opendata/9059/125/39702d7d-8e4b-4061-87df-786dc8ed85bf.csv
		try {
			source = Optional.ofNullable(new URL(URL1));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		
		CSVParser<String> p = new CSVParser<>(null);
		
		try(InputStreamReader isr = new InputStreamReader(source.get().openStream(), "UTF-8")) {
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
