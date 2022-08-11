package com.berkay.banking_system_mybatis.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessfulAccountCreateResponse {
	private final String message="Account Created!";
	private long accountNumber;
	
	public SuccessfulAccountCreateResponse(long accountNumber) {
		this.accountNumber=accountNumber;
	}
}