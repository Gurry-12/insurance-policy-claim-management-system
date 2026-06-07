package com.insurance.demo.dto.request;

import com.insurance.demo.enums.PaymentMode;

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

	@NotNull(message = "Payment mode is required")
	private PaymentMode paymentMode;

	@NotBlank(message = "Transaction reference is required")
	private String transactionReference;
}