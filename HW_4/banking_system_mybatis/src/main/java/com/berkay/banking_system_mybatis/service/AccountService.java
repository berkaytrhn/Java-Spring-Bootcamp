package com.berkay.banking_system_mybatis.service;

import java.io.IOException;
import java.util.HashMap;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.berkay.banking_system_mybatis.currencyconversion.ICurrencyConverter;
import com.berkay.banking_system_mybatis.models.Account;
import com.berkay.banking_system_mybatis.repository.IAccountRepository;
import com.berkay.banking_system_mybatis.request.AccountCreateRequest;
import com.berkay.banking_system_mybatis.utils.Utils;

@Component
public class AccountService {
	
	@Value("${currencies}")
	private String currencies;
	
	@Autowired
	private ICurrencyConverter currencyConverter;
	
	@Autowired
	private IAccountRepository accountRepository;
	
	@Transactional
	public boolean transfer(double amount, int senderId, int receiverId) throws IOException, InterruptedException, ParseException {
		Account sender = this.accountRepository.findById(senderId);
		Account receiver = this.accountRepository.findById(receiverId);
		
		// even if one account is not found, return error(can not perform transfer)
		if((sender==null)||(receiver==null)) return false;
		
		// sender account has not enough money to transfer 
		if((sender.getBalance()-amount)<0) return false;
		
		double finalAmount=amount;
		if(!sender.getType().equals(receiver.getType())) {
			//if types are not equal, convert
			finalAmount=this.currencyConverter.convertCurrency(amount, sender.getType(), receiver.getType());
		}

		
		// perform transfer whether they have different types or not, 
		// if they have; it is already converted
		sender.setBalance(sender.getBalance()-amount);
		receiver.setBalance(receiver.getBalance()+finalAmount);
		
		// update last modified
		sender.setLastUpdateDate(System.currentTimeMillis());
		receiver.setLastUpdateDate(System.currentTimeMillis());
		
		// update records, if one of them fails, transaction will rollback because of @Transactional annotation
		this.accountRepository.updateRecord(sender);
		boolean res=this.accountRepository.updateRecord(receiver);
		return res;
	}
	
	@Transactional
	public boolean deleteAccount(int accountId) {
		HashMap<String, Object> map = new HashMap<>() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{
			put("deleted", true);
			put("accountId", accountId);
			put("lastUpdateDate", System.currentTimeMillis());
		}};
		return this.accountRepository.deleteById(map);
	}
	
	public Account addAccount(AccountCreateRequest accountCreateRequest) {
		// build account object
		Account account = Account.builder()
				.name(accountCreateRequest.getName())
				.surname(accountCreateRequest.getSurname())
				.email(accountCreateRequest.getEmail())
				.tc(accountCreateRequest.getTc())
				.type(accountCreateRequest.getType())
				.build();
		
		
		// generate account number and set last update time
		long accNum = Utils.generateAccountNumber();
		account.setAccountNumber(accNum);
		account.setLastUpdateDate(System.currentTimeMillis());

		if (accountRepository.createRecord(account)) return account;
		
		return null; 
	}
	
	public Account getAccount(int id) {		
		Account account = this.accountRepository.findById(id);
		if(account == null) return account;


		if(account.isDeleted()) return null;
		
		return account;
	}
	
	@Transactional
	public Account deposit(int accountId, double amount) {
		Account account = this.accountRepository.findById(accountId);
		
		if (account == null)  return account;
		
		if (account.isDeleted()) return null;
		
		account.setBalance(account.getBalance()+amount);
		
		this.accountRepository.updateRecord(account);
		
		return account;
		
	}
	
	
	public boolean typeCheck(String inputType) {
		// method for checking types
		for(String type: this.currencies.split(",")) {
			if(type.equals(inputType)) return true;
		}
		return false;
	}
}
