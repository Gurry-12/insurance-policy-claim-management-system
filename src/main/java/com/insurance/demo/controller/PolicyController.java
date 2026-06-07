package com.insurance.demo.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.insurance.demo.dto.request.PolicyIssueRequestDTO;
import com.insurance.demo.dto.request.PolicyPurchaseRequestDTO;
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
	public ResponseEntity<PolicyResponseDTO> purchasePolicy(@Valid @RequestBody PolicyPurchaseRequestDTO requestDTO,
			Authentication authentication) {

		PolicyResponseDTO response = policyService.purchasePolicy(requestDTO, authentication.getName());

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PostMapping("/issue")
	public ResponseEntity<PolicyResponseDTO> issuePolicy(@Valid @RequestBody PolicyIssueRequestDTO requestDTO) {

		PolicyResponseDTO response = policyService.issuePolicy(requestDTO);

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping("/my-policies")
	public ResponseEntity<Page<PolicyResponseDTO>> getMyPolicies(

			Authentication authentication,

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "10") int size,

			@RequestParam(defaultValue = "id") String sortBy,

			@RequestParam(defaultValue = "asc") String direction) {

		return ResponseEntity.ok(

				policyService.getCustomerPolicies(authentication.getName(), page, size, sortBy, direction));
	}

	@GetMapping("/customer/{customerId}")
	public ResponseEntity<Page<PolicyResponseDTO>> getPoliciesByCustomer(

			@PathVariable Long customerId,

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "10") int size,

			@RequestParam(defaultValue = "id") String sortBy,

			@RequestParam(defaultValue = "asc") String direction) {

		return ResponseEntity.ok(

				policyService.getPoliciesByCustomer(customerId, page, size, sortBy, direction));
	}

	@GetMapping
	public ResponseEntity<Page<PolicyResponseDTO>> getAllPolicies(

			@RequestParam(defaultValue = "0") int page,

			@RequestParam(defaultValue = "10") int size,

			@RequestParam(defaultValue = "id") String sortBy,

			@RequestParam(defaultValue = "asc") String direction) {

		return ResponseEntity.ok(

				policyService.getAllPolicies(page, size, sortBy, direction));
	}

	@PatchMapping("/{policyId}/activate")
	public ResponseEntity<PolicyResponseDTO> activatePolicy(@PathVariable Long policyId) {

		return ResponseEntity.ok(policyService.activatePolicy(policyId));
	}

	@PatchMapping("/{policyId}/cancel")
	public ResponseEntity<PolicyResponseDTO> cancelPolicy(@PathVariable Long policyId) {

		return ResponseEntity.ok(policyService.cancelPolicy(policyId));
	}
}