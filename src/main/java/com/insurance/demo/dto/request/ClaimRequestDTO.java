package com.insurance.demo.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
public class ClaimRequestDTO {

	@NotNull(message = "Policy Id is required")
	private Long policyId;

	@NotNull(message = "Claim amount is required")
	@Positive(message = "Claim amount must be greater than zero")
	private BigDecimal claimAmount;

	@NotBlank(message = "Claim reason is required")
	private String claimReason;

	@NotNull(message = "Incident date is required")
	private LocalDate incidentDate;
	
	//private List<ClaimDocumentRequestDTO> documents;
}