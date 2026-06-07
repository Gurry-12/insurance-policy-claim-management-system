package com.insurance.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.model.Policy;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

	boolean existsByPolicyNumber(String policyNumber);

	Page<Policy> findByCustomerId(Long customerId, Pageable pageable);

	Page<Policy> findByPolicyStatus(PolicyStatus policyStatus, Pageable pageable);

	Page<Policy> findByCustomerIdAndPolicyStatus(Long customerId, PolicyStatus policyStatus, Pageable pageable);
}


