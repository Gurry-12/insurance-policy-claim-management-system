package com.insurance.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.enums.Role;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.model.InsuranceProduct;

@Repository
public interface InsurenceProductRepository extends JpaRepository<InsuranceProduct, Long>{

	boolean existsByProductNameIgnoreCase(String productName);

	Optional<InsuranceProduct> findByProductNameIgnoreCase(String productName);


	List<InsuranceProduct> findByIsActiveTrue();
	

}
