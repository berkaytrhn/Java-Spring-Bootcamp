package com.berkay.banking_system_mybatis.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account {

	private int id;
	private String name;
	private String surname;
	private String email;
	private String tc;
	private String type;
	
	private long accountNumber;
	
	@JsonIgnore
	private long lastUpdateDate;
	
	@Builder.Default
	private double balance=0.0;
	
	@Builder.Default
	private boolean deleted=false;
}
