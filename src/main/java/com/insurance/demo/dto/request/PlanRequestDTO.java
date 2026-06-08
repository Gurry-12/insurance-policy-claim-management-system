package com.insurance.demo.dto.request;

import com.insurance.demo.enums.PremiumType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequestDTO {

	@NotNull(message = "Product Id is required")
	private Long productId;

	@NotBlank(message = "Plan name is required")
	private String planName;

	@Positive(message = "Coverage amount must be greater than zero")
	private Double coverageAmount;

	@Positive(message = "Premium amount must be greater than zero")
	private Double premiumAmount;

	@NotNull(message = "Premium type is required")
	private PremiumType premiumType;

	@Positive(message = "Duration must be greater than zero")
	private Integer duration;

	@NotBlank(message = "Terms and conditions are required")
	private String termsAndConditions;
}