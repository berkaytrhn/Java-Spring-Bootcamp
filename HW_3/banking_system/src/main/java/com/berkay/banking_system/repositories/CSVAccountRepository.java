package com.berkay.banking_system.repositories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.berkay.banking_system.currencyconversion.ICurrencyConverter;
import com.berkay.banking_system.model.Account;
import com.berkay.banking_system.request.AccountCreateRequest;
import com.berkay.banking_system.utils.AccountUtils;

import lombok.Getter;


@Component
@Qualifier("csv_repository")
public class CSVAccountRepository implements IAccountRepository {


	@Getter
	@Value("${currencies}")
	private static String currencies;

	 @Value("${file.repositoryBasePath}")
	 private String BASE_REPOSITORY_PATH;
	
	 
	 @Value("${field.separator}")
	 private String separator;
	 
	 @Autowired
	 private ICurrencyConverter currencyConverter;
	 
	@Override
	public Account create(AccountCreateRequest accountCreateRequest) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		// method for create account 
		
		// build account object
		Account account = Account.builder()
				.name(accountCreateRequest.getName())
				.surname(accountCreateRequest.getSurname())
				.email(accountCreateRequest.getEmail())
				.tc(accountCreateRequest.getTc())
				.type(accountCreateRequest.getType())
				.build();
		
		
		// generate account number and set last update time
		long accNum = AccountUtils.generateAccountNumber();
		account.setAccountNumber(accNum);
		account.setLastUpdateDate(System.currentTimeMillis());
		
		// write account to file
		this.update(account);
		return account;
	}

	@Override
	public Account findByAccounNumber(long accountNumber) throws IOException {
		// method for finding account by given accoun number
		
		
		// construct file path
		String filePath=String.format("%s/%s.txt", this.BASE_REPOSITORY_PATH, String.valueOf(accountNumber));
		
		// if account file does not exist, return null
		File repo=new File(filePath);
		if (!repo.exists()) return null;
		
		// read account file 
		BufferedReader reader = new BufferedReader(new FileReader(repo));
		String content=reader.readLine();
		
		// build account object from csv formatted file content
		Account account=this.csvToObject(content);
		reader.close();
		return account;
	}
	
	

	@Override
	public Account csvToObject(String csv) {
		// method for converting csv formatted content to object
		String[] attributes=csv.split(this.separator);
		Account account = Account.builder()
				.name(attributes[0])
				.surname(attributes[1])
				.email(attributes[2])
				.tc(attributes[3])
				.type(attributes[4])
				.accountNumber(Long.parseLong(attributes[5]))
				.lastUpdateDate(Long.parseLong(attributes[6]))
				.balance(Double.parseDouble(attributes[7]))
				.build();	
		return account;
	}
	

	public String objectToCSV(Account account, String separator) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<String> attributes=new ArrayList<>();
		
		// convert object to csv format dynamically using reflection
		Field[] fields = account.getClass().getDeclaredFields();
		for(Field field:fields) {
			Method method= account.getClass().getMethod(String.format("get%s", AccountUtils.upperCaseFirstLetter(field.getName())));
			attributes.add(String.valueOf(method.invoke(account)));
		}
		return String.join(separator, attributes);
	}
	
	
	@Override
	public void update(Account account) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		// method for updating given account
		
		// convert csv format 
		String converted = objectToCSV(account, this.separator);
		
		// construct file path and write
		String path=String.format("%s/%s.txt", BASE_REPOSITORY_PATH, String.valueOf(account.getAccountNumber()));
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		writer.write(String.format("%s", converted));
		writer.close();
	}

	@Override
	public boolean transfer(double amount, long senderAccountNumber, long receiverAccountNumber) throws IOException, InterruptedException, ParseException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		// sender and receiver accounts
		Account senderAccount = this.findByAccounNumber(senderAccountNumber);
		Account receiverAccount = this.findByAccounNumber(receiverAccountNumber);
		
		// even if one account is not found, return error(can not perform transfer)
		if((senderAccount==null)||(receiverAccount==null)) return false;
		

		//check if sender account balance is enough for transfer
		if((senderAccount.getBalance()-amount)<=0) return false;
		
		double finalAmount=amount;
		if(!senderAccount.getType().equals(receiverAccount.getType())) {
			//if types are not equal, convert
			finalAmount=this.currencyConverter.convertCurrency(amount, senderAccount.getType(), receiverAccount.getType());
		}
		
		
		// perform transfer whether they have different types or not, 
		// if they have; it is already converted
		senderAccount.setBalance(senderAccount.getBalance()-amount);
		receiverAccount.setBalance(receiverAccount.getBalance()+finalAmount);
		
		// update accounts because their balance has changed
		this.update(senderAccount);
		this.update(receiverAccount);
		return true;
	}

	@Override
	public Account deposit(long accountNumber, double amount) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		// account money deposit operation 
		
		// find account by number
		Account account=this.findByAccounNumber(accountNumber);

		// account not found, return account which is null
		if(account==null) return account;
		
		// account found, update balance and last modified date
		account.setBalance(account.getBalance()+amount);
		account.setLastUpdateDate(System.currentTimeMillis());
		
		//update account on storage
		this.update(account);
		return account;
	}
	
	

	public static boolean typeCheck(String inputType) {
		// method for checking types
		for(String type: CSVAccountRepository.getCurrencies().split(",")) {
			if(type.equals(inputType)) return true;
		}
		return false;
	}
	
	
	
}
