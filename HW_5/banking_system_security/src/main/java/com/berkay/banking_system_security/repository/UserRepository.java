package com.berkay.banking_system_security.repository;


import org.apache.ibatis.annotations.Mapper;

import com.berkay.banking_system_security.models.User;

@Mapper
public interface UserRepository {

	public User getUserByUsername(String username);
	
}
