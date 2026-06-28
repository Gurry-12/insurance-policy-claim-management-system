package com.insurance.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.AgentSpeciality;
import com.insurance.demo.model.AppUser;

@Repository
public interface AgentSpecialityRepository extends JpaRepository<AgentSpeciality, Long> {
	AgentSpeciality findByAgent(AppUser agent);
}