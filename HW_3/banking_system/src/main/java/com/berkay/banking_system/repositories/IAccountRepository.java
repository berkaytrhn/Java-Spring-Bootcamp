package com.berkay.banking_system.repositories;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.json.simple.parser.ParseException;

import com.berkay.banking_system.model.Account;
import com.berkay.banking_system.request.AccountCreateRequest;

public interface IAccountRepository {
	
	public Account create(AccountCreateRequest accountCreateRequest) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException;
	
	public Account findByAccounNumber(long accountNumber) throws FileNotFoundException, IOException;
	
	public void update(Account account) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException;
	
	public Account csvToObject(String csv);
	
	public boolean transfer(double amount, long senderAccountNumber, long receiverAccountNumber) throws IOException, InterruptedException, ParseException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
	
	public Account deposit(long accountNumber, double amount) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException;
	
	public String objectToCSV(Account account, String separator) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
	
}
