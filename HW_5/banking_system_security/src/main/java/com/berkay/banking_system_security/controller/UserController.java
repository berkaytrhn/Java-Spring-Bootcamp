package com.berkay.banking_system_security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.berkay.banking_system_security.models.User;
import com.berkay.banking_system_security.request.LoginRequest;
import com.berkay.banking_system_security.security.JWTTokenUtil;

@RestController
public class UserController {

	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JWTTokenUtil tokenUtil;
	
	@Autowired
	private UserDetailsService userDetailService;
	
	
	@RequestMapping(path="authenticate", method = RequestMethod.POST)
	public ResponseEntity<Object> auth(@RequestBody LoginRequest loginRequest){
		
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));



			final UserDetails userDetails = userDetailService.loadUserByUsername(loginRequest.getUsername());

			final String token = tokenUtil.generateToken(userDetails);
			return new ResponseEntity<Object>(token, HttpStatus.OK);
			
			
		} catch (BadCredentialsException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
			return new ResponseEntity<Object>("", HttpStatus.BAD_REQUEST);
		} catch (DisabledException e) {
			System.out.println(e);
		}  catch (Exception e) {
			System.out.println(e);
		} 
		return new ResponseEntity<Object>("", HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
}
