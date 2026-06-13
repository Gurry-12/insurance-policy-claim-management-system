package com.insurance.demo.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
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
public class RegisterCustomerRequestDTO {

	// ===== USER DETAILS =====
	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Only letters and spaces are allowed")
	@Size(min = 2, max = 100, message = "name should be beteeen 2 - 100 characters")
	@NotBlank(message = "Full name is required")
	private String fullName;

	@Email(message = "Enter valid email")
	@NotBlank(message = "Email is required")
	private String email;

	@NotBlank(message = "Password is required")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$", message = "Password must contain uppercase, lowercase, digit and special character")
	private String password;

	@NotBlank(message = "Mobile number is required")
	@Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Use international format, example: +919876543210")
	private String mobileNumber;

	// ===== CUSTOMER DETAILS =====

	@Past(message = "Date of birth must be a past date")
	private LocalDate dateOfBirth;

	@NotBlank(message = "Address is required")
	private String address;

	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Only letters and spaces are allowed")
	@NotBlank(message = "City is required")
	private String city;

	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Only letters and spaces are allowed")
	@NotBlank(message = "State is required")
	private String state;

	@Pattern(regexp = "^[1-9][0-9]{6}$", message = "Enter valid PIN code")
	private String pinCode;

	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Only letters and spaces are allowed")
	@NotBlank(message = "Nominee name is required")
	private String nomineeName;

	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Only letters and spaces are allowed")
	@NotBlank(message = "Nominee relation is required")
	private String nomineeRelation;
}