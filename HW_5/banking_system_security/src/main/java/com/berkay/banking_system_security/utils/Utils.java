package com.berkay.banking_system_security.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.berkay.banking_system_security.models.User;

public class Utils {
	public static long generateAccountNumber(){
		return (long) (Math.random()*10000000000L);
	}
	
	public static String upperCaseFirstLetter(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}
	

	public static User getAuthenticatedUserInfo() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
}
