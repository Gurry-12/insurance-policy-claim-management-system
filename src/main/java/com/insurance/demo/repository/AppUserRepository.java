package com.insurance.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.enums.Role;
import com.insurance.demo.model.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	boolean existsByEmail(String email);

	Optional<AppUser> findByEmail(String email);
	
	boolean existsByMobileNumber(String mobileNumber);

	Optional<AppUser> findByEmailAndIsActiveTrue(String email);

	List<AppUser> findByRoleIn(List<Role> roles);
	
	List<AppUser> findByRoleNot(Role role);

	@org.springframework.data.jpa.repository.Query("SELECT u FROM AppUser u WHERE " +
			"(:role IS NULL OR u.role = :role) AND " +
			"(:isActive IS NULL OR u.isActive = :isActive)")
	org.springframework.data.domain.Page<AppUser> findByFilters(
			@org.springframework.data.repository.query.Param("role") Role role,
			@org.springframework.data.repository.query.Param("isActive") Boolean isActive,
			org.springframework.data.domain.Pageable pageable);
}
