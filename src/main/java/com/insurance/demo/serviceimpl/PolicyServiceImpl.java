package com.insurance.demo.serviceimpl;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.insurance.demo.dto.request.PolicyIssueRequestDTO;
import com.insurance.demo.dto.request.PolicyPurchaseRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PolicyResponseDTO;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.exception.PlanNotActiveException;
import com.insurance.demo.exception.PolicyNotFoundException;
import com.insurance.demo.model.Customer;
import com.insurance.demo.model.Policy;
import com.insurance.demo.model.PolicyPlan;
import com.insurance.demo.repository.CustomerRepository;
import com.insurance.demo.repository.PolicyPlanRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.service.PolicyService;
import com.insurance.demo.util.PolicyNumberGenerator;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import com.insurance.demo.dto.response.PageResponseDTO;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

	private final PolicyRepository policyRepository;

	private final PolicyPlanRepository policyPlanRepository;

	private final CustomerRepository customerRepository;

	private final ModelMapper modelMapper;

	@Override
	public ApiResponseDTO<PolicyResponseDTO> purchasePolicy(PolicyPurchaseRequestDTO requestDTO, String customerEmail) {

		Customer customer = customerRepository.findByUserEmail(customerEmail)
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		PolicyPlan plan = policyPlanRepository.findByIdAndIsActiveTrue(requestDTO.getPlanId())
				.orElseThrow(PlanNotActiveException::new);

		Policy policy = new Policy();

		policy.setCustomer(customer);
		policy.setPolicyPlan(plan);

		policy.setPolicyNumber(PolicyNumberGenerator.generatePolicyNumber());

		policy.setStartDate(LocalDate.now());

		policy.setEndDate(LocalDate.now().plusYears(plan.getDuration()));

		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);

		policy.setTotalPremiumPaid(0.0);

		Policy savedPolicy = policyRepository.save(policy);

		PolicyResponseDTO responseDTO = convertToResponseDTO(savedPolicy);

		return new ApiResponseDTO<>("Policy purchased successfully", true, responseDTO, LocalDateTime.now());
	}

	@Override
	public ApiResponseDTO<PolicyResponseDTO> issuePolicy(PolicyIssueRequestDTO requestDTO) {

		Customer customer = customerRepository.findById(requestDTO.getCustomerId())
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		PolicyPlan plan = policyPlanRepository.findByIdAndIsActiveTrue(requestDTO.getPlanId())
				.orElseThrow(PlanNotActiveException::new);

		Policy policy = new Policy();

		policy.setCustomer(customer);
		policy.setPolicyPlan(plan);

		policy.setPolicyNumber(PolicyNumberGenerator.generatePolicyNumber());

		policy.setStartDate(requestDTO.getStartDate());

		policy.setEndDate(requestDTO.getStartDate().plusYears(plan.getDuration()));

		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);

		policy.setTotalPremiumPaid(0.0);

		Policy savedPolicy = policyRepository.save(policy);

		PolicyResponseDTO responseDTO = convertToResponseDTO(savedPolicy);

		return new ApiResponseDTO<>("Policy issued successfully", true, responseDTO, LocalDateTime.now());
	}

	@Override
	public PageResponseDTO<PolicyResponseDTO> getAllPolicies(int page, int size, String sortBy, String direction) {

		Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Policy> policyPage = policyRepository.findAll(pageable);

		List<PolicyResponseDTO> content = policyPage.getContent().stream().map(this::convertToResponseDTO).toList();

		return new PageResponseDTO<>(content, policyPage.getNumber(), policyPage.getSize(),
				policyPage.getTotalElements(), policyPage.getTotalPages(), policyPage.isLast(), direction);
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
	public ApiResponseDTO<PolicyResponseDTO> activatePolicy(Long policyId) {

		Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException(policyId));

		policy.setPolicyStatus(PolicyStatus.ACTIVE);

		Policy updatedPolicy = policyRepository.save(policy);

		PolicyResponseDTO responseDTO = convertToResponseDTO(updatedPolicy);

		return new ApiResponseDTO<>("Policy activated successfully", true, responseDTO, LocalDateTime.now());
	}

	
	
	@Override
	public ApiResponseDTO<PolicyResponseDTO> cancelPolicy(Long policyId) {

		Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException(policyId));

		policy.setPolicyStatus(PolicyStatus.CANCELLED);

		Policy updatedPolicy = policyRepository.save(policy);

		PolicyResponseDTO responseDTO = convertToResponseDTO(updatedPolicy);

		return new ApiResponseDTO<>("Policy cancelled successfully", true, responseDTO, LocalDateTime.now());
	}

	
	private PolicyResponseDTO convertToResponseDTO(Policy policy) {

		PolicyResponseDTO dto = modelMapper.map(policy, PolicyResponseDTO.class);

		dto.setPolicyId(policy.getId());

		dto.setCustomerId(policy.getCustomer().getId());

		dto.setCustomerName(policy.getCustomer().getUser().getFullName());

		dto.setPlanId(policy.getPolicyPlan().getId());

		dto.setPlanName(policy.getPolicyPlan().getPlanName());

		dto.setPolicyStatus(policy.getPolicyStatus().name());

		return dto;
	}
}