package com.insurance.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	private String emailOtp;
	
	@NotBlank
	private String phoneOtp;
}

