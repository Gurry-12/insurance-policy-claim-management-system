package com.insurance.demo.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.insurance.demo.dto.request.ClaimDocumentRequestDTO;
import com.insurance.demo.dto.request.ClaimRequestDTO;
import com.insurance.demo.dto.request.ClaimReviewRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.ClaimHistoryResponseDTO;
import com.insurance.demo.dto.response.ClaimResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.model.ClaimDocument;
import com.insurance.demo.service.ClaimDocumentService;
import com.insurance.demo.service.ClaimService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ClaimController {

	private final ClaimService claimService;
	private final ClaimDocumentService claimDocumentService;

	@PostMapping(
			value = "/raise",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('CUSTOMER')")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<ClaimResponseDTO> raiseClaim(
			@Valid @RequestPart("claim") ClaimRequestDTO dto, @RequestPart("files")
			List<MultipartFile> files) throws IOException {
		return claimService.raiseClaim(dto, files);
	}

	@GetMapping("/my-claims")
	@PreAuthorize("hasRole('CUSTOMER')")
	@Operation(summary = "Customer views their own claims")
	public ApiResponseDTO<List<ClaimResponseDTO>> getMyClaims() {
		return claimService.getMyClaims();
	}

	// AGENT & ADMIN ENDPOINTS

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	@Operation(summary = "Agent/Admin - View all claims with pagination")
	public PageResponseDTO<ClaimResponseDTO> getAllClaims(@RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDirection, @RequestParam(required = false) Long customerId,
			@RequestParam(required = false) String status) {

		return claimService.getAllClaimsWithPagination(pageNumber, pageSize, sortBy, sortDirection, customerId, status);
	}

	@GetMapping("/{claimId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
	@Operation(summary = "Get claim details by ID (with ownership check in service)")
	public ApiResponseDTO<ClaimResponseDTO> getClaimById(@PathVariable Long claimId) {
		return claimService.getClaimById(claimId);
	}

	@GetMapping("/{claimId}/history")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
	@Operation(summary = "View claim status history")
	public PageResponseDTO<ClaimHistoryResponseDTO> getClaimHistory(@PathVariable Long claimId,
			@RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "id") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection,
			@RequestParam(required = false) String updatedBy, @RequestParam(required = false) String status) {
		return claimService.getClaimHistory(claimId, pageNumber, pageSize, sortBy, sortDirection, updatedBy, status);
	}

	// AGENT ENDPOINTS

	@PatchMapping("/{claimId}/under-review")
	@PreAuthorize("hasRole('AGENT')")
	@Operation(summary = "Agent marks claim under review")
	public ApiResponseDTO<ClaimResponseDTO> underReviewClaim(@PathVariable Long claimId) {
		return claimService.underReviewClaim(claimId);
	}

	@PatchMapping("/{claimId}/review")
	@PreAuthorize("hasRole('AGENT')")
	@Operation(summary = "Agent reviews and recommends claim decision")
	public ApiResponseDTO<ClaimResponseDTO> reviewClaim(@PathVariable Long claimId,
			@Valid @RequestBody ClaimReviewRequestDTO dto) {

		return claimService.reviewClaim(claimId, dto);
	}

	// ADMIN ENDPOINTS

	@PreAuthorize("hasRole('ADMIN')")
	@PatchMapping("/{claimId}/final-decision")
	@Operation(summary = "Admin makes final approval or rejection")
	public ApiResponseDTO<ClaimResponseDTO> finalDecision(@PathVariable Long claimId,
			@Valid @RequestBody ClaimReviewRequestDTO dto) {

		return claimService.finalDecision(claimId, dto);
	}


}