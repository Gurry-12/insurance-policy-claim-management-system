package com.insurence.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.Policy;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long>{

}
