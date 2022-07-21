package com.berkay.banking_system.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account {

	private String name;
	private String surname;
	private String email;
	private String tc;
	private String type;
	private long accountNumber;
	private long lastUpdateDate;
	@Builder.Default
	private double balance=0.0;
}
