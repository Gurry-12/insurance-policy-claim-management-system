package com.insurance.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.request.PolicyIssueRequestDTO;
import com.insurance.demo.dto.request.PolicyPurchaseRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PolicyResponseDTO;
import com.insurance.demo.service.PolicyService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PolicyController {

	private final PolicyService policyService;

	@PostMapping("/purchase")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('CUSTOMER')")
	public ApiResponseDTO<PolicyResponseDTO> purchasePolicy(@Valid @RequestBody PolicyPurchaseRequestDTO requestDTO) {

		return policyService.purchasePolicy(requestDTO);
	}

	@PostMapping("/issue")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<PolicyResponseDTO> issuePolicy(@Valid @RequestBody PolicyIssueRequestDTO requestDTO) {

		return policyService.issuePolicy(requestDTO);
	}

	@GetMapping("/my-policies")
	@PreAuthorize("hasRole('CUSTOMER')")
	public PageResponseDTO<PolicyResponseDTO> getMyPolicies(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String direction) {

		return policyService.getCustomerPolicies(authentication.getName(), page, size, sortBy, direction);
	}

	@GetMapping("/customer/{customerId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	public PageResponseDTO<PolicyResponseDTO> getPoliciesByCustomer(@PathVariable Long customerId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String direction) {

		return policyService.getPoliciesByCustomer(customerId, page, size, sortBy, direction);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	public PageResponseDTO<PolicyResponseDTO> getAllPolicies(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String direction,
			@RequestParam(required = false) Long customerId,
			@RequestParam(required = false) String status) {

		return policyService.getAllPolicies(page, size, sortBy, direction, customerId, status);
	}

	@GetMapping("/{policyId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
	public ApiResponseDTO<PolicyResponseDTO> getPolicyById(@PathVariable Long policyId) {
		return policyService.getPolicyById(policyId);
	}
	
	@PatchMapping("/{policyId}/cancel")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	public ApiResponseDTO<PolicyResponseDTO> cancelPolicy(@PathVariable Long policyId) {

		return policyService.cancelPolicy(policyId);
	}
}