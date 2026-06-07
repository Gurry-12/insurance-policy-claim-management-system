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

}
