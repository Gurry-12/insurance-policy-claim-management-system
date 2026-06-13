package com.insurance.demo.service;

import com.insurance.demo.dto.request.LoginRequestDTO;
import com.insurance.demo.dto.request.UserRequestDTO;
import com.insurance.demo.dto.request.VerifyOtpRequest;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.LoginResponseDTO;
import com.insurance.demo.dto.response.UserResponseDTO;

import jakarta.validation.Valid;

public interface AuthService {

	LoginResponseDTO login(LoginRequestDTO requestDto);

	ApiResponseDTO<UserResponseDTO> registerUser(UserRequestDTO dto);

	ApiResponseDTO<UserResponseDTO> verifyOtp(@Valid VerifyOtpRequest request);

}
