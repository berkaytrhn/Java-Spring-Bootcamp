package com.berkay.banking_system_security.service;


import org.springframework.beans.factory.annotation.Autowired;



import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.berkay.banking_system_security.models.User;
import com.berkay.banking_system_security.repository.UserRepository;

@Service
public class MyBatisUserDetailService implements UserDetailsService{

	
	
	
	private UserRepository userRepository;

	@Autowired
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}




	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


		User user = this.userRepository.getUserByUsername(username);
		System.out.println("inside mybatis service user: " + user);
		return user;
	}

	
	

}
