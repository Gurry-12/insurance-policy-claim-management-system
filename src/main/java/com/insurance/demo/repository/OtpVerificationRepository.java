package com.insurance.demo.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.insurance.demo.model.AppUser;
import com.insurance.demo.model.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByUserAndUsedFalseOrderByCreatedAtDesc(AppUser user);
}
