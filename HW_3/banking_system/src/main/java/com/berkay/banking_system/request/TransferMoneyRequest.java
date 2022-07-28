package com.berkay.banking_system.request;

import lombok.Data;

@Data
public class TransferMoneyRequest {
	private long transferredAccountNumber;
	private double amount;
}
