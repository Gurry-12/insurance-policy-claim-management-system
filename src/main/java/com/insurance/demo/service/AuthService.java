package com.insurance.demo.service;

import com.insurance.demo.dto.response.UserResponseDTO;

public interface AuthService {

	String verify(String email, String password);

}