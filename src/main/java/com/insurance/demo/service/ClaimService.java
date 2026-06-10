package com.insurance.demo.service;

import com.insurance.demo.dto.request.ClaimRequestDTO;
import com.insurance.demo.dto.request.ClaimReviewRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.ClaimHistoryResponseDTO;
import com.insurance.demo.dto.response.ClaimResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ClaimService {

	ApiResponseDTO<ClaimResponseDTO> raiseClaim(ClaimRequestDTO dto, List<MultipartFile> files) throws IOException; // Customer only

	ApiResponseDTO<ClaimResponseDTO> reviewClaim(Long claimId, ClaimReviewRequestDTO dto); // Agent

	ApiResponseDTO<ClaimResponseDTO> finalDecision(Long claimId, ClaimReviewRequestDTO dto); // Admin

	ApiResponseDTO<ClaimResponseDTO> getClaimById(Long claimId);

	ApiResponseDTO<List<ClaimResponseDTO>> getMyClaims(); // Customer

	PageResponseDTO<ClaimResponseDTO> getAllClaimsWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection, Long customerId, String status);

	PageResponseDTO<ClaimHistoryResponseDTO> getClaimHistory(Long claimId, int pageNumber, int pageSize, String sortBy,
			String sortDirection, String updatedBy, String status);

	ApiResponseDTO<ClaimResponseDTO> underReviewClaim(Long claimId);

}