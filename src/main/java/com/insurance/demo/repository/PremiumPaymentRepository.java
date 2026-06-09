package com.insurance.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.Policy;
import com.insurance.demo.model.PremiumPayment;

@Repository
public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long>{
	
	boolean existsByTransactionReference(String transactionReference);

	List<PremiumPayment> findByPolicyId(Long id);

	List<PremiumPayment> findByPolicyCustomerUserId(Long userId);

	List<PremiumPayment> findByPolicyIdAndPolicyCustomerUserId(Long policyId, Long userId);

	@org.springframework.data.jpa.repository.Query("SELECT p FROM PremiumPayment p WHERE " +
			"(:policyId IS NULL OR p.policy.id = :policyId) AND " +
			"(:paymentStatus IS NULL OR p.paymentStatus = :paymentStatus)")
	org.springframework.data.domain.Page<PremiumPayment> findByFilters(
			@org.springframework.data.repository.query.Param("policyId") Long policyId,
			@org.springframework.data.repository.query.Param("paymentStatus") com.insurance.demo.enums.PaymentStatus paymentStatus,
			org.springframework.data.domain.Pageable pageable);
}
