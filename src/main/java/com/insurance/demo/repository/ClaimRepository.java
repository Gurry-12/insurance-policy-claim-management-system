package com.insurance.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

import com.insurance.demo.enums.ClaimStatus;
import com.insurance.demo.enums.ProductType;
import com.insurance.demo.model.Claim;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long>{

	Optional<Claim> findByClaimNumber(String claimNumber);

    // Security: Customer can only access their own claims
    Optional<Claim> findByIdAndPolicyCustomerUserId(Long claimId, Long userId);

    List<Claim> findByPolicyCustomerUserId(Long customerUserId);
    Page<Claim> findByPolicyCustomerUserId(Long customerUserId, Pageable pageable);

    Page<Claim> findByClaimStatus(ClaimStatus status, Pageable pageable);
    Page<Claim> findByPolicyCustomerId(Long customerId, Pageable pageable);

	Page<Claim> findByPolicyCustomerIdAndClaimStatus(Long customerId, ClaimStatus status, Pageable pageable);

	Page<Claim> findByAssignedStaffId(Long staffId, Pageable pageable);

	Page<Claim> findByAssignedStaffIdAndClaimStatus(Long staffId, ClaimStatus status, Pageable pageable);

	Page<Claim> findByPolicyPolicyPlanInsuranceProductProductType(ProductType productType, Pageable pageable);

	Page<Claim> findByPolicyPolicyPlanInsuranceProductProductTypeAndClaimStatus(ProductType productType, ClaimStatus status, Pageable pageable);

	@Query("SELECT SUM(c.claimAmount) FROM Claim c WHERE c.policy.id = :policyId AND c.claimStatus != :status")
	BigDecimal sumActiveClaimsByPolicyId(@Param("policyId") Long policyId, @Param("status") ClaimStatus status);
}
