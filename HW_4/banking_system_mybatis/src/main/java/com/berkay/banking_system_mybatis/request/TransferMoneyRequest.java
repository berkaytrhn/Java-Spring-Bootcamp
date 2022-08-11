package com.berkay.banking_system_mybatis.request;

import lombok.Data;

@Data
public class TransferMoneyRequest {
	private int transferredAccountId;
	private double amount;
}
