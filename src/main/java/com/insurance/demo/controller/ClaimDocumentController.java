package com.insurance.demo.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.ClaimDocumentResponseDTO;
import com.insurance.demo.service.ClaimDocumentService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/document")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")

public class ClaimDocumentController {

	private final ClaimDocumentService claimDocumentService;

	@PreAuthorize("hasRole('CUSTOMER')")
	@PostMapping(value = "/upload/{claimId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponseDTO<List<ClaimDocumentResponseDTO>> uploadDocument(@PathVariable Long claimId,
			@RequestParam("files") List<MultipartFile> files) throws IOException {

		return claimDocumentService.uploadDocuments(claimId, files);

	}

}
