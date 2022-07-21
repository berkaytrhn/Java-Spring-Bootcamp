package com.berkay.banking_system.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessfulAccountResponse {
	private String message;
	private long accounNumber;
}
