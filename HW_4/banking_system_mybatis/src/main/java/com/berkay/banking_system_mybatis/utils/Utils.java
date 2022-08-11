package com.berkay.banking_system_mybatis.utils;

public class Utils {
	public static long generateAccountNumber(){
		return (long) (Math.random()*10000000000L);
	}
	
	public static String upperCaseFirstLetter(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}
}
