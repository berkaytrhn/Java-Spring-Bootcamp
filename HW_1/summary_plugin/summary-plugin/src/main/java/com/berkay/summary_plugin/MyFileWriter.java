package com.berkay.summary_plugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MyFileWriter {
	
	public static void writeFile(String path, String data) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		writer.write(data);
		writer.close();
	}
}
