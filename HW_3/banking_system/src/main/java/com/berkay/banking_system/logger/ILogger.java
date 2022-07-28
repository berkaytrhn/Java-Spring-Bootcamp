package com.berkay.banking_system.logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;



public interface ILogger {
	public void log(String message) throws IOException;
	
	public String read() throws FileNotFoundException, IOException;
	
	public List<String> parseLog(long accountNumber) throws IOException;
}
