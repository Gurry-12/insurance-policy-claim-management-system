package com.insurance.demo.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.request.LoginRequestDTO;
import com.insurance.demo.dto.request.UserRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.LoginResponseDTO;
import com.insurance.demo.dto.response.UserResponseDTO;
import com.insurance.demo.repository.AppUserRepository;
import com.insurance.demo.security.JwtService;
import com.insurance.demo.service.AuthService;
import com.insurance.demo.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO requestDto) {

		log.info("Login request received for email: {}", requestDto.getEmail());

		return authService.login(requestDto);

	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO dto) {

		return authService.registerUser(dto);
	}

}
