package com.insurance.demo.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.PlanRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PlanResponseDTO;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.InsuranceProduct;
import com.insurance.demo.model.PolicyPlan;
import com.insurance.demo.repository.InsuranceProductRepository;
import com.insurance.demo.repository.PolicyPlanRepository;
import com.insurance.demo.service.PolicyPlanService;
import com.insurance.demo.util.PaginationValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyPlanServiceImpl implements PolicyPlanService {

	private final PolicyPlanRepository policyPlanRepository;
	private final InsuranceProductRepository productRepository;
	private final ModelMapper modelMapper;

	@Override
	@Transactional
	public ApiResponseDTO<PlanResponseDTO> createPolicyPlan(PlanRequestDTO dto) {

		log.info("Creating policy plan: {}", dto.getPlanName());

		if (dto.getCoverageAmount().compareTo(dto.getPremiumAmount()) <= 0) {
			throw new BadRequestException("The policy coverage amount must strictly exceed the required premium amount.");
		}

		// Validate Product exists and is active
		InsuranceProduct product = productRepository.findById(dto.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

		if (!Boolean.TRUE.equals(product.getIsActive())) {
			throw new BadRequestException("Cannot create a policy plan under an inactive insurance product.");
		}

		// Check duplicate plan name
		if (policyPlanRepository.existsByPlanNameIgnoreCase(dto.getPlanName())) {
			throw new DuplicateResourceException("Policy plan already exists with name: " + dto.getPlanName());
		}

		PolicyPlan plan = new PolicyPlan();

		plan.setPlanName(dto.getPlanName().toLowerCase());
		plan.setCoverageAmount(dto.getCoverageAmount());
		plan.setPremiumAmount(dto.getPremiumAmount());
		plan.setPremiumType(dto.getPremiumType());
		plan.setDuration(dto.getDuration());
		plan.setTermsAndConditions(dto.getTermsAndConditions());

		plan.setInsuranceProduct(product);
		plan.setIsActive(dto.getActiveStatus() != null ? dto.getActiveStatus() : true);

		PolicyPlan savedPlan = policyPlanRepository.save(plan);

		log.info("Plan ID after save: {}", savedPlan.getId());

		PlanResponseDTO responseDTO = modelMapper.map(savedPlan, PlanResponseDTO.class);
		return new ApiResponseDTO<>("Policy Plan created successfully", true, responseDTO, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<PlanResponseDTO> updatePolicyPlan(Long planId, PlanRequestDTO dto) {

		log.info("Updating policy plan with id: {}", planId);

		if (dto.getCoverageAmount().compareTo(dto.getPremiumAmount()) <= 0) {
			throw new BadRequestException("Cannot create a policy plan under an inactive insurance product.");
		}

		PolicyPlan existingPlan = policyPlanRepository.findById(planId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy plan not found with id: " + planId));

		if (!Boolean.TRUE.equals(existingPlan.getIsActive())) {
			throw new BadRequestException("Cannot update an inactive policy plan. Please activate it first.");
		}

		// Validate product if changed
		if (!existingPlan.getInsuranceProduct().getId().equals(dto.getProductId())) {
			InsuranceProduct newProduct = productRepository.findById(dto.getProductId()).orElseThrow(
					() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

			if (!Boolean.TRUE.equals(newProduct.getIsActive())) {
				throw new BadRequestException("Cannot link a policy plan to an inactive insurance product.");
			}
			existingPlan.setInsuranceProduct(newProduct);
		}

		// Check duplicate name (excluding self)
		if (!existingPlan.getPlanName().equalsIgnoreCase(dto.getPlanName())
				&& policyPlanRepository.existsByPlanNameIgnoreCase(dto.getPlanName())) {
			throw new DuplicateResourceException("Plan name already exists: " + dto.getPlanName());
		}

		// Update fields
		existingPlan.setPlanName(dto.getPlanName().toLowerCase());
		existingPlan.setCoverageAmount(dto.getCoverageAmount());
		existingPlan.setPremiumAmount(dto.getPremiumAmount());
		existingPlan.setPremiumType(dto.getPremiumType());
		existingPlan.setDuration(dto.getDuration());
		existingPlan.setTermsAndConditions(dto.getTermsAndConditions());
		if (dto.getActiveStatus() != null) {
			existingPlan.setIsActive(dto.getActiveStatus());
		}

		PolicyPlan updatedPlan = policyPlanRepository.save(existingPlan);

		PlanResponseDTO responseDTO = modelMapper.map(updatedPlan, PlanResponseDTO.class);
		return new ApiResponseDTO<>("Policy Plan updated successfully", true, responseDTO, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<PlanResponseDTO> deactivatePolicyPlan(Long planId) {

		log.info("Deactivating policy plan id: {}", planId);

		PolicyPlan plan = policyPlanRepository.findById(planId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy plan not found with id: " + planId));

		if (Boolean.FALSE.equals(plan.getIsActive())) {
			PlanResponseDTO dto = modelMapper.map(plan, PlanResponseDTO.class);
			return new ApiResponseDTO<>("The policy plan is already marked as inactive", false, dto, LocalDateTime.now());
		}
		

		plan.setIsActive(false);
		PolicyPlan deactivatedPlan = policyPlanRepository.save(plan);

		PlanResponseDTO responseDTO = modelMapper.map(deactivatedPlan, PlanResponseDTO.class);
		return new ApiResponseDTO<>("Policy plan deactivated successfully", true, responseDTO, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<PlanResponseDTO> activatePolicyPlan(Long planId) {

		log.info("Activating policy plan id: {}", planId);

		PolicyPlan plan = policyPlanRepository.findById(planId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy plan not found with id: " + planId));

		if (Boolean.TRUE.equals(plan.getIsActive())) {
			PlanResponseDTO dto = modelMapper.map(plan, PlanResponseDTO.class);
			return new ApiResponseDTO<>("The policy plan is already marked as active", false, dto, LocalDateTime.now());
		}

		plan.setIsActive(true);
		PolicyPlan activatedPlan = policyPlanRepository.save(plan);

		PlanResponseDTO responseDTO = modelMapper.map(activatedPlan, PlanResponseDTO.class);
		return new ApiResponseDTO<>("Policy plan activated successfully", true, responseDTO, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<PlanResponseDTO>> viewActivePlans() {

		List<PolicyPlan> plans = policyPlanRepository.findByIsActiveTrue();

		List<PlanResponseDTO> responseList = plans.stream().map(plan -> modelMapper.map(plan, PlanResponseDTO.class))
				.toList();

		return new ApiResponseDTO<>("Active policy plans retrieved successfully", true, responseList,
				LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<PlanResponseDTO>> viewActivePlansUnderInsuranceProduct(Long productId) {

		List<PolicyPlan> plans = policyPlanRepository.findByInsuranceProductIdAndIsActiveTrue(productId);

		List<PlanResponseDTO> responseList = plans.stream().map(plan -> modelMapper.map(plan, PlanResponseDTO.class))
				.toList();

		return new ApiResponseDTO<>("Active plans under product retrieved successfully", true, responseList,
				LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<PlanResponseDTO> getAllPlansWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection, Long productId, Boolean isActive) {

		log.info("Fetching policy plans with pagination: page={}, size={}, sortBy={}, direction={}, productId={}, active={}", pageNumber,
				pageSize, sortBy, sortDirection, productId, isActive);

		PaginationValidator.validate(pageNumber, pageSize);
		PaginationValidator.validateSortField(sortBy, Set.of("id", "planName", "coverageAmount", "premiumAmount", "createdDate"));

		Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

		Page<PolicyPlan> planPage;
		if (productId != null && isActive != null) {
			planPage = policyPlanRepository.findByInsuranceProductIdAndIsActive(productId, isActive, pageable);
		} else if (productId != null) {
			planPage = policyPlanRepository.findByInsuranceProductId(productId, pageable);
		} else if (isActive != null) {
			planPage = policyPlanRepository.findByIsActive(isActive, pageable);
		} else {
			planPage = policyPlanRepository.findAll(pageable);
		}

		List<PlanResponseDTO> content = planPage.getContent().stream()
				.map(plan -> modelMapper.map(plan, PlanResponseDTO.class)).toList();

		return new PageResponseDTO<>(content, planPage.getNumber(), planPage.getSize(), planPage.getTotalElements(),
				planPage.getTotalPages(), planPage.isLast(), sortDirection);
	}


	@Override
	public ApiResponseDTO<PlanResponseDTO> getPlanById(Long planId) {

		PolicyPlan plan = policyPlanRepository.findById(planId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy plan not found with id: " + planId));

		org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean isCustomer = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

		if (isCustomer && !Boolean.TRUE.equals(plan.getIsActive())) {
			throw new ResourceNotFoundException("No active plan associated with id - " + planId);
		}

		return new ApiResponseDTO<>("Policy plan retrieved successfully.", true, modelMapper.map(plan, PlanResponseDTO.class),
				LocalDateTime.now());

	}
}