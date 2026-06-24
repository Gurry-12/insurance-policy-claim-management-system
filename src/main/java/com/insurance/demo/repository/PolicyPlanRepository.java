package com.insurance.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.PolicyPlan;

@Repository
public interface PolicyPlanRepository extends JpaRepository<PolicyPlan, Long> {


	Optional<PolicyPlan> findByIdAndIsActiveTrue(Long id);

	boolean existsByPlanNameIgnoreCase(String planName);

	List<PolicyPlan> findByIsActiveTrue();

	List<PolicyPlan> findByInsuranceProductIdAndIsActiveTrue(Long productId);

	Page<PolicyPlan> findByIsActiveTrue(Pageable pageable);

	Page<PolicyPlan> findByInsuranceProductIdAndIsActiveTrue(Long productId, Pageable pageable);

	Page<PolicyPlan> findByInsuranceProductIdAndIsActive(Long productId, Boolean isActive, Pageable pageable);

	Page<PolicyPlan> findByInsuranceProductId(Long productId, Pageable pageable);

	Page<PolicyPlan> findByIsActive(Boolean isActive, Pageable pageable);

}

