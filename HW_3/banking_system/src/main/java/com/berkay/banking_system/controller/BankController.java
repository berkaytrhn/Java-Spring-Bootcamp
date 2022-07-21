package com.berkay.banking_system.controller;

import java.lang.reflect.InvocationTargetException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.berkay.banking_system.model.Account;
import com.berkay.banking_system.utils.AccountUtils;

@Controller
@RequestMapping("/api")
public class BankController {
	
	@PostMapping(path="account")
	public ResponseEntity<Object> createAccount(@RequestBody Account account) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		System.out.println(account);
		if (AccountUtils.accountTypeCheck(account.getType())); else return new ResponseEntity<Object>("Invalid account type!!", HttpStatus.BAD_REQUEST);
		
		// setting current time as update time before converting to file format
		account.setLastUpdateDate(AccountUtils.getCurrentTime());
		
		// performing file format conversion
		String converted=AccountUtils.convertFileFormat(account);
		
		// return response entity with successful status code
		return new ResponseEntity<Object>("OK!", HttpStatus.ACCEPTED);
	}
	
}
