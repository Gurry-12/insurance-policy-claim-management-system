package com.insurence.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.AppUser;
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long>{

}
