package com.berkay.movies_web_service.utils;

import java.io.BufferedReader;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {
	
	public static void write(String path, String data) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
		writer.write(data);
		writer.flush();
		writer.close();
	}
	
	public static String read(String path) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(path));

		String data="";
		
		String line;
		while ((line=reader.readLine()) != null) {
		    data+=line+System.lineSeparator();
		}
		reader.close();
		return data;
	}
	
	public static boolean checkForFile(String path) throws IOException {
		File file = new  File(path);
		
		if(!file.exists()) {
			file.createNewFile();
			return false;
		}
		return true;
	}
	
	
}
