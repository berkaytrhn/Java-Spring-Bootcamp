package com.berkay.banking_system_security.models;

import lombok.Data;

@Data
public class Authority {

	private int userId;
	private String authority;
}
