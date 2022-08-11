package com.berkay.banking_system_mybatis.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.berkay.banking_system_mybatis.kafka.ProducerClass;
import com.berkay.banking_system_mybatis.logger.ILogger;
import com.berkay.banking_system_mybatis.models.Account;
import com.berkay.banking_system_mybatis.request.AccountCreateRequest;
import com.berkay.banking_system_mybatis.request.DepositRequest;
import com.berkay.banking_system_mybatis.request.TransferMoneyRequest;
import com.berkay.banking_system_mybatis.response.LogResponse;
import com.berkay.banking_system_mybatis.response.SuccessfulAccountCreateResponse;
import com.berkay.banking_system_mybatis.response.TransferResponse;
import com.berkay.banking_system_mybatis.service.AccountService;
import com.berkay.banking_system_mybatis.utils.Utils;


@RestController
@RequestMapping("/api/v1/")
public class AccountController {

	@Autowired
	private AccountService accountService;
	
	
	@Autowired
	private ProducerClass producer;
	
	
	@Autowired
	private ILogger logger;
	
	
	@PostMapping(path="/account/{accountId}")
	public ResponseEntity<Object> transferMoney(@PathVariable int accountId, @RequestBody TransferMoneyRequest transferMoneyRequest) throws IOException, InterruptedException, ParseException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		// accountnumber'lar id olacak
		
		// money transfer operation
		boolean res=this.accountService.transfer(transferMoneyRequest.getAmount(), accountId, transferMoneyRequest.getTransferredAccountId());
		
		// creating and sending kafka log message
		List<String> details=new ArrayList<>();
		
		
		// put all felds and values in hashmap for dynamic detail part
		for(Field field: transferMoneyRequest.getClass().getDeclaredFields()) {
			Method method= TransferMoneyRequest.class.getMethod(String.format("get%s", Utils.upperCaseFirstLetter(field.getName())));
			details.add(String.format("%s:%s", field.getName(), String.valueOf(method.invoke(transferMoneyRequest))));
		}
		// preparing detail string part using string join from list
		String detail=String.join(",", details);
		
		
		// calling log message method of kafka producer
		producer.sendLogMessage(
				String.format(
						"%s %s %s",
						String.valueOf(accountId),
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
	
	
	@PostMapping(path="/account")
	public ResponseEntity<Object> createAccount(@RequestBody AccountCreateRequest accountCreateRequest) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException{
		
		// account type check
		if (accountService.typeCheck(accountCreateRequest.getType())); else return new ResponseEntity<Object>("Invalid account type!!", HttpStatus.BAD_REQUEST);
		
		// performing file format conversion
		System.out.println(accountCreateRequest);
		
		Account account = this.accountService.addAccount(accountCreateRequest);
		
		if (account == null) return new ResponseEntity<Object>("Error Creating account!", HttpStatus.INTERNAL_SERVER_ERROR);
		
		// return response entity with successful status code
		return new ResponseEntity<Object>(
				new SuccessfulAccountCreateResponse(account.getId()), 
				HttpStatus.ACCEPTED);
	}
	
	
	@PatchMapping(path = "/account/{accountId}")
	public ResponseEntity<Object> depositMoney(@RequestBody DepositRequest depositRequest, @PathVariable int accountId) throws FileNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		// deposit operation
		Account account = this.accountService.deposit(accountId, depositRequest.getAmount());
		
		// if deposit not success, returned null else account itself
		if(account==null) return new ResponseEntity<Object>("User Not Found!!", HttpStatus.NOT_FOUND);
					
		
		// creating and sending kafka log message
		List<String> details=new ArrayList<>();
		
		
		// put all fields and values in list for dynamic detail part
		for(Field field: depositRequest.getClass().getDeclaredFields()) {
			Method method= DepositRequest.class.getMethod(String.format("get%s", Utils.upperCaseFirstLetter(field.getName())));
			details.add(String.format("%s:%s", field.getName(), String.valueOf(method.invoke(depositRequest))));
		}
		// preparing detail string part using string join from list
		String detail=String.join(",", details);
		
		// creating and sending kafka log message
		producer.sendLogMessage(
				String.format(
						"%s %s %s",
						String.valueOf(account.getId()),
						"deposit",
						detail
					)
				);
		
		// returning http status and response for successful operation
		return ResponseEntity.ok().lastModified(account.getLastUpdateDate()).body(account);
	}
	
	
	@GetMapping(path="/account/{id}")
	public ResponseEntity<Object> getAccount(@PathVariable int id){
		Account account = this.accountService.getAccount(id);
		
		if (account == null) return new ResponseEntity<Object>("User Not Found!!", HttpStatus.NOT_FOUND);
		
		
		return ResponseEntity.ok().lastModified(account.getLastUpdateDate()).body(account);
	}
	
	
	@GetMapping("/logs/{accountId}")
	public ResponseEntity<Object> getLogs(@PathVariable int accountId) throws IOException{
		
		// get logs which includes given account
		List<String> relevantLogs = this.logger.parseLog(accountId);

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
				Account account = this.accountService.getAccount(Integer.parseInt(components[0]));
				
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
				Account account = this.accountService.getAccount(Integer.parseInt(components[0]));
				
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
	
	@DeleteMapping("/account/{accountId}")
	public ResponseEntity<Object> deleteAccount(@PathVariable int accountId){
		boolean res=this.accountService.deleteAccount(accountId);
		
		if(!res) return new ResponseEntity<Object>("Delete failed!!", HttpStatus.INTERNAL_SERVER_ERROR);
		
		return new ResponseEntity<Object>("Delete Successful!", HttpStatus.OK);		
	}
	
	
	// kafka listener
	@KafkaListener(topics = "log", groupId = "group_1")
	public void listen(@Payload String logMessage) throws IOException {
		System.out.println(String.format("KAFKA LISTENER RECEIVED MESSAGE: %s", logMessage));
		this.logger.log(logMessage);
	}
}
