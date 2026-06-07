package com.insurance.demo.service;

import org.springframework.data.domain.Page;

import com.insurance.demo.dto.request.PolicyIssueRequestDTO;
import com.insurance.demo.dto.request.PolicyPurchaseRequestDTO;
import com.insurance.demo.dto.response.PolicyResponseDTO;

public interface PolicyService {

	PolicyResponseDTO purchasePolicy(PolicyPurchaseRequestDTO requestDTO, String customerEmail);

	PolicyResponseDTO issuePolicy(PolicyIssueRequestDTO requestDTO);

	Page<PolicyResponseDTO> getAllPolicies(int page, int size, String sortBy, String direction);

	Page<PolicyResponseDTO> getCustomerPolicies(String email, int page, int size, String sortBy, String direction);

	Page<PolicyResponseDTO> getPoliciesByCustomer(Long customerId, int page, int size, String sortBy, String direction);

	PolicyResponseDTO activatePolicy(Long policyId);

	PolicyResponseDTO cancelPolicy(Long policyId);
}