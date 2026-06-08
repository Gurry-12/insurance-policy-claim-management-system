package com.insurance.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.insurance.demo.dto.response.ProductResponseDTO;
import com.insurance.demo.service.PremiumPaymentService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PremiumPaymentController {

	private final PremiumPaymentService paymentService;

	@PostMapping("/create")
	@PreAuthorize("hasRole('COSTOMER')")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<PaymentResponseDTO> makePayment(@Valid @RequestBody PaymentRequestDTO dto) {

		return paymentService.recordPayment(dto);

	}

	@GetMapping("/policy/{id}")
	public ApiResponseDTO<List<PaymentResponseDTO>> getPaymentsByPolicy(@PathVariable Long id) {
		return paymentService.getPaymentsByPolicy(id);
	}

	@GetMapping("/{id}")
	public ApiResponseDTO<PaymentResponseDTO> getPaymentById(@PathVariable Long paymentId) {

		return paymentService.getPaymentById(paymentId);
	}

	@GetMapping("/page")
	public PageResponseDTO<PaymentResponseDTO> getAllPaymentsWithPagination(
			@RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "5") int pageSize,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDirection) {
		return paymentService.getAllPaymentsWithPagination(pageNumber, pageSize, sortBy, sortDirection);
	}

}
