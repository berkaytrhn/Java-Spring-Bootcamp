package com.berkay.banking_system.utils;

public class AccountUtils {

	
	private static final String[] types= {"Dolar", "TL", "Altın"};
	
	
	public static long generateAccountNumber(){
		long accountNumber = (long) Math.random()*10000000000L;
		return accountNumber;
	}
	
	public static boolean accountTypeCheck(String inputType) {
		for(String type:types) {
			if(!type.equals(inputType)) return false; 
		}
		return true;
	}
}
