package com.berkay.banking_system_mybatis.logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class FileLogger implements ILogger {


	@Value("${log.path}")
	private String logPath;
	
	
	@Override
	public void log(String message) throws IOException{
		// write log to file
		BufferedWriter writer = new BufferedWriter(new FileWriter(logPath, true));
		writer.write(String.format("%s%s", message, System.lineSeparator()));
		writer.close();
	}

	

	@Override
	public String read() throws IOException {
		// method for reading the log file
		BufferedReader reader = new BufferedReader(new FileReader(logPath));
		
		String data="";
		String line;
		while((line=reader.readLine()) != null) {
			data+=String.format("%s%s", line, System.lineSeparator());
		}
		reader.close();
		return data;
	}



	@Override
	public List<String> parseLog(long accountNumber) throws IOException {
		// read log file and parse to filter for given account
		
		String logs=this.read();
		
		// split line by line
		String[] splittedLogs = logs.split(System.lineSeparator());
		
		List<String[]> parsedLogs=new ArrayList<>();
		
		// split line by space character for accessing by components of log text
		for(String log: splittedLogs) {
			parsedLogs.add(log.split(" "));
		}
		
		
		List<String> relevantLogs= new ArrayList<>();
		
		// relevant logs filtering operation
		for(String[] logArray: parsedLogs) {
			for(String component: logArray) {
				if(component.equals(String.valueOf(accountNumber))) {
					relevantLogs.add(String.join(" ", logArray));
				}
			}
		}
	
		// if given account has no log empty, list will be empty and return null
		if(relevantLogs.size()==0) return null;
		
		return relevantLogs;
	}

	
}

