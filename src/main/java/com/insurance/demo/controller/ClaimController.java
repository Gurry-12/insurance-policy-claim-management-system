package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.request.ClaimDocumentRequestDTO;
import com.insurance.demo.dto.request.ClaimRequestDTO;
import com.insurance.demo.dto.request.ClaimReviewRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.ClaimHistoryResponseDTO;
import com.insurance.demo.dto.response.ClaimResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.service.ClaimDocumentService;
import com.insurance.demo.service.ClaimService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@SecurityRequirement(name = "Basic Auth")
public class ClaimController {

	private final ClaimService claimService;
	private final ClaimDocumentService claimDocumentService;

	//  CUSTOMER ENDPOINTS 

	@PostMapping("/raise")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Customer raises a new claim")
	public ApiResponseDTO<ClaimResponseDTO> raiseClaim(@Valid @RequestBody ClaimRequestDTO dto) {
		return claimService.raiseClaim(dto);
	}

	@GetMapping("/my-claims")
	@Operation(summary = "Customer views their own claims")
	public ApiResponseDTO<List<ClaimResponseDTO>> getMyClaims() {
		return claimService.getMyClaims();
	}

	//  AGENT & ADMIN ENDPOINTS 

	@GetMapping
	@Operation(summary = "Agent/Admin - View all claims with pagination")
	public PageResponseDTO<ClaimResponseDTO> getAllClaims(@RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDirection) {

		return claimService.getAllClaimsWithPagination(pageNumber, pageSize, sortBy, sortDirection);
	}

	@GetMapping("/{claimId}")
	@Operation(summary = "Get claim details by ID (with ownership check in service)")
	public ApiResponseDTO<ClaimResponseDTO> getClaimById(@PathVariable Long claimId) {
		return claimService.getClaimById(claimId);
	}

	@GetMapping("/{claimId}/history")
	@Operation(summary = "View claim status history")
	public ApiResponseDTO<List<ClaimHistoryResponseDTO>> getClaimHistory(@PathVariable Long claimId) {
		return claimService.getClaimHistory(claimId);
	}

	//  AGENT ENDPOINTS 

	@PatchMapping("/{claimId}/review")
	@Operation(summary = "Agent reviews and recommends claim decision")
	public ApiResponseDTO<ClaimResponseDTO> reviewClaim(@PathVariable Long claimId,
			@Valid @RequestBody ClaimReviewRequestDTO dto) {

		return claimService.reviewClaim(claimId, dto);
	}

	//  ADMIN ENDPOINTS 

	@PatchMapping("/{claimId}/final-decision")
	@Operation(summary = "Admin makes final approval or rejection")
	public ApiResponseDTO<ClaimResponseDTO> finalDecision(@PathVariable Long claimId,
			@Valid @RequestBody ClaimReviewRequestDTO dto) {

		return claimService.finalDecision(claimId, dto);
	}

	// Optional: Add documents to existing claim
	@PostMapping("/{claimId}/documents")
	@Operation(summary = "Add supporting documents to a claim")
	public ApiResponseDTO<String> addDocuments(@PathVariable Long claimId,
			@RequestBody List<ClaimDocumentRequestDTO> documents) {
		return claimDocumentService.addDocumentsToClaim(claimId, documents);
	}
}