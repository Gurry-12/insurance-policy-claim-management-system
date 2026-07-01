package com.insurance.demo.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponseDTO {

	private Long planId;

	private Long productId;

	private String productName;

	private String planName;

	private BigDecimal coverageAmount;

	private BigDecimal premiumAmount;

	private String premiumType;

	private Integer duration;

	private String termsAndConditions;

	private boolean isActive;

	private LocalDateTime createdDate;
}