package com.insurance.demo.service;

import com.insurance.demo.dto.request.ClaimRequestDTO;
import com.insurance.demo.dto.request.ClaimReviewRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.ClaimHistoryResponseDTO;
import com.insurance.demo.dto.response.ClaimResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;

import java.util.List;

public interface ClaimService {

    ApiResponseDTO<ClaimResponseDTO> raiseClaim(ClaimRequestDTO dto);   // Customer only

    ApiResponseDTO<ClaimResponseDTO> reviewClaim(Long claimId, ClaimReviewRequestDTO dto);  // Agent

    ApiResponseDTO<ClaimResponseDTO> finalDecision(Long claimId, ClaimReviewRequestDTO dto); // Admin

    ApiResponseDTO<ClaimResponseDTO> getClaimById(Long claimId);

    ApiResponseDTO<List<ClaimResponseDTO>> getMyClaims();   // Customer

    PageResponseDTO<ClaimResponseDTO> getAllClaimsWithPagination(int pageNumber, int pageSize, 
                                                                String sortBy, String sortDirection);

    ApiResponseDTO<List<ClaimHistoryResponseDTO>> getClaimHistory(Long claimId);
}