package com.insurance.demo.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.CoverageOptionRequestDTO;
import com.insurance.demo.dto.request.CoverageRegenerationRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.CoverageOption;
import com.insurance.demo.model.PolicyPlan;
import com.insurance.demo.repository.CoverageOptionRepository;
import com.insurance.demo.repository.PolicyPlanRepository;
import com.insurance.demo.service.CoverageOptionService;
import com.insurance.demo.util.MessageConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoverageOptionServiceImpl implements CoverageOptionService {

	private final CoverageOptionRepository coverageOptionRepository;
	private final PolicyPlanRepository policyPlanRepository;

	@Override
	@Transactional
	public ApiResponseDTO<CoverageOption> createCoverageOption(Long planId, CoverageOptionRequestDTO dto) {
		log.info("Creating coverage option for plan id: {}", planId);

		PolicyPlan plan = policyPlanRepository.findById(planId)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.PolicyPlan.NOT_FOUND + planId));

		CoverageOption option = new CoverageOption();
		option.setPolicyPlan(plan);
		option.setCoverageAmount(dto.getCoverageAmount());
		option.setLabel(dto.getLabel());
		option.setDisplayOrder(dto.getDisplayOrder());
		option.setIsActive(dto.getActiveStatus() != null ? dto.getActiveStatus() : true);

		CoverageOption savedOption = coverageOptionRepository.save(option);

		return new ApiResponseDTO<>("Coverage option created successfully", true, savedOption, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<CoverageOption> updateCoverageOption(Long planId, Long optionId, CoverageOptionRequestDTO dto) {
		log.info("Updating coverage option id: {} for plan id: {}", optionId, planId);

		CoverageOption option = coverageOptionRepository.findById(optionId)
				.orElseThrow(() -> new ResourceNotFoundException("Coverage option not found with id: " + optionId));

		if (!option.getPolicyPlan().getId().equals(planId)) {
			throw new BadRequestException("Coverage option does not belong to the specified plan");
		}

		option.setCoverageAmount(dto.getCoverageAmount());
		option.setLabel(dto.getLabel());
		option.setDisplayOrder(dto.getDisplayOrder());
		if (dto.getActiveStatus() != null) {
			option.setIsActive(dto.getActiveStatus());
		}

		CoverageOption updatedOption = coverageOptionRepository.save(option);

		return new ApiResponseDTO<>("Coverage option updated successfully", true, updatedOption, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<CoverageOption>> getCoverageOptions(Long planId) {
		
		if (!policyPlanRepository.existsById(planId)) {
			throw new ResourceNotFoundException(MessageConstants.PolicyPlan.NOT_FOUND + planId);
		}
		
		List<CoverageOption> options = coverageOptionRepository.findByPolicyPlanId(planId);
		
		return new ApiResponseDTO<>("Coverage options retrieved successfully", true, options, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<CoverageOption> activateCoverageOption(Long planId, Long optionId) {
		CoverageOption option = coverageOptionRepository.findById(optionId)
				.orElseThrow(() -> new ResourceNotFoundException("Coverage option not found with id: " + optionId));

		if (!option.getPolicyPlan().getId().equals(planId)) {
			throw new BadRequestException("Coverage option does not belong to the specified plan");
		}

		option.setIsActive(true);
		CoverageOption updatedOption = coverageOptionRepository.save(option);

		return new ApiResponseDTO<>("Coverage option activated successfully", true, updatedOption, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<CoverageOption> deactivateCoverageOption(Long planId, Long optionId) {
		CoverageOption option = coverageOptionRepository.findById(optionId)
				.orElseThrow(() -> new ResourceNotFoundException("Coverage option not found with id: " + optionId));

		if (!option.getPolicyPlan().getId().equals(planId)) {
			throw new BadRequestException("Coverage option does not belong to the specified plan");
		}

		option.setIsActive(false);
		CoverageOption updatedOption = coverageOptionRepository.save(option);

		return new ApiResponseDTO<>("Coverage option deactivated successfully", true, updatedOption, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<Void> deleteCoverageOption(Long planId, Long optionId) {
		CoverageOption option = coverageOptionRepository.findById(optionId)
				.orElseThrow(() -> new ResourceNotFoundException("Coverage option not found with id: " + optionId));

		if (!option.getPolicyPlan().getId().equals(planId)) {
			throw new BadRequestException("Coverage option does not belong to the specified plan");
		}

		coverageOptionRepository.delete(option);

		return new ApiResponseDTO<>("Coverage option deleted successfully", true, null, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<List<CoverageOption>> regenerateCoverageOptions(Long planId, CoverageRegenerationRequestDTO dto) {
		log.info("Regenerating coverage options for plan id: {} with min: {}, max: {}, step: {}", 
				planId, dto.getMinCoverage(), dto.getMaxCoverage(), dto.getIncrementStep());

		PolicyPlan plan = policyPlanRepository.findById(planId)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.PolicyPlan.NOT_FOUND + planId));

		// Validate input
		if (dto.getMinCoverage().compareTo(dto.getMaxCoverage()) >= 0) {
			throw new BadRequestException("Minimum coverage must be less than maximum coverage");
		}
		if (dto.getIncrementStep().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BadRequestException("Increment step must be positive");
		}

		// Delete existing coverage options for this plan
		List<CoverageOption> existingOptions = coverageOptionRepository.findByPolicyPlanId(planId);
		coverageOptionRepository.deleteAll(existingOptions);
		log.info("Deleted {} existing coverage options for plan id: {}", existingOptions.size(), planId);

		// Generate new coverage options
		List<CoverageOption> newOptions = new ArrayList<>();
		BigDecimal currentAmount = dto.getMinCoverage();
		BigDecimal lakhs = BigDecimal.valueOf(100000);
		int displayOrder = 1;

		while (currentAmount.compareTo(dto.getMaxCoverage()) <= 0) {
			CoverageOption option = new CoverageOption();
			option.setPolicyPlan(plan);
			option.setCoverageAmount(currentAmount);
			BigDecimal inLakhs = currentAmount.divide(lakhs, 2, RoundingMode.HALF_UP);
			option.setLabel("₹" + inLakhs + " Lakhs");
			option.setDisplayOrder(displayOrder);
			option.setIsActive(true);

			newOptions.add(option);
			currentAmount = currentAmount.add(dto.getIncrementStep());
			displayOrder++;
		}

		List<CoverageOption> savedOptions = coverageOptionRepository.saveAll(newOptions);
		log.info("Created {} new coverage options for plan id: {}", savedOptions.size(), planId);

		return new ApiResponseDTO<>("Coverage options regenerated successfully", true, savedOptions, LocalDateTime.now());
	}

}
