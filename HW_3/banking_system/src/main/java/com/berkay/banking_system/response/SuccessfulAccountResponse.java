package com.berkay.banking_system.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessfulAccountResponse {
	private final String message="Account Created!";
	private long accountNumber;
	
	public SuccessfulAccountResponse(long accountNumber) {
		this.accountNumber=accountNumber;
	}
}
