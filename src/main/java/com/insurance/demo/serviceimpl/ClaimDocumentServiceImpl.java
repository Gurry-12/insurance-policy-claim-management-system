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
	public List<ClaimDocumentResponseDTO> addDocumentsToClaim(Long claimId, List<MultipartFile> files) throws IOException {

		String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		// Security Check - Only owner or agent/admin can add documents
		if (!claim.getPolicy().getCustomer().getUser().getEmail().equals(currentUserEmail)) {
			throw new BadRequestException("You can only add documents to your own claim");
		}

		if (files == null || files.isEmpty()) {
			throw new BadRequestException("At least one document is required");
		}

//		for (ClaimDocumentRequestDTO docDTO : documentDTOs) {
//			ClaimDocument document = new ClaimDocument();
//			document.setClaim(claim);
//			document.setName(docDTO.getDocumentName());
//			document.setDocumentType(docDTO.getDocumentType());
//			document.setDocumentReference(docDTO.getDocumentReference());
//			document.setUploadedDate(LocalDateTime.now());

		// claimDocumentRepository.save(document);

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
	public ClaimDocument uploadDocument(Long claimId, MultipartFile file) throws IOException {

		if (file == null || file.isEmpty()) {
			throw new BadRequestException("Please select a file");
		}

		String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		if (!claim.getPolicy().getCustomer().getUser().getEmail().equals(currentUserEmail)) {

			throw new BadRequestException("You can only upload documents to your own claim");
		}

		Map<String, Object> uploadResult = cloudinaryService.uploadFile(file);

		ClaimDocument document = new ClaimDocument();

		document.setClaim(claim);
		document.setName(file.getOriginalFilename());
		document.setDocumentType(file.getContentType());
		document.setDocumentUrl(uploadResult.get("secure_url").toString());
		document.setPublicId(uploadResult.get("public_id").toString());
		document.setUploadedDate(LocalDateTime.now());

		return claimDocumentRepository.save(document);
	}

	@Transactional
	@Override
	public void deleteDocument(Long documentId) throws IOException {

		ClaimDocument document = claimDocumentRepository.findById(documentId)
				.orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

		String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

		// Security Check
		if (!document.getClaim().getPolicy().getCustomer().getUser().getEmail().equals(currentUserEmail)) {

			throw new BadRequestException("You can only delete your own documents");
		}

		// Delete from Cloudinary
		cloudinaryService.deleteFile(document.getPublicId());

		// Delete from DB
		claimDocumentRepository.delete(document);

		log.info("Document {} deleted successfully", documentId);
	}
}