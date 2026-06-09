package com.insurance.demo.service;

import com.insurance.demo.dto.request.ClaimDocumentRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;

import java.util.List;

public interface ClaimDocumentService {

    ApiResponseDTO<String> addDocumentsToClaim(Long claimId, List<ClaimDocumentRequestDTO> documents);
}