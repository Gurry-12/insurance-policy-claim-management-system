package com.insurance.demo.serviceImpl;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.insurance.demo.dto.request.PolicyIssueRequestDTO;
import com.insurance.demo.dto.request.PolicyPurchaseRequestDTO;
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

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

	private final PolicyRepository policyRepository;

	private final PolicyPlanRepository policyPlanRepository;

	private final CustomerRepository customerRepository;

	private final ModelMapper modelMapper;

	@Override
	public PolicyResponseDTO purchasePolicy(PolicyPurchaseRequestDTO requestDTO, String customerEmail) {

		Customer customer = customerRepository.findByUserEmail(customerEmail)
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		PolicyPlan plan = policyPlanRepository.findByIdAndIsActiveTrue(requestDTO.getPlanId())
				.orElseThrow(PlanNotActiveException::new);

		Policy policy = new Policy();

		policy.setCustomer(customer);
		policy.setPolicyPlan(plan);

		policy.setPolicyNumber(PolicyNumberGenerator.generatePolicyNumber());

		policy.setStartDate(LocalDate.now());

		policy.setEndDate(LocalDate.now().plusMonths(plan.getDuration()));

		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);

		policy.setTotalPremiumPaid(0.0);

		Policy savedPolicy = policyRepository.save(policy);

		return convertToResponseDTO(savedPolicy);
	}

	@Override
	public PolicyResponseDTO issuePolicy(PolicyIssueRequestDTO requestDTO) {

		Customer customer = customerRepository.findById(requestDTO.getCustomerId())
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		PolicyPlan plan = policyPlanRepository.findByIdAndIsActiveTrue(requestDTO.getPlanId())
				.orElseThrow(PlanNotActiveException::new);

		Policy policy = new Policy();

		policy.setCustomer(customer);

		policy.setPolicyPlan(plan);

		policy.setPolicyNumber(PolicyNumberGenerator.generatePolicyNumber());

		policy.setStartDate(requestDTO.getStartDate());

		policy.setEndDate(requestDTO.getStartDate().plusMonths(plan.getDuration()));

		policy.setPolicyStatus(PolicyStatus.PENDING_PAYMENT);

		policy.setTotalPremiumPaid(0.0);

		Policy savedPolicy = policyRepository.save(policy);

		return convertToResponseDTO(savedPolicy);
	}

	@Override
	public Page<PolicyResponseDTO> getAllPolicies(int page, int size, String sortBy, String direction) {

		Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		return policyRepository.findAll(pageable).map(this::convertToResponseDTO);
	}

	@Override
	public Page<PolicyResponseDTO> getCustomerPolicies(String email, int page, int size, String sortBy,
			String direction) {

		Customer customer = customerRepository.findByUserEmail(email)
				.orElseThrow(() -> new RuntimeException("Customer not found"));

		Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		return policyRepository.findByCustomerId(customer.getId(), pageable).map(this::convertToResponseDTO);
	}

	@Override
	public Page<PolicyResponseDTO> getPoliciesByCustomer(Long customerId, int page, int size, String sortBy,
			String direction) {

		Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		return policyRepository.findByCustomerId(customerId, pageable).map(this::convertToResponseDTO);
	}

	@Override
	public PolicyResponseDTO activatePolicy(Long policyId) {

		Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException(policyId));

		policy.setPolicyStatus(PolicyStatus.ACTIVE);

		Policy updatedPolicy = policyRepository.save(policy);

		return convertToResponseDTO(updatedPolicy);
	}

	@Override
	public PolicyResponseDTO cancelPolicy(Long policyId) {

		Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new PolicyNotFoundException(policyId));

		policy.setPolicyStatus(PolicyStatus.CANCELLED);

		Policy updatedPolicy = policyRepository.save(policy);

		return convertToResponseDTO(updatedPolicy);
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