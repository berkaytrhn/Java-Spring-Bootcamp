package com.berkay.banking_system_security.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

	private int id;
	private String name;
	private String surname;
	private String email;
	private String tc;
	private String type;
	private int userId;

	
	private long accountNumber;
	
	@JsonIgnore
	private long lastUpdateDate;
	
	@Builder.Default
	private double balance=0.0;
	
	@Builder.Default
	private boolean deleted=false;
}
