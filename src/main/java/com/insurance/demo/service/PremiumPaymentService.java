package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.request.PaymentRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PaymentResponseDTO;
import com.insurance.demo.dto.response.ProductResponseDTO;

import jakarta.validation.Valid;

public interface PremiumPaymentService {

	public ApiResponseDTO<PaymentResponseDTO> recordPayment(@Valid PaymentRequestDTO dto);
	
	public ApiResponseDTO<List<PaymentResponseDTO>> getPaymentsByPolicy(Long id);

	public ApiResponseDTO<PaymentResponseDTO> getPaymentById(Long paymentId);

	public PageResponseDTO<PaymentResponseDTO> getAllPaymentsWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection, Long policyId, String paymentStatus);

	public ApiResponseDTO<List<PaymentResponseDTO>> getMyPayments();

	public ApiResponseDTO<List<PaymentResponseDTO>> getPaymentsByMyPolicy(Long policyId);
}
