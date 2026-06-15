package com.insurance.demo.serviceimpl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.insurance.demo.dto.request.ClaimRequestDTO;
import com.insurance.demo.dto.request.ClaimReviewRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.ClaimDocumentResponseDTO;
import com.insurance.demo.dto.response.ClaimHistoryResponseDTO;
import com.insurance.demo.dto.response.ClaimResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.enums.ClaimStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.model.Claim;
import com.insurance.demo.model.ClaimStatusHistory;
import com.insurance.demo.model.Policy;
import com.insurance.demo.repository.AppUserRepository;
import com.insurance.demo.repository.ClaimRepository;
import com.insurance.demo.repository.ClaimStatusHistoryRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.ClaimDocumentService;
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
	private final ClaimDocumentService claimDocumentService;

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> raiseClaim(ClaimRequestDTO dto, List<MultipartFile> files)
			throws IOException {// Customer only

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		// Find Policy
		Policy policy = policyRepository.findById(dto.getPolicyId())
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + dto.getPolicyId()));

		// SRS Business Rules
		if (!policy.getCustomer().getUser().getEmail().equals(email)) {
			throw new BadRequestException("You can only raise a claim on your own policy");
		}

		if (!PolicyStatus.ACTIVE.equals(policy.getPolicyStatus())) {
			throw new BadRequestException("Claim can only be raised against Active policies");
		}

		if (dto.getClaimAmount() > policy.getPolicyPlan().getCoverageAmount()) {
			throw new BadRequestException("Claim amount cannot exceed policy coverage amount");
		}

		if (dto.getIncidentDate().isAfter(LocalDate.now())) {
			throw new BadRequestException("Incident date cannot be in the future");
		}

		// Create Claim
		Claim claim = new Claim();
		claim.setPolicy(policy);
		claim.setClaimAmount(dto.getClaimAmount());
		claim.setClaimReason(dto.getClaimReason());
		claim.setIncidentDate(dto.getIncidentDate().atStartOfDay());
		claim.setClaimStatus(ClaimStatus.SUBMITTED);
		claim.setClaimNumber(ClaimNumberGenerator.generateClaimNumber());

		if (files == null || files.isEmpty()) {

			throw new BadRequestException("At least one supporting document is required for claim");
		}

		Claim savedClaim = claimRepository.save(claim);

		List<ClaimDocumentResponseDTO> response = claimDocumentService.addDocumentsToClaim(savedClaim.getId(), files);

		// Record History
		recordClaimHistory(savedClaim, null, ClaimStatus.SUBMITTED, "Claim submitted by customer with documents",
				email);

		ClaimResponseDTO responseDto = convertToResponseDTO(savedClaim);
		return new ApiResponseDTO<>("Claim raised successfully with documents", true, responseDto, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> reviewClaim(Long claimId, ClaimReviewRequestDTO dto) {

		if (dto.getRecommendedStatus() != ClaimStatus.RECOMMENDED_FOR_APPROVAL
				&& dto.getRecommendedStatus() != ClaimStatus.RECOMMENDED_FOR_REJECTION) {

			throw new BadRequestException("Agent can only recommend approval or rejection");
		}

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {
			throw new BadRequestException("Finalized claims cannot be modified");
		}

		if (claim.getClaimStatus() != ClaimStatus.UNDER_REVIEW) {
			throw new BadRequestException("Claim must be under review before recommendation");
		}

		ClaimStatus previous = claim.getClaimStatus();

		// Agent recommends
		claim.setClaimStatus(dto.getRecommendedStatus());
		claim.setAgentRemarks(dto.getRemarks());

		Claim updated = claimRepository.save(claim);

		recordClaimHistory(updated, previous, claim.getClaimStatus(), dto.getRemarks(),
				SecurityContextHolder.getContext().getAuthentication().getName());

		ClaimResponseDTO response = convertToResponseDTO(updated);
		return new ApiResponseDTO<>("Claim reviewed successfully by agent", true, response, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> finalDecision(Long claimId, ClaimReviewRequestDTO dto) {

		if (dto.getRecommendedStatus() != ClaimStatus.APPROVED && dto.getRecommendedStatus() != ClaimStatus.REJECTED) {

			throw new BadRequestException("Admin can only approve or reject claims");
		}

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {
			throw new BadRequestException("Claim already finalized");
		}

		if (claim.getClaimStatus() != ClaimStatus.RECOMMENDED_FOR_APPROVAL
				&& claim.getClaimStatus() != ClaimStatus.RECOMMENDED_FOR_REJECTION) {

			throw new BadRequestException("Claim must be reviewed by an agent before final decision");
		}

		ClaimStatus previous = claim.getClaimStatus();

		claim.setClaimStatus(dto.getRecommendedStatus()); // ADMIN final decision
		claim.setAdminRemarks(dto.getRemarks());

		Claim updated = claimRepository.save(claim);

		recordClaimHistory(updated, previous, claim.getClaimStatus(), dto.getRemarks(),
				SecurityContextHolder.getContext().getAuthentication().getName());

		ClaimResponseDTO response = convertToResponseDTO(updated);
		return new ApiResponseDTO<>("Final claim decision recorded by admin", true, response, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<ClaimResponseDTO> getClaimById(Long claimId) {
		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInEmail = authentication.getName();
		boolean isCustomer = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

		if (isCustomer && (claim.getPolicy() == null || claim.getPolicy().getCustomer() == null
				|| claim.getPolicy().getCustomer().getUser() == null
				|| !claim.getPolicy().getCustomer().getUser().getEmail().equals(loggedInEmail))) {
			throw new AccessDeniedException("You are not allowed to view this claim");
		}

		ClaimResponseDTO response = convertToResponseDTO(claim);
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

		List<ClaimResponseDTO> responseList = claims.stream().map(this::convertToResponseDTO).toList();

		return new ApiResponseDTO<>("My claims retrieved successfully", true, responseList, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<ClaimResponseDTO> getAllClaimsWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection, Long customerId, String status) {

		log.info("Fetching claims with pagination: page={}, size={}, sortBy={}, customerId={}, status={}", pageNumber,
				pageSize, sortBy, customerId, status);

		validatePagination(pageNumber, pageSize);
		validateSortField(sortBy);

		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

		ClaimStatus claimStatus = null;
		if (status != null && !status.trim().isEmpty()) {
			try {
				claimStatus = ClaimStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new BadRequestException("Invalid claim status: " + status);
			}
		}

		Page<Claim> claimPage = claimRepository.findByFilters(customerId, claimStatus, pageable);

		List<ClaimResponseDTO> content = claimPage.getContent().stream().map(this::convertToResponseDTO).toList();

		return new PageResponseDTO<>(content, claimPage.getNumber(), claimPage.getSize(), claimPage.getTotalElements(),
				claimPage.getTotalPages(), claimPage.isLast(), sortDirection);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<ClaimHistoryResponseDTO> getClaimHistory(Long claimId, int pageNumber, int pageSize,
			String sortBy, String sortDirection, String updatedBy, String status) {

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInEmail = authentication.getName();
		boolean isCustomer = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

		if (isCustomer && (claim.getPolicy() == null || claim.getPolicy().getCustomer() == null
				|| claim.getPolicy().getCustomer().getUser() == null
				|| !claim.getPolicy().getCustomer().getUser().getEmail().equals(loggedInEmail))) {
			throw new AccessDeniedException("You are not allowed to access another customer's claim history");
		}

		validatePagination(pageNumber, pageSize);
		validateHistorySortField(sortBy);

		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

		Page<ClaimStatusHistory> historyPage = historyRepository.findByFilters(claimId, updatedBy, status, pageable);

		List<ClaimHistoryResponseDTO> content = historyPage.getContent().stream().map(this::convertToHistoryResponseDTO)
				.toList();

		return new PageResponseDTO<>(content, historyPage.getNumber(), historyPage.getSize(),
				historyPage.getTotalElements(), historyPage.getTotalPages(), historyPage.isLast(), sortDirection);
	}

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> underReviewClaim(Long claimId) {

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {
			throw new BadRequestException("Finalized claims cannot be modified");
		}

		if (claim.getClaimStatus() != ClaimStatus.SUBMITTED) {
			throw new BadRequestException("Only submitted claims can be moved to under review");
		}

		ClaimStatus previous = claim.getClaimStatus();

		// Agent recommends
		claim.setClaimStatus(ClaimStatus.UNDER_REVIEW);
		claim.setAgentRemarks("Claim under review");

		Claim updated = claimRepository.save(claim);

		recordClaimHistory(updated, previous, claim.getClaimStatus(), updated.getAgentRemarks(),
				SecurityContextHolder.getContext().getAuthentication().getName());

		ClaimResponseDTO response = convertToResponseDTO(updated);
		return new ApiResponseDTO<>("Claim under review by agent", true, response, LocalDateTime.now());
	}

	// Helper Methods

	private ClaimResponseDTO convertToResponseDTO(Claim claim) {
		ClaimResponseDTO response = new ClaimResponseDTO();
		response.setClaimId(claim.getId());
		response.setClaimNumber(claim.getClaimNumber());
		if (claim.getPolicy() != null) {
			response.setPolicyId(claim.getPolicy().getId());
			response.setPolicyNumber(claim.getPolicy().getPolicyNumber());
			if (claim.getPolicy().getCustomer() != null && claim.getPolicy().getCustomer().getUser() != null) {
				response.setCustomerName(claim.getPolicy().getCustomer().getUser().getFullName());
			}
		}
		response.setClaimAmount(claim.getClaimAmount());
		response.setClaimReason(claim.getClaimReason());
		if (claim.getIncidentDate() != null) {
			response.setIncidentDate(claim.getIncidentDate().toLocalDate());
		}
		if (claim.getClaimStatus() != null) {
			response.setClaimStatus(claim.getClaimStatus().name());
		}
		response.setAgentRemarks(claim.getAgentRemarks());
		response.setAdminRemarks(claim.getAdminRemarks());
		response.setCreatedDate(claim.getCreatedDate());
		response.setUpdatedDate(claim.getUpdatedDate());
		return response;
	}

	private ClaimHistoryResponseDTO convertToHistoryResponseDTO(ClaimStatusHistory history) {
		ClaimHistoryResponseDTO response = new ClaimHistoryResponseDTO();
		response.setHistoryId(history.getId());
		response.setPreviousStatus(history.getPreviousStatus());
		response.setNewStatus(history.getNewStatus());
		response.setRemarks(history.getRemarks());
		response.setUpdatedBy(history.getUpdatedBy());
		response.setUpdatedDate(history.getUpdatedDate());
		return response;
	}

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

	private void validateHistorySortField(String sortBy) {
		List<String> allowed = List.of("id", "updatedDate", "newStatus", "updatedBy");
		if (!allowed.contains(sortBy)) {
			throw new BadRequestException("Invalid sort field for history: " + sortBy);
		}
	}
}