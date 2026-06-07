package com.insurance.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.InsuranceProduct;

@Repository
public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, Long>{

}
