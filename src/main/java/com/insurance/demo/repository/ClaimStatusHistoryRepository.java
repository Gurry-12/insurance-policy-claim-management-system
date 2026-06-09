package com.insurance.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.ClaimStatusHistory;

@Repository
public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistory, Long>{

	@org.springframework.data.jpa.repository.Query("SELECT h FROM ClaimStatusHistory h WHERE " +
			"h.claim.id = :claimId AND " +
			"(:updatedBy IS NULL OR LOWER(h.updatedBy) LIKE LOWER(CONCAT('%', :updatedBy, '%'))) AND " +
			"(:status IS NULL OR h.newStatus = :status)")
	org.springframework.data.domain.Page<ClaimStatusHistory> findByFilters(
			@org.springframework.data.repository.query.Param("claimId") Long claimId,
			@org.springframework.data.repository.query.Param("updatedBy") String updatedBy,
			@org.springframework.data.repository.query.Param("status") String status,
			org.springframework.data.domain.Pageable pageable);
}
