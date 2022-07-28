package com.berkay.banking_system.request;

import lombok.Data;

@Data
public class AccountCreateRequest {

	private String name;
    private String surname;
    private String email;
    private String tc;
    private String type;
}
