package com.insurance.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDTO {

	@Email(message = "enter a valid email")
	@NotBlank(message = "email is required")
	private String email;

	@NotBlank(message = "email OTP is required")
	private String emailOtp;

	@NotBlank(message = "phone OTP is required")
	private String phoneOtp;

	@NotBlank(message = "new password is required")
	@Size(min = 8, message = "password must be at least 8 characters long")
	private String newPassword;

}