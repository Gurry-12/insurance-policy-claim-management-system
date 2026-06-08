package com.insurance.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
	public ApiResponseDTO<PolicyResponseDTO> purchasePolicy(@Valid @RequestBody PolicyPurchaseRequestDTO requestDTO,
			Authentication authentication) {

		return policyService.purchasePolicy(requestDTO, authentication.getName());
	}

	@PostMapping("/issue")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<PolicyResponseDTO> issuePolicy(@Valid @RequestBody PolicyIssueRequestDTO requestDTO) {

		return policyService.issuePolicy(requestDTO);
	}

	@GetMapping("/my-policies")
	public PageResponseDTO<PolicyResponseDTO> getMyPolicies(Authentication authentication,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String direction) {

		return policyService.getCustomerPolicies(authentication.getName(), page, size, sortBy, direction);
	}

	@GetMapping("/customer/{customerId}")
	public PageResponseDTO<PolicyResponseDTO> getPoliciesByCustomer(@PathVariable Long customerId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "asc") String direction) {

		return policyService.getPoliciesByCustomer(customerId, page, size, sortBy, direction);
	}

	@GetMapping
	public PageResponseDTO<PolicyResponseDTO> getAllPolicies(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String direction) {

		return policyService.getAllPolicies(page, size, sortBy, direction);
	}

	@PatchMapping("/{policyId}/activate")
	public ApiResponseDTO<PolicyResponseDTO> activatePolicy(@PathVariable Long policyId) {

		return policyService.activatePolicy(policyId);
	}

	@PatchMapping("/{policyId}/cancel")
	public ApiResponseDTO<PolicyResponseDTO> cancelPolicy(@PathVariable Long policyId) {

		return policyService.cancelPolicy(policyId);
	}
}