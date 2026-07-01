package com.insurance.demo.dto.request;

import com.insurance.demo.enums.PremiumType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequestDTO {

	@NotNull(message = "Product Id is required")
	private Long productId;

	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Only letters and spaces are allowed")
	@NotBlank(message = "Plan name is required")
	private String planName;

	@Positive(message = "Coverage amount must be greater than zero")
	private BigDecimal coverageAmount;

	@Positive(message = "Premium amount must be greater than zero")
	private BigDecimal premiumAmount;

	@NotNull(message = "Premium type is required")
	private PremiumType premiumType;

	@Positive(message = "Duration must be greater than zero")
	@Max(value = 40, message = "Duration cannot exceed 40 years")
	private Integer duration;

	@NotBlank(message = "Terms and conditions are required")
	private String termsAndConditions;

	@NotNull(message = "Active status is required")
	private Boolean activeStatus;
}