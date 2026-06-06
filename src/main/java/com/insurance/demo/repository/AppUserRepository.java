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

	Optional<AppUser> findByEmailAndIsActiveTrue(String email);

	List<AppUser> findByRoleIn(List<Role> roles);
	
	List<AppUser> findByRoleNot(Role role);
}
