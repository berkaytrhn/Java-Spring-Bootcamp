package com.berkay.banking_system.utils;

import java.lang.reflect.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.berkay.banking_system.model.Account;

public class AccountUtils {

	
	private static final String[] types= {"Dolar", "TL", "Altın"};
	
	@Value("${file.separator}")
	private static String separator;
	
	public static String convertFileFormat(Account account) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<String> attributes=new ArrayList<>();
		
		Field[] fields = account.getClass().getDeclaredFields();
		for(Field field:fields) {
			Method method= account.getClass().getMethod(String.format("get%s", upperCaseFirstLetter(field.getName())));
			attributes.add(String.valueOf(method.invoke(account)));
		}
		return String.join(AccountUtils.separator, attributes);
		//return String.join(separator, );
	}
	
	public static long generateAccountNumber(){
		long accountNumber = (long) Math.random()*10000000000L;
		return accountNumber;
	}
	
	public static String upperCaseFirstLetter(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}
	
	public static boolean accountTypeCheck(String inputType) {
		for(String type:types) {
			System.out.println(type+inputType);
			if(type.equals(inputType)) return true;
		}
		return false;
	}
	
	public static long getCurrentTime() {
		long currentTime = new Date().getTime();
		return currentTime;
	}
}
