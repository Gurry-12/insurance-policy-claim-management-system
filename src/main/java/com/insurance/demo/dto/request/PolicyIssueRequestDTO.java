package com.insurance.demo.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyIssueRequestDTO {

	@NotNull(message = "Customer Id is required")
	private Long customerId;

	@NotNull(message = "Plan Id is required")
	private Long planId;

	@NotNull(message = "Start date is required")
	@PastOrPresent(message = "start date can not be in future")
	private LocalDate startDate;
}