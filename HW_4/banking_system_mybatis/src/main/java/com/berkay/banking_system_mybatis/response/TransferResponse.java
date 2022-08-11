package com.berkay.banking_system_mybatis.response;

import lombok.Data;

@Data
public class TransferResponse {

	private String message;
	
	public TransferResponse(String message) {
		this.message=message;
	}
	
}