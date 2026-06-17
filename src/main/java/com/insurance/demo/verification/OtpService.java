package com.insurance.demo.verification;


import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.model.OtpVerification;
import com.insurance.demo.repository.OtpVerificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiry-minutes}")
    private long expiryMinutes;

    @Transactional
    public void createAndSendOtp(AppUser user) {
        String emailOtp = generateSixDigitOtp();
        String phoneOtp = generateSixDigitOtp();

        OtpVerification otpVerification = OtpVerification.builder()
                .user(user)
                .emailOtp(emailOtp)
                .phoneOtp(phoneOtp)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .used(false)
                .build();

        otpRepository.save(otpVerification);
        emailService.sendOtp(user.getEmail(), emailOtp);
        smsService.sendOtp(user.getMobileNumber(), phoneOtp);
    }

    @Transactional
    public void verifyOtp(AppUser user, String emailOtp, String phoneOtp) {
        OtpVerification latestOtp = otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("No active OTP found. Please register again."));

        if (latestOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired. Please register again to get a new OTP.");
        }

        if (!latestOtp.getEmailOtp().equals(emailOtp)) {
            throw new BadRequestException("Invalid email OTP");
        }

//        if (!latestOtp.getPhoneOtp().equals(phoneOtp)) {
//            throw new BadRequestException("Invalid phone OTP");
//        }

        latestOtp.setUsed(true);
        otpRepository.save(latestOtp);
    }

    private String generateSixDigitOtp() {
        int number = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(number);
    }
}
