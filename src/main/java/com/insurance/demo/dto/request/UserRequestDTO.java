package com.insurance.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Only letters and spaces are allowed")
	@NotBlank(message = "Full name is required")
	@Size(min = 2, max = 100, message = "name should be beteeen 2 - 100 characters")
	private String fullName;

	@Email(message = "Enter valid email")
	@NotBlank(message = "Email is required")
	private String email;

	@NotBlank(message = "Password is required")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,15}$", message = "Password must contain uppercase, lowercase, digit and special character")
	private String password;

	@NotBlank(message = "Mobile number is required")
	@Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Use international format, example: +919876543210")
	private String mobileNumber;

}