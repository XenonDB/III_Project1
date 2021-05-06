package net.passengerDB.iii.project.waterstation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		
		Optional<URL> source = Optional.empty();
		//https://www.k12ea.gov.tw/Tw/common/Downloader?id=07d0bb71-802b-4f43-9e05-ba09121f9c33
		//https://odws.hccg.gov.tw/001/Upload/25/opendata/9059/125/39702d7d-8e4b-4061-87df-786dc8ed85bf.csv
		try {
			source = Optional.ofNullable(new URL("https://odws.hccg.gov.tw/001/Upload/25/opendata/9059/125/39702d7d-8e4b-4061-87df-786dc8ed85bf.csv"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		
		try(InputStreamReader isr = new InputStreamReader(source.get().openStream(), "MS950")) {
			
			BufferedReader br = new BufferedReader(isr);
			int i = 0;
			String tmp = br.readLine();
			while(tmp != null) {
				i++;
				System.out.println(tmp);
				//Thread.sleep(100L);
				tmp = br.readLine();
			}
			System.out.println(i);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
