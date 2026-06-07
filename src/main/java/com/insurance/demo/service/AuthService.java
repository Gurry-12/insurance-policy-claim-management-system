package com.insurance.demo.service;

import com.insurance.demo.dto.request.LoginRequestDTO;
import com.insurance.demo.model.AppUser;

public interface AuthService {

	String verify(LoginRequestDTO loginRequestDTO);

}
