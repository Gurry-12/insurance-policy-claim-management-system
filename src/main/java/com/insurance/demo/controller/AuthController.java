package com.insurance.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.request.UserRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.UserResponseDTO;
import com.insurance.demo.serviceimpl.AuthService;

import jakarta.persistence.Access;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
	
	
	@Autowired
	private AuthService authService;
	
	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestParam String username,
	                                    @RequestParam String password) {

	    return ResponseEntity.ok(authService.verify(username, password));
	}
	
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO dto) {
 
		return authService.registerUser(dto);
	}

	
	

}
