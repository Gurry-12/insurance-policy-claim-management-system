package com.insurance.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.InsuranceProduct;

@Repository
public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, Long>{

	boolean existsByProductNameIgnoreCase(String productName);

	Optional<InsuranceProduct> findByProductNameIgnoreCase(String productName);

	List<InsuranceProduct> findByIsActiveTrue();

	@org.springframework.data.jpa.repository.Query("SELECT p FROM InsuranceProduct p WHERE " +
			"(:productType IS NULL OR p.productType = :productType) AND " +
			"(:isActive IS NULL OR p.isActive = :isActive)")
	org.springframework.data.domain.Page<InsuranceProduct> findByFilters(
			@org.springframework.data.repository.query.Param("productType") com.insurance.demo.enums.ProductType productType,
			@org.springframework.data.repository.query.Param("isActive") Boolean isActive,
			org.springframework.data.domain.Pageable pageable);
}
