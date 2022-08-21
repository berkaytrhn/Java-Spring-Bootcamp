package com.berkay.banking_system_security.controller;

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
import org.springframework.security.config.authentication.UserServiceBeanDefinitionParser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.berkay.banking_system_security.kafka.ProducerClass;
import com.berkay.banking_system_security.logger.ILogger;
import com.berkay.banking_system_security.models.Account;
import com.berkay.banking_system_security.models.User;
import com.berkay.banking_system_security.request.AccountCreateRequest;
import com.berkay.banking_system_security.request.DepositRequest;
import com.berkay.banking_system_security.request.TransferMoneyRequest;
import com.berkay.banking_system_security.response.LogResponse;
import com.berkay.banking_system_security.response.SuccessfulAccountCreateResponse;
import com.berkay.banking_system_security.response.TransferResponse;
import com.berkay.banking_system_security.service.AccountService;
import com.berkay.banking_system_security.utils.Utils;


@RestController
@RequestMapping("/api/v1/")
public class AccountController {

	private final String unauthorizedMessage="You can perform operations only with your account!!";
	
	@Autowired
	private AccountService accountService;
	
	
	@Autowired
	private ProducerClass producer;
	
	
	@Autowired
	private ILogger logger;
	
	
	@RequestMapping(path="/account/{accountId}", method = RequestMethod.POST)
	public ResponseEntity<Object> transferMoney(@PathVariable int accountId, @RequestBody TransferMoneyRequest transferMoneyRequest) throws IOException, InterruptedException, ParseException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		//check if the operation is performing by the account owner
		if (! checkCredentials(accountId)) return new ResponseEntity<Object>(this.unauthorizedMessage, HttpStatus.FORBIDDEN);

		
		
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
	
	
	@RequestMapping(path="/account", method = RequestMethod.POST)
	public ResponseEntity<Object> createAccount(@RequestBody AccountCreateRequest accountCreateRequest) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException{
		
		// account type check
		if (accountService.typeCheck(accountCreateRequest.getType())); else return new ResponseEntity<Object>("Invalid account type!!", HttpStatus.BAD_REQUEST);
		
		// performing file format conversion
		System.out.println(accountCreateRequest);
		
		Account account = this.accountService.addAccount(accountCreateRequest);
		
		if (account == null) return new ResponseEntity<Object>("Error Creating account!", HttpStatus.INTERNAL_SERVER_ERROR);
		
		System.out.println(account);
		// return response entity with successful status code
		return new ResponseEntity<Object>(
				new SuccessfulAccountCreateResponse(account.getId()), 
				HttpStatus.ACCEPTED);
	}
	
	
	@RequestMapping(path = "/account/{accountId}", method = RequestMethod.PATCH)
	public ResponseEntity<Object> depositMoney(@RequestBody DepositRequest depositRequest, @PathVariable int accountId) throws FileNotFoundException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		
		System.out.println(accountService.getAccount(accountId));
		
		if (! checkCredentials(accountId)) return new ResponseEntity<Object>(this.unauthorizedMessage, HttpStatus.FORBIDDEN);

		
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
	
	
	@RequestMapping(path="/account/{id}", method = RequestMethod.GET)
	public ResponseEntity<Object> getAccount(@PathVariable int id){
		
		if (! checkCredentials(id)) return new ResponseEntity<Object>(this.unauthorizedMessage, HttpStatus.FORBIDDEN);

		
		
		Account account = this.accountService.getAccount(id);
		
		if (account == null) return new ResponseEntity<Object>("User Not Found!!", HttpStatus.NOT_FOUND);
		
		
		return ResponseEntity.ok().lastModified(account.getLastUpdateDate()).body(account);
	}
	
	
	@RequestMapping(path="/logs/{accountId}", method = RequestMethod.GET)
	public ResponseEntity<Object> getLogs(@PathVariable int accountId) throws IOException{
		
		if (! checkCredentials(accountId)) return new ResponseEntity<Object>(this.unauthorizedMessage, HttpStatus.FORBIDDEN);

		
		
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
	
	@RequestMapping(path="/account/{accountId}", method = RequestMethod.DELETE)
	public ResponseEntity<Object> deleteAccount(@PathVariable int accountId){
		
		boolean res=this.accountService.deleteAccount(accountId);
		
		if(!res) return new ResponseEntity<Object>("Delete failed!!", HttpStatus.INTERNAL_SERVER_ERROR);
		
		return new ResponseEntity<Object>("Delete Successful!", HttpStatus.OK);		
	}
	
	
	public boolean checkCredentials(int id) {
		Account account = accountService.getAccount(id);
		if (account == null) return false;
		int userId=account.getUserId();
		System.out.println(account + " : " + userId);

		User user= Utils.getAuthenticatedUserInfo();
		return (user.getId() != userId) ? false: true;
	}
	
	// kafka listener
	@KafkaListener(topics = "log", groupId = "group_1")
	public void listen(@Payload String logMessage) throws IOException {
		System.out.println(String.format("KAFKA LISTENER RECEIVED MESSAGE: %s", logMessage));
		this.logger.log(logMessage);
	}
}
