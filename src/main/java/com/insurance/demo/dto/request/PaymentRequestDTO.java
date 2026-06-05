package com.insurance.demo.dto.request;

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
public class PaymentRequestDTO {

	@NotNull(message = "Policy Id is required")
	private Long id;

	@Positive(message = "Amount must be greater than zero")
	private Double amount;

	@NotBlank(message = "Payment mode is required")
	private String paymentMode;

	@NotBlank(message = "Transaction reference is required")
	private String transactionReference;
}