package com.insurance.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance.demo.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	Optional<Customer> findByUserEmail(String email);

	boolean existsByUserId(Long userId);

	@org.springframework.data.jpa.repository.Query("SELECT c FROM Customer c WHERE " +
			"(:city IS NULL OR LOWER(c.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
			"(:state IS NULL OR LOWER(c.state) LIKE LOWER(CONCAT('%', :state, '%')))")
	org.springframework.data.domain.Page<Customer> findByFilters(
			@org.springframework.data.repository.query.Param("city") String city,
			@org.springframework.data.repository.query.Param("state") String state,
			org.springframework.data.domain.Pageable pageable);
}