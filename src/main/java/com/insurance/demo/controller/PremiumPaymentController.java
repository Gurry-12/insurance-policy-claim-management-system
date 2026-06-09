package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.request.PaymentRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PaymentResponseDTO;
import com.insurance.demo.service.PremiumPaymentService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PremiumPaymentController {

	private final PremiumPaymentService paymentService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT')")
	public ApiResponseDTO<PaymentResponseDTO> makePayment(@Valid @RequestBody PaymentRequestDTO dto) {
		return paymentService.recordPayment(dto);
	}

	@GetMapping("/policy/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	public ApiResponseDTO<List<PaymentResponseDTO>> getPaymentsByPolicy(@PathVariable Long id) {
		return paymentService.getPaymentsByPolicy(id);
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
	public ApiResponseDTO<PaymentResponseDTO> getPaymentById(@PathVariable(name = "id") Long paymentId) {
		return paymentService.getPaymentById(paymentId);
	}

	@GetMapping("/page")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	public PageResponseDTO<PaymentResponseDTO> getAllPaymentsWithPagination(
			@RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDirection,
			@RequestParam(required = false) Long policyId,
			@RequestParam(required = false) String paymentStatus) {
		return paymentService.getAllPaymentsWithPagination(pageNumber, pageSize, sortBy, sortDirection, policyId, paymentStatus);
	}

	@GetMapping("/my-payments")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ApiResponseDTO<List<PaymentResponseDTO>> getMyPayments() {
		return paymentService.getMyPayments();
	}

	@GetMapping("/my-policies/{policyId}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ApiResponseDTO<List<PaymentResponseDTO>> getPaymentsByMyPolicy(@PathVariable Long policyId) {
		return paymentService.getPaymentsByMyPolicy(policyId);
	}
}
