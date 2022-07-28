package com.berkay.banking_system.response;

import lombok.Data;

@Data
public class LogResponse {
	private String log;

	public LogResponse(String log) {
		super();
		this.log = log;
	}
}
