package com.insurance.demo.service;

import com.insurance.demo.dto.request.PolicyIssueRequestDTO;
import com.insurance.demo.dto.request.PolicyPurchaseRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PolicyResponseDTO;

public interface PolicyService {

	ApiResponseDTO<PolicyResponseDTO> purchasePolicy(PolicyPurchaseRequestDTO requestDTO, String customerEmail);

	ApiResponseDTO<PolicyResponseDTO> issuePolicy(PolicyIssueRequestDTO requestDTO);

	PageResponseDTO<PolicyResponseDTO> getAllPolicies(int page, int size, String sortBy, String direction);

	PageResponseDTO<PolicyResponseDTO> getCustomerPolicies(String email, int page, int size, String sortBy,
			String direction);

	PageResponseDTO<PolicyResponseDTO> getPoliciesByCustomer(Long customerId, int page, int size, String sortBy,
			String direction);

	ApiResponseDTO<PolicyResponseDTO> activatePolicy(Long policyId);

	ApiResponseDTO<PolicyResponseDTO> cancelPolicy(Long policyId);
}