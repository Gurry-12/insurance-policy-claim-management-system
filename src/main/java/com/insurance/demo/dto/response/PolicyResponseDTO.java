package com.insurance.demo.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponseDTO {

	private Long policyId;

	private String policyNumber;

	private Long customerId;

	private String customerName;

	private Long planId;

	private String planName;

	private LocalDate startDate;

	private LocalDate endDate;

	private String policyStatus;

	private Double totalPremiumPaid;

	private String productType;

	private Double coverageAmount;

	private Double premiumAmount;

	private String premiumType;

	private LocalDateTime createdDate;
}