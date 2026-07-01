package com.insurance.demo.serviceimpl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.insurance.demo.dto.request.ClaimDocumentRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.ClaimDocumentResponseDTO;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.Claim;
import com.insurance.demo.model.ClaimDocument;
import com.insurance.demo.repository.ClaimDocumentRepository;
import com.insurance.demo.repository.ClaimRepository;
import com.insurance.demo.service.ClaimDocumentService;
import com.insurance.demo.service.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimDocumentServiceImpl implements ClaimDocumentService {

	private final ClaimRepository claimRepository;
	private final ClaimDocumentRepository claimDocumentRepository;
	private final CloudinaryService cloudinaryService;

	private final ModelMapper modelMapper;

	@Override
	@Transactional
	public List<ClaimDocumentResponseDTO> addDocumentsToClaim(Long claimId, List<MultipartFile> files)
			throws IOException {

		if (files == null || files.isEmpty()) {
			throw new ResourceNotFoundException("At least one supporting document must be provided.");
		}

		for (MultipartFile file : files) {

			if (file == null || file.isEmpty()) {
				throw new BadRequestException("Uploaded document cannot be empty.");
			}

			if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
				throw new BadRequestException("Uploaded document must have a valid file name.");
			}

			// File type validation: only images and PDFs allowed
			String contentType = file.getContentType();
			if (contentType == null || !java.util.Set.of("image/jpeg", "image/png", "image/jpg", "application/pdf")
					.contains(contentType)) {
				throw new BadRequestException("Only JPEG, PNG, and PDF documents are accepted.");
			}

			// File size validation: max 10 MB per file
			if (file.getSize() > 10 * 1024 * 1024) {
				throw new BadRequestException("Each document must not exceed 10 MB in size.");
			}
		}

		String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		// Security Check - Only owner or staff/admin can add documents
		if (!claim.getPolicy().getCustomer().getUser().getEmail().equals(currentUserEmail)) {
			throw new BadRequestException("You are only permitted to upload supporting documents to your own claims.");
		}

		List<ClaimDocument> documents = new ArrayList<>();

		for (MultipartFile file : files) {
			Map<String, Object> cloudinaryMetaData = cloudinaryService.uploadFile(file);

			ClaimDocument document = new ClaimDocument();

			document.setClaim(claim);
			document.setName(file.getOriginalFilename());
			document.setDocumentType(file.getContentType());
			document.setDocumentReference(cloudinaryMetaData.get("secure_url").toString());
			document.setUploadedDate(LocalDateTime.now());

			documents.add(document);

		}

		List<ClaimDocument> output = claimDocumentRepository.saveAll(documents);

		List<ClaimDocumentResponseDTO> response = output.stream()
				.map(document -> modelMapper.map(document, ClaimDocumentResponseDTO.class)).toList();
		log.info("{} documents added to claim {}", claimId);

		return response;

	}

	@Transactional
	@Override
	public ApiResponseDTO<List<ClaimDocumentResponseDTO>> uploadDocuments(Long claimId, List<MultipartFile> files)
			throws IOException {

		List<ClaimDocumentResponseDTO> responseDTOs = addDocumentsToClaim(claimId, files);

		return new ApiResponseDTO<>("Supporting documents uploaded successfully.", true, responseDTOs,
				LocalDateTime.now());

	}

}