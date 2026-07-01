package com.insurance.demo.serviceimpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
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
import com.insurance.demo.enums.ProductType;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.model.Claim;
import com.insurance.demo.model.ClaimStatusHistory;
import com.insurance.demo.model.Policy;
import com.insurance.demo.repository.AppUserRepository;
import com.insurance.demo.repository.ClaimDocumentRepository;
import com.insurance.demo.repository.ClaimRepository;
import com.insurance.demo.repository.ClaimStatusHistoryRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.ClaimDocumentService;
import com.insurance.demo.service.ClaimService;
import com.insurance.demo.util.ClaimNumberGenerator;
import com.insurance.demo.util.PaginationValidator;

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
	private final ClaimDocumentRepository claimDocumentRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> raiseClaim(ClaimRequestDTO dto, List<MultipartFile> files)
			throws IOException {// Customer only

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

			String contentType = file.getContentType();
			if (contentType == null || !(contentType.equals("application/pdf") || contentType.startsWith("image/"))) {
				throw new BadRequestException("Only PDF and image files are allowed.");
			}

			if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
				throw new BadRequestException("File size exceeds the 5MB limit.");
			}
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		// Find Policy
		Policy policy = policyRepository.findById(dto.getPolicyId())
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + dto.getPolicyId()));

		// SRS Business Rules
		if (!policy.getCustomer().getUser().getEmail().equals(email)) {
			throw new BadRequestException("Claims can only be filed against your own active policies.");
		}

		if (!List.of(PolicyStatus.ACTIVE).contains(policy.getPolicyStatus())) {
			throw new BadRequestException("Claim can only be raised against Active policies");
		}

		BigDecimal activeClaimsSum = claimRepository.sumActiveClaimsByPolicyId(policy.getId(), ClaimStatus.REJECTED);
		BigDecimal remainingCoverage = policy.getPolicyPlan().getCoverageAmount().subtract(activeClaimsSum);

		if (dto.getClaimAmount().compareTo(remainingCoverage) > 0) {
			throw new BadRequestException("The requested claim amount exceeds your remaining policy coverage of " + remainingCoverage);
		}

		if (dto.getIncidentDate().isAfter(LocalDate.now())) {
			throw new BadRequestException("Incident date cannot be in the future");
		}
		
		if(dto.getIncidentDate().isBefore(policy.getStartDate()) || dto.getIncidentDate().isAfter(policy.getEndDate())) {
			throw new BadRequestException("Incident date should be between the policy period");
		}

		// Create Claim
		Claim claim = new Claim();
		claim.setPolicy(policy);
		claim.setClaimAmount(dto.getClaimAmount());
		claim.setClaimReason(dto.getClaimReason());
		claim.setIncidentDate(dto.getIncidentDate().atStartOfDay());
		claim.setClaimStatus(ClaimStatus.SUBMITTED);
		claim.setClaimNumber(ClaimNumberGenerator.generateClaimNumber());

		Claim savedClaim = claimRepository.save(claim);

		List<ClaimDocumentResponseDTO> response = claimDocumentService.addDocumentsToClaim(savedClaim.getId(), files);

		// Record History
		recordClaimHistory(savedClaim, null, ClaimStatus.SUBMITTED, "Claim submitted by customer with documents",
				email);

		ClaimResponseDTO responseDto = convertToResponseDTO(savedClaim);

		responseDto.setDocuments(response);

		return new ApiResponseDTO<>("Claim submitted successfully with supporting documents.", true, responseDto,
				LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> reviewClaim(Long claimId, ClaimReviewRequestDTO dto) {

		if (dto.getRecommendedStatus() != ClaimStatus.RECOMMENDED_FOR_APPROVAL
				&& dto.getRecommendedStatus() != ClaimStatus.RECOMMENDED_FOR_REJECTION) {

			throw new BadRequestException("Internal Staff can only recommend approval or rejection of a claim.");
		}

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AppUser currentUser = userRepository.findByEmail(auth.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (claim.getAssignedStaff() == null || !claim.getAssignedStaff().getId().equals(currentUser.getId())) {
			throw new AccessDeniedException("You are not authorized to review this claim. It is assigned to another staff member.");
		}

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {
			throw new BadRequestException("This claim has already been approved or rejected and cannot be modified.");
		}

		if (claim.getClaimStatus() != ClaimStatus.UNDER_REVIEW) {
			throw new BadRequestException("The claim must be under review before a recommendation can be made.");
		}

		ClaimStatus previous = claim.getClaimStatus();

		claim.setAssignedStaff(currentUser);

		claim.setClaimStatus(dto.getRecommendedStatus());
		claim.setStaffRemarks(dto.getRemarks());

		Claim updated = claimRepository.save(claim);

		recordClaimHistory(updated, previous, claim.getClaimStatus(), dto.getRemarks(),
				SecurityContextHolder.getContext().getAuthentication().getName());

		ClaimResponseDTO response = convertToResponseDTO(updated);
		return new ApiResponseDTO<>("Claim review recommendation submitted successfully", true, response,
				LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> finalDecision(Long claimId, ClaimReviewRequestDTO dto) {

		if (dto.getRecommendedStatus() != ClaimStatus.APPROVED && dto.getRecommendedStatus() != ClaimStatus.REJECTED) {

			throw new BadRequestException("Administrators can only finalize claims by approving or rejecting them.");
		}

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		if (claim.getClaimStatus() == ClaimStatus.APPROVED || claim.getClaimStatus() == ClaimStatus.REJECTED) {
			throw new BadRequestException("The final decision for this claim has already been made.");
		}

		if (claim.getClaimStatus() != ClaimStatus.RECOMMENDED_FOR_APPROVAL
				&& claim.getClaimStatus() != ClaimStatus.RECOMMENDED_FOR_REJECTION) {

			throw new BadRequestException(
					"The claim must be reviewed and recommended by an Internal Staff before a final decision.");
		}

		ClaimStatus previous = claim.getClaimStatus();

		claim.setClaimStatus(dto.getRecommendedStatus()); // ADMIN final decision
		claim.setAdminRemarks(dto.getRemarks());

		Claim updated = claimRepository.save(claim);

		recordClaimHistory(updated, previous, claim.getClaimStatus(), dto.getRemarks(),
				SecurityContextHolder.getContext().getAuthentication().getName());

		ClaimResponseDTO response = convertToResponseDTO(updated);
		return new ApiResponseDTO<>("Final decision on the claim has been recorded successfully.", true, response,
				LocalDateTime.now());
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
			throw new AccessDeniedException("You do not have permission to view this claim.");
		}

		List<ClaimDocumentResponseDTO> documents = claimDocumentRepository.findByClaimId(claim.getId()).stream()
				.map(document -> modelMapper.map(document, ClaimDocumentResponseDTO.class)).toList();

		ClaimResponseDTO response = convertToResponseDTO(claim);
		response.setDocuments(documents);
		return new ApiResponseDTO<>("Claim details retrieved successfully.", true, response, LocalDateTime.now());
	}
	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<ClaimResponseDTO>> getClaimsByPolicyId(Long policyId) {
		List<Claim> claims = claimRepository.findByPolicyId(policyId);
		List<ClaimResponseDTO> responseList = new java.util.ArrayList<>();
		for (Claim claim : claims) {
			List<com.insurance.demo.dto.response.ClaimDocumentResponseDTO> documents = claimDocumentRepository.findByClaimId(claim.getId()).stream()
					.map(document -> modelMapper.map(document, com.insurance.demo.dto.response.ClaimDocumentResponseDTO.class)).toList();
			ClaimResponseDTO response = convertToResponseDTO(claim);
			response.setDocuments(documents);
			responseList.add(response);
		}
		return new ApiResponseDTO<>("Claims retrieved successfully.", true, responseList, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<ClaimResponseDTO>> getMyClaims() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		AppUser user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		List<Claim> claims = claimRepository.findByPolicyCustomerUserId(user.getId());

		// List<ClaimResponseDTO> responseList =
		// claims.stream().map(this::convertToResponseDTO).toList();
		List<ClaimResponseDTO> responseList = new ArrayList<>();

		for (Claim claim : claims) {
			List<ClaimDocumentResponseDTO> documents = claimDocumentRepository.findByClaimId(claim.getId()).stream()
					.map(document -> modelMapper.map(document, ClaimDocumentResponseDTO.class)).toList();
			ClaimResponseDTO responseDTO = convertToResponseDTO(claim);
			responseDTO.setDocuments(documents);
			responseList.add(responseDTO);
		}

		return new ApiResponseDTO<>("Customer claims retrieved successfully.", true, responseList, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<ClaimResponseDTO> getAllClaimsWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection, Long customerId, String status) {

		log.info("Fetching claims with pagination: page={}, size={}, sortBy={}, customerId={}, status={}", pageNumber,
				pageSize, sortBy, customerId, status);

		PaginationValidator.validate(pageNumber, pageSize);
		PaginationValidator.validateSortField(sortBy, Set.of("id", "claimNumber", "claimAmount", "createdDate", "claimStatus"));

		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

		ClaimStatus claimStatus = null;
		if (status != null && !status.trim().isEmpty()) {
			try {
				claimStatus = ClaimStatus.valueOf(status.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new BadRequestException("Invalid claim status provided for filtering: " + status);
			}
		}

		Page<Claim> claimPage;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		AppUser currentUser = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		boolean isInternalStaff = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_INTERNAL_STAFF"));
		ProductType staffSpeciality = null;

		if (isInternalStaff && currentUser.getStaffSpeciality() != null) {
			staffSpeciality = currentUser.getStaffSpeciality().getProductSpeciality();
		}

		if (isInternalStaff) {
			if (staffSpeciality == null) {
				// Staff without a speciality should see no claims
				claimPage = Page.empty(pageable);
			} else if (claimStatus != null) {
				claimPage = claimRepository.findByPolicyPolicyPlanInsuranceProductProductTypeAndClaimStatus(staffSpeciality, claimStatus, pageable);
			} else {
				claimPage = claimRepository.findByPolicyPolicyPlanInsuranceProductProductType(staffSpeciality, pageable);
			}
		} else if (customerId != null && claimStatus != null) {
			claimPage = claimRepository.findByPolicyCustomerIdAndClaimStatus(customerId, claimStatus, pageable);
		} else if (customerId != null) {
			claimPage = claimRepository.findByPolicyCustomerId(customerId, pageable);
		} else if (claimStatus != null) {
			claimPage = claimRepository.findByClaimStatus(claimStatus, pageable);
		} else {
			claimPage = claimRepository.findAll(pageable);
		}

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

		PaginationValidator.validate(pageNumber, pageSize);
		PaginationValidator.validateSortField(sortBy, Set.of("id", "updatedDate", "newStatus", "updatedBy"));

		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

		Page<ClaimStatusHistory> historyPage;
		boolean hasUpdatedBy = updatedBy != null && !updatedBy.trim().isEmpty();
		boolean hasStatus = status != null && !status.trim().isEmpty();

		if (hasUpdatedBy && hasStatus) {
			historyPage = historyRepository.findByClaimIdAndUpdatedByContainingIgnoreCaseAndNewStatus(claimId,
					updatedBy.trim(), status.trim(), pageable);
		} else if (hasUpdatedBy) {
			historyPage = historyRepository.findByClaimIdAndUpdatedByContainingIgnoreCase(claimId, updatedBy.trim(),
					pageable);
		} else if (hasStatus) {
			historyPage = historyRepository.findByClaimIdAndNewStatus(claimId, status.trim(), pageable);
		} else {
			historyPage = historyRepository.findByClaimId(claimId, pageable);
		}

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
			throw new BadRequestException("This claim has already been approved or rejected and cannot be modified.");
		}

		if (claim.getClaimStatus() != ClaimStatus.SUBMITTED) {
			throw new BadRequestException("Only newly submitted claims can be moved to the under review status.");
		}

		ClaimStatus previous = claim.getClaimStatus();

		// Staff recommends
		claim.setClaimStatus(ClaimStatus.UNDER_REVIEW);
		claim.setStaffRemarks("Claim under review");

		Claim updated = claimRepository.save(claim);

		recordClaimHistory(updated, previous, claim.getClaimStatus(), updated.getStaffRemarks(),
				SecurityContextHolder.getContext().getAuthentication().getName());

		ClaimResponseDTO response = convertToResponseDTO(updated);
		return new ApiResponseDTO<>("Claim status updated to under review.", true, response, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<ClaimResponseDTO> assignStaff(Long claimId) {

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

		if (claim.getClaimStatus() != ClaimStatus.UNDER_REVIEW) {
			throw new BadRequestException("Claim must be UNDER_REVIEW to be assigned.");
		}

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AppUser currentUser = userRepository.findByEmail(auth.getName())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (claim.getAssignedStaff() != null && !claim.getAssignedStaff().getId().equals(currentUser.getId())) {
			throw new BadRequestException("Claim is already assigned to another staff member.");
		}

		claim.setAssignedStaff(currentUser);
		Claim updated = claimRepository.save(claim);

		recordClaimHistory(updated, claim.getClaimStatus(), claim.getClaimStatus(), "Staff member assigned",
				auth.getName());

		ClaimResponseDTO response = convertToResponseDTO(updated);
		return new ApiResponseDTO<>("Claim successfully assigned.", true, response, LocalDateTime.now());
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
		if (claim.getAssignedStaff() != null) {
			response.setAssignedStaffId(claim.getAssignedStaff().getId());
			response.setAssignedStaffName(claim.getAssignedStaff().getFullName());
		}
		response.setClaimAmount(claim.getClaimAmount());
		response.setClaimReason(claim.getClaimReason());
		if (claim.getIncidentDate() != null) {
			response.setIncidentDate(claim.getIncidentDate().toLocalDate());
		}
		if (claim.getClaimStatus() != null) {
			response.setClaimStatus(claim.getClaimStatus().name());
		}
		response.setStaffRemarks(claim.getStaffRemarks());
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

}