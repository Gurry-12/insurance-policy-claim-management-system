package com.insurance.demo.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	
	@Autowired
	private AuthenticationManager authenticationManager;

	public  String verify(String username, String password) {
		
		 Authentication authentication =
	                authenticationManager.authenticate(
	                        new UsernamePasswordAuthenticationToken(
	                                username,
	                                password
	                        ));

	        if (authentication.isAuthenticated()) {
	            return "Login Successful";
	        }

	        return "Invalid Credentials";
	    }

	}


