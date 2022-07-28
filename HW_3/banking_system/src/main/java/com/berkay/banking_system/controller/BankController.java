package com.berkay.banking_system.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.berkay.banking_system.kafka.ProducerClass;
import com.berkay.banking_system.logger.ILogger;
import com.berkay.banking_system.model.Account;
import com.berkay.banking_system.repositories.CSVAccountRepository;
import com.berkay.banking_system.repositories.IAccountRepository;
import com.berkay.banking_system.request.AccountCreateRequest;
import com.berkay.banking_system.request.DepositRequest;
import com.berkay.banking_system.request.TransferMoneyRequest;
import com.berkay.banking_system.response.LogResponse;
import com.berkay.banking_system.response.SuccessfulAccountResponse;
import com.berkay.banking_system.response.TransferResponse;
import com.berkay.banking_system.utils.AccountUtils;

@Controller
@RequestMapping("/api/v1")
public class BankController {


	@Autowired
	@Qualifier("csv_repository")
	private IAccountRepository accountRepository;


	@Autowired
	private ILogger logger;
	
	
	@Autowired
	private ProducerClass producer;
	

	@PostMapping(path="/account")
	public ResponseEntity<Object> createAccount(@RequestBody AccountCreateRequest accountCreateRequest) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException{
		
		// account type check
		if (CSVAccountRepository.typeCheck(accountCreateRequest.getType())); else return new ResponseEntity<Object>("Invalid account type!!", HttpStatus.BAD_REQUEST);
		
		// performing file format conversion
		System.out.println(accountCreateRequest);
		Account account = this.accountRepository.create(accountCreateRequest);
		
		// return response entity with successful status code
		return new ResponseEntity<Object>(
				new SuccessfulAccountResponse(account.getAccountNumber()), 
				HttpStatus.ACCEPTED);
	}
	
	@GetMapping(path="/account/{accountNumber}")
	public ResponseEntity<Object> getAccount(@PathVariable long accountNumber) throws FileNotFoundException, IOException{

		// find relevant account
		Account account=accountRepository.findByAccounNumber(accountNumber);
		
		// not found response 
		if(account==null) return new ResponseEntity<Object>("User Not Found!!", HttpStatus.NOT_FOUND);
		
		// account found and ready to be returned
		return ResponseEntity.ok().lastModified(account.getLastUpdateDate()).body(account);
	}
	
	@PatchMapping(path = "/account/{accountNumber}")
	public ResponseEntity<Object> depositMoney(@RequestBody DepositRequest depositRequest, @PathVariable long accountNumber) throws FileNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		// deposit operation
		Account account=accountRepository.deposit(accountNumber, depositRequest.getAmount());
		
		// if deposit not success, returned null else account itself
		if(account==null) return new ResponseEntity<Object>("User Not Found!!", HttpStatus.NOT_FOUND);
					
		
		// creating and sending kafka log message
		List<String> details=new ArrayList<>();
		
		
		// put all fields and values in list for dynamic detail part
		for(Field field: depositRequest.getClass().getDeclaredFields()) {
			Method method= DepositRequest.class.getMethod(String.format("get%s", AccountUtils.upperCaseFirstLetter(field.getName())));
			details.add(String.format("%s:%s", field.getName(), String.valueOf(method.invoke(depositRequest))));
		}
		// preparing detail string part using string join from list
		String detail=String.join(",", details);
		
		// creating and sending kafka log message
		producer.sendLogMessage(
				String.format(
						"%s %s %s",
						String.valueOf(account.getAccountNumber()),
						"deposit",
						detail
					)
				);
		
		// returning http status and response for successful operation
		return ResponseEntity.ok().lastModified(account.getLastUpdateDate()).body(account);
	}

	
	@PostMapping(path="/account/{accountNumber}")
	public ResponseEntity<Object> transferMoney(@PathVariable long accountNumber, @RequestBody TransferMoneyRequest transferMoneyRequest) throws IOException, InterruptedException, ParseException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		
		// money transfer operation
		boolean res=this.accountRepository.transfer(transferMoneyRequest.getAmount(), accountNumber, transferMoneyRequest.getTransferredAccountNumber());
		
		// creating and sending kafka log message
		List<String> details=new ArrayList<>();
		
		
		// put all felds and values in hashmap for dynamic detail part
		for(Field field: transferMoneyRequest.getClass().getDeclaredFields()) {
			Method method= TransferMoneyRequest.class.getMethod(String.format("get%s", AccountUtils.upperCaseFirstLetter(field.getName())));
			details.add(String.format("%s:%s", field.getName(), String.valueOf(method.invoke(transferMoneyRequest))));
		}
		// preparing detail string part using string join from list
		String detail=String.join(",", details);
		
		
		// calling log message method of kafka producer
		producer.sendLogMessage(
				String.format(
						"%s %s %s",
						String.valueOf(accountNumber),
						"transfer",
						detail
					)
				);
		
		
		// success response
		if(res) return ResponseEntity.ok().body(new TransferResponse("Transferred Successfully"));
		
		// insufficient balance error response
		return new ResponseEntity<Object>(
				new TransferResponse("Insufficient balance"), 
				HttpStatus.NOT_ACCEPTABLE
				);
	}
	
	
	@GetMapping("/logs/{accountNumber}")
	public ResponseEntity<Object> getLogs(@PathVariable long accountNumber) throws IOException{
		
		// get logs which includes given account
		List<String> relevantLogs = this.logger.parseLog(accountNumber);

		// this account does not have any log
		if(relevantLogs==null) return new ResponseEntity<Object>("Logs Not Found!", HttpStatus.NOT_FOUND);
		
		// create log responses array
		List<LogResponse> logResponses = new ArrayList<>();
		
		// convert log strings to log response format given in readme.md
		for (String log : relevantLogs) {
			
			// deposit type transaction
			String[] components=log.split(" ");
			if(components[1].equals("deposit")) {
				
				// for account type
				Account account = this.accountRepository.findByAccounNumber(Long.parseLong(components[0]));
				
				// create log response obj and add to list
				logResponses.add(
							new LogResponse(
										String.format("%s nolu hesaba %s %s yatırılmıştır.",
													String.valueOf(components[0]),
													String.valueOf(components[2].split(":")[1]),
													account.getType()
												)
									)
						);
			}
			// transfer type transaction
			else if(components[1].equals("transfer")){
				
				// for account type
				Account account = this.accountRepository.findByAccounNumber(Long.parseLong(components[0]));
				
				// get transfer details
				String[] detail=components[2].split(",");
				String transferred=detail[0].split(":")[1];
				String amount=detail[1].split(":")[1];
				
				// create log response obj and add to list
				logResponses.add(
							new LogResponse(
										String.format("%s nolu hesaptan %s hesabına %s %s transfer edilmiştir.",
													String.valueOf(components[0]),
													transferred,
													amount,
													account.getType()
												)
									)
						);
			}
		}
		
		return new ResponseEntity<Object>(logResponses, HttpStatus.OK);
		
	}
	
	
	// kafka listener
	@KafkaListener(topics = "log", groupId = "group_1")
	public void listen(@Payload String logMessage) throws IOException {
		System.out.println(String.format("KAFKA LISTENER RECEIVED MESSAGE: %s", logMessage));
		this.logger.log(logMessage);
	}
}
