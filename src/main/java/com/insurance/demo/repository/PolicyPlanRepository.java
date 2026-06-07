package com.insurance.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import com.insurance.demo.model.PolicyPlan;

@Repository
public interface PolicyPlanRepository extends JpaRepository<PolicyPlan, Long> {

	Optional<PolicyPlan> findByIdAndIsActiveTrue(Long id);
}