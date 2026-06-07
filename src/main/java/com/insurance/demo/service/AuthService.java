package com.insurance.demo.service;

import com.insurance.demo.dto.request.LoginRequestDTO;

public interface AuthService {

	String verify(LoginRequestDTO loginRequestDTO);

}

