package com.insurance.demo.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.ClaimDocumentRequestDTO;
import com.insurance.demo.dto.request.ClaimRequestDTO;
import com.insurance.demo.dto.request.ClaimReviewRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.ClaimHistoryResponseDTO;
import com.insurance.demo.dto.response.ClaimResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.enums.ClaimStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.model.Claim;
import com.insurance.demo.model.ClaimDocument;
import com.insurance.demo.model.ClaimStatusHistory;
import com.insurance.demo.model.Policy;
import com.insurance.demo.repository.AppUserRepository;
import com.insurance.demo.repository.ClaimDocumentRepository;
import com.insurance.demo.repository.ClaimRepository;
import com.insurance.demo.repository.ClaimStatusHistoryRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.ClaimService;
import com.insurance.demo.util.ClaimNumberGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

	private final ClaimRepository claimRepository;
	private final PolicyRepository policyRepository;
	private final ClaimStatusHistoryRepository historyRepository;
	private final AppUserRepository userRepository;
	private final ClaimDocumentRepository claimDocumentRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> raiseClaim(ClaimRequestDTO dto) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		// Find Policy
		Policy policy = policyRepository.findById(dto.getPolicyId())
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + dto.getPolicyId()));

		// SRS Business Rules
		if (!policy.getCustomer().getUser().getEmail().equals(email)) {
			throw new BadRequestException("You can only raise a claim on your own policy");
		}

		if (policy.getPolicyStatus() != PolicyStatus.ACTIVE) {
			throw new BadRequestException("Claim can only be raised against Active policies");
		}

		if (dto.getClaimAmount() > policy.getPolicyPlan().getCoverageAmount()) {
			throw new BadRequestException("Claim amount cannot exceed policy coverage amount");
		}

		if (dto.getIncidentDate().isAfter(LocalDate.now())) {
			throw new BadRequestException("Incident date cannot be in the future");
		}

		// Create Claim
		Claim claim = modelMapper.map(dto, Claim.class);
		claim.setPolicy(policy);
		claim.setClaimStatus(ClaimStatus.SUBMITTED);
		claim.setClaimNumber(ClaimNumberGenerator.generateClaimNumber());

		Claim savedClaim = claimRepository.save(claim);

		// === Handle Documents (Important as per SRS) ===
		if (dto.getDocuments() == null || dto.getDocuments().isEmpty()) {
			throw new BadRequestException("At least one supporting document is required for claim");
		}

		for (ClaimDocumentRequestDTO docDTO : dto.getDocuments()) {
			ClaimDocument document = new ClaimDocument();
			document.setClaim(savedClaim);
			document.setName(docDTO.getDocumentName());
			document.setType(docDTO.getDocumentType());
			document.setDocumentReference(docDTO.getDocumentReference() != null ? docDTO.getDocumentReference()
					: "DOC-" + System.currentTimeMillis());
			document.setUploadedDate(LocalDateTime.now());

			claimDocumentRepository.save(document); 
		}

		// Record History
		recordClaimHistory(savedClaim, null, ClaimStatus.SUBMITTED, "Claim submitted by customer with documents",
				email);

		ClaimResponseDTO response = modelMapper.map(savedClaim, ClaimResponseDTO.class);
		return new ApiResponseDTO<>("Claim raised successfully with documents", true, response, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> reviewClaim(Long claimId, ClaimReviewRequestDTO dto) {

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {
			throw new BadRequestException("Finalized claims cannot be modified");
		}

		ClaimStatus previous = claim.getClaimStatus();

		// Agent recommends
		claim.setClaimStatus(dto.getRecommendedStatus());
		claim.setAgentRemarks(dto.getRemarks());

		Claim updated = claimRepository.save(claim);

		recordClaimHistory(updated, previous, claim.getClaimStatus(), dto.getRemarks(),
				SecurityContextHolder.getContext().getAuthentication().getName());

		ClaimResponseDTO response = modelMapper.map(updated, ClaimResponseDTO.class);
		return new ApiResponseDTO<>("Claim reviewed successfully by agent", true, response, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> finalDecision(Long claimId, ClaimReviewRequestDTO dto) {

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {
			throw new BadRequestException("Claim already finalized");
		}

		ClaimStatus previous = claim.getClaimStatus();

		claim.setClaimStatus(dto.getRecommendedStatus()); // ADMIN final decision
		claim.setAdminRemarks(dto.getRemarks());

		Claim updated = claimRepository.save(claim);

		recordClaimHistory(updated, previous, claim.getClaimStatus(), dto.getRemarks(),
				SecurityContextHolder.getContext().getAuthentication().getName());

		ClaimResponseDTO response = modelMapper.map(updated, ClaimResponseDTO.class);
		return new ApiResponseDTO<>("Final claim decision recorded by admin", true, response, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<ClaimResponseDTO> getClaimById(Long claimId) {
		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		ClaimResponseDTO response = modelMapper.map(claim, ClaimResponseDTO.class);
		return new ApiResponseDTO<>("Claim details retrieved", true, response, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<ClaimResponseDTO>> getMyClaims() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		AppUser user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		List<Claim> claims = claimRepository.findByPolicyCustomerUserId(user.getId());

		List<ClaimResponseDTO> responseList = claims.stream()
				.map(claim -> modelMapper.map(claim, ClaimResponseDTO.class)).toList();

		return new ApiResponseDTO<>("My claims retrieved successfully", true, responseList, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<ClaimResponseDTO> getAllClaimsWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection) {

		log.info("Fetching claims with pagination: page={}, size={}, sortBy={}", pageNumber, pageSize, sortBy);

		validatePagination(pageNumber, pageSize);
		validateSortField(sortBy);

		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

		Page<Claim> claimPage = claimRepository.findAll(pageable); // Admin/Agent can see all

		List<ClaimResponseDTO> content = claimPage.getContent().stream()
				.map(claim -> modelMapper.map(claim, ClaimResponseDTO.class)).toList();

		return new PageResponseDTO<>(content, claimPage.getNumber(), claimPage.getSize(), claimPage.getTotalElements(),
				claimPage.getTotalPages(), claimPage.isLast(), sortDirection);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<ClaimHistoryResponseDTO>> getClaimHistory(Long claimId) {

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		List<ClaimStatusHistory> histories = claim.getClaimStatusHistories();

		List<ClaimHistoryResponseDTO> responseList = histories.stream()
				.map(history -> modelMapper.map(history, ClaimHistoryResponseDTO.class)).toList();

		return new ApiResponseDTO<>("Claim history retrieved successfully", true, responseList, LocalDateTime.now());
	}

	// ====================== Helper Methods ======================

	private void recordClaimHistory(Claim claim, ClaimStatus previous, ClaimStatus newStatus, String remarks,
			String updatedBy) {

		ClaimStatusHistory history = new ClaimStatusHistory();
		history.setClaim(claim);
		history.setPreviousStatus(previous != null ? previous.name() : null);
		history.setNewStatus(newStatus.name());
		history.setRemarks(remarks);
		history.setUpdatedBy(updatedBy);
		history.setUpdatedDate(LocalDateTime.now());

		historyRepository.save(history);
	}

	private void validatePagination(int pageNumber, int pageSize) {
		if (pageNumber < 0)
			throw new BadRequestException("Page number cannot be negative");
		if (pageSize <= 0)
			throw new BadRequestException("Page size must be greater than 0");
		if (pageSize > 100)
			throw new BadRequestException("Page size cannot exceed 100");
	}

	private void validateSortField(String sortBy) {
		List<String> allowed = List.of("id", "claimNumber", "claimAmount", "createdDate", "claimStatus");
		if (!allowed.contains(sortBy)) {
			throw new BadRequestException("Invalid sort field: " + sortBy);
		}
	}
}