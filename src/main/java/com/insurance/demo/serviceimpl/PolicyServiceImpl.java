package com.insurance.demo.serviceimpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

import com.insurance.demo.dto.request.PolicyIssueRequestDTO;
import com.insurance.demo.dto.request.PolicyPurchaseRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PolicyResponseDTO;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.enums.ProductType;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.PlanNotActiveException;
import com.insurance.demo.exception.PolicyNotFoundException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.Customer;
import com.insurance.demo.model.Policy;
import com.insurance.demo.model.PolicyPlan;
import com.insurance.demo.repository.CustomerRepository;
import com.insurance.demo.repository.PolicyPlanRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.repository.ClaimRepository;
import com.insurance.demo.enums.ClaimStatus;
import com.insurance.demo.service.PolicyService;
import com.insurance.demo.util.PolicyNumberGenerator;
import com.sun.jdi.request.DuplicateRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

	private final PolicyRepository policyRepository;
	private final ClaimRepository claimRepository;
	private final PolicyPlanRepository policyPlanRepository;
	private final CustomerRepository customerRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ApiResponseDTO<PolicyResponseDTO> purchasePolicy(PolicyPurchaseRequestDTO requestDTO) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String customerEmail = authentication.getName();
		Customer customer = customerRepository.findByUserEmail(customerEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

		if (!isCustomerProfileComplete(customer)) {
			throw new BadRequestException("Please complete your customer profile before purchasing a policy.");
		}

		PolicyPlan plan = policyPlanRepository.findByIdAndIsActiveTrue(requestDTO.getPlanId())
				.orElseThrow(PlanNotActiveException::new);

		ProductType productType = plan.getInsuranceProduct().getProductType();

		if (productType == ProductType.HEALTH) {

			boolean exists = policyRepository.existsByCustomerIdAndPolicyPlanIdAndPolicyStatusIn(customer.getId(),
					plan.getId(), List.of(PolicyStatus.ACTIVE, PolicyStatus.PENDING_PAYMENT));

			if (exists) {
				throw new DuplicateResourceException("This health policy is already active or pending payment.");
			}

		} else {

			boolean pendingExists = policyRepository.existsByCustomerIdAndPolicyPlanIdAndPolicyStatusIn(
					customer.getId(), plan.getId(), List.of(PolicyStatus.PENDING_PAYMENT));

			if (pendingExists) {
				throw new DuplicateResourceException("This policy is already pending payment.");
			}
		}

		Policy policy = new Policy();

		policy.setCustomer(customer);
		policy.setPolicyPlan(plan);

		policy.setPolicyNumber(PolicyNumberGenerator.generatePolicyNumber());

		policy.setStartDate(requestDTO.getStartDate());

		policy.setEndDate(requestDTO.getStartDate().plusYears(plan.getDuration()));

		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);

		policy.setTotalPremiumPaid(BigDecimal.ZERO);

		Policy savedPolicy = policyRepository.save(policy);

		PolicyResponseDTO responseDTO = convertToResponseDTO(savedPolicy);

		return new ApiResponseDTO<>("Policy purchased successfully and is pending payment.", true, responseDTO,
				LocalDateTime.now());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ApiResponseDTO<PolicyResponseDTO> issuePolicy(PolicyIssueRequestDTO requestDTO) {

		Customer customer = customerRepository.findById(requestDTO.getCustomerId())
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		if (!isCustomerProfileComplete(customer)) {
			throw new BadRequestException("Please complete your customer profile before purchasing a policy.");
		}

		PolicyPlan plan = policyPlanRepository.findByIdAndIsActiveTrue(requestDTO.getPlanId())
				.orElseThrow(PlanNotActiveException::new);
		
		ProductType productType = plan.getInsuranceProduct().getProductType();

		if (productType == ProductType.HEALTH) {

			boolean exists = policyRepository.existsByCustomerIdAndPolicyPlanIdAndPolicyStatusIn(customer.getId(),
					plan.getId(), List.of(PolicyStatus.ACTIVE, PolicyStatus.PENDING_PAYMENT));

			if (exists) {
				throw new DuplicateResourceException("This health policy is already active or pending payment.");
			}

		} else {

			boolean pendingExists = policyRepository.existsByCustomerIdAndPolicyPlanIdAndPolicyStatusIn(
					customer.getId(), plan.getId(), List.of(PolicyStatus.PENDING_PAYMENT));

			if (pendingExists) {
				throw new DuplicateResourceException("This policy is already pending payment.");
			}
		}


		Policy policy = new Policy();

		policy.setCustomer(customer);
		policy.setPolicyPlan(plan);

		policy.setPolicyNumber(PolicyNumberGenerator.generatePolicyNumber());

		policy.setStartDate(requestDTO.getStartDate());

		policy.setEndDate(requestDTO.getStartDate().plusYears(plan.getDuration()));

		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);

		policy.setTotalPremiumPaid(BigDecimal.ZERO);

		Policy savedPolicy = policyRepository.save(policy);

		PolicyResponseDTO responseDTO = convertToResponseDTO(savedPolicy);

		return new ApiResponseDTO<>("Policy issued successfully to the customer.", true, responseDTO,
				LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<PolicyResponseDTO> getPolicyById(Long policyId) {
		log.info("Fetching policy by id: {}", policyId);
		Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException(policyId));

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		boolean isCustomer = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

		if (isCustomer && !policy.getCustomer().getUser().getEmail().equals(email)) {
			throw new AccessDeniedException("You are not allowed to access another customer's policy details");
		}

		PolicyResponseDTO responseDTO = convertToResponseDTO(policy);
		return new ApiResponseDTO<>("Policy details retrieved successfully", true, responseDTO, LocalDateTime.now());
	}

	@Override
	public PageResponseDTO<PolicyResponseDTO> getAllPolicies(int pageNumber, int pageSize, String sortBy,
			String sortDirection, Long customerId, String status) {

		Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

		PolicyStatus statusEnum = null;
		if (status != null && !status.trim().isEmpty()) {
			try {
				statusEnum = PolicyStatus.valueOf(status.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new BadRequestException("Invalid policy status filter: " + status);
			}
		}

		Page<Policy> policyPage;
		if (customerId != null && statusEnum != null) {
			policyPage = policyRepository.findByCustomerIdAndPolicyStatus(customerId, statusEnum, pageable);
		} else if (customerId != null) {
			policyPage = policyRepository.findByCustomerId(customerId, pageable);
		} else if (statusEnum != null) {
			policyPage = policyRepository.findByPolicyStatus(statusEnum, pageable);
		} else {
			policyPage = policyRepository.findAll(pageable);
		}

		List<PolicyResponseDTO> content = policyPage.getContent().stream().map(this::convertToResponseDTO).toList();

		return new PageResponseDTO<>(content, policyPage.getNumber(), policyPage.getSize(),
				policyPage.getTotalElements(), policyPage.getTotalPages(), policyPage.isLast(), sortDirection);
	}

	@Override
	public PageResponseDTO<PolicyResponseDTO> getCustomerPolicies(String email, int page, int size, String sortBy,
			String direction) {

		Customer customer = customerRepository.findByUserEmail(email)
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Policy> policyPage = policyRepository.findByCustomerId(customer.getId(), pageable);

		List<PolicyResponseDTO> content = policyPage.getContent().stream().map(this::convertToResponseDTO).toList();

		return new PageResponseDTO<>(content, policyPage.getNumber(), policyPage.getSize(),
				policyPage.getTotalElements(), policyPage.getTotalPages(), policyPage.isLast(), direction);
	}

	@Override
	public PageResponseDTO<PolicyResponseDTO> getPoliciesByCustomer(Long customerId, int page, int size, String sortBy,
			String direction) {

		Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Policy> policyPage = policyRepository.findByCustomerId(customerId, pageable);

		List<PolicyResponseDTO> content = policyPage.getContent().stream().map(this::convertToResponseDTO).toList();

		return new PageResponseDTO<>(content, policyPage.getNumber(), policyPage.getSize(),
				policyPage.getTotalElements(), policyPage.getTotalPages(), policyPage.isLast(), direction);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ApiResponseDTO<PolicyResponseDTO> cancelPolicy(Long policyId) {

		Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException(policyId));

		// Block cancellation if any claim is still open
		List<ClaimStatus> openStatuses = List.of(ClaimStatus.SUBMITTED, ClaimStatus.UNDER_REVIEW, ClaimStatus.RECOMMENDED_FOR_APPROVAL, ClaimStatus.RECOMMENDED_FOR_REJECTION);
		boolean hasOpenClaims = policy.getClaims().stream()
				.anyMatch(c -> openStatuses.contains(c.getClaimStatus()));
		if (hasOpenClaims) {
			throw new BadRequestException("Policy cannot be cancelled while a claim is still pending or under review.");
		}

		policy.setPolicyStatus(PolicyStatus.CANCELLED);

		Policy updatedPolicy = policyRepository.save(policy);

		PolicyResponseDTO responseDTO = convertToResponseDTO(updatedPolicy);

		return new ApiResponseDTO<>("Policy cancelled successfully", true, responseDTO, LocalDateTime.now());
	}

	private PolicyResponseDTO convertToResponseDTO(Policy policy) {

		PolicyResponseDTO dto = modelMapper.map(policy, PolicyResponseDTO.class);

		dto.setPolicyId(policy.getId());

		dto.setCustomerId(policy.getCustomer().getId());

			BigDecimal activeClaimsSum = claimRepository.sumActiveClaimsByPolicyId(policy.getId(), ClaimStatus.REJECTED);
			BigDecimal remaining = policy.getPolicyPlan().getCoverageAmount().subtract(activeClaimsSum);
			dto.setRemainingClaimAmount(remaining);

		dto.setCustomerName(policy.getCustomer().getUser().getFullName());

		dto.setPlanId(policy.getPolicyPlan().getId());

		dto.setPlanName(policy.getPolicyPlan().getPlanName());

		dto.setPolicyStatus(policy.getPolicyStatus().name());

		dto.setProductType(policy.getPolicyPlan().getInsuranceProduct().getProductType().name());
		dto.setCoverageAmount(policy.getPolicyPlan().getCoverageAmount());
		dto.setPremiumAmount(policy.getPolicyPlan().getPremiumAmount());
		dto.setPremiumType(policy.getPolicyPlan().getPremiumType().name());

		return dto;
	}

	private boolean isCustomerProfileComplete(Customer customer) {
		if (customer == null) return false;
		if (customer.getDateOfBirth() == null) return false;
		if (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) return false;
		if (customer.getCity() == null || customer.getCity().trim().isEmpty()) return false;
		if (customer.getState() == null || customer.getState().trim().isEmpty()) return false;
		if (customer.getPinCode() == null || customer.getPinCode().trim().isEmpty()) return false;
		if (customer.getNomineeName() == null || customer.getNomineeName().trim().isEmpty()) return false;
		if (customer.getNomineeRelation() == null || customer.getNomineeRelation().trim().isEmpty()) return false;
		return true;
	}

}