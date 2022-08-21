package com.berkay.banking_system_security.request;

import lombok.Data;

@Data
public class TransferMoneyRequest {
	private int transferredAccountId;
	private double amount;
}
