package com.insurance.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

	@Email(message = "enter the valid email")
	@NotBlank(message = "email is required")
	private String email;

	@NotBlank(message = "password is required")
	private String password;

}
