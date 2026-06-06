package com.insurance.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.PolicyPlan;
@Repository
public interface PolicyPlanRepository extends JpaRepository<PolicyPlan, Long>{

}
