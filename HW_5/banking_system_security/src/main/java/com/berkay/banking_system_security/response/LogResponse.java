package com.berkay.banking_system_security.response;

import lombok.Data;

@Data
public class LogResponse {
	private String log;

	public LogResponse(String log) {
		this.log = log;
	}
}