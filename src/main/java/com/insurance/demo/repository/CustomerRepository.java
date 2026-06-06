package com.insurance.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>{

}
