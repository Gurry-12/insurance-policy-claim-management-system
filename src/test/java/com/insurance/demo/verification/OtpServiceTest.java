package com.insurance.demo.verification;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.insurance.demo.enums.Role;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.model.OtpVerification;
import com.insurance.demo.repository.OtpVerificationRepository;

@ExtendWith(MockitoExtension.class)
public class OtpServiceTest {

    @Mock
    private OtpVerificationRepository otpRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private SmsService smsService;

    @InjectMocks
    private OtpService otpService;

    @Test
    void testCreateAndSendOtp_Staff_SendsWithIsStaffTrue() {
        // Arrange
        AppUser user = new AppUser();
        user.setEmail("staff@example.com");
        user.setMobileNumber("1234567890");
        user.setRole(Role.ROLE_INTERNAL_STAFF);

        when(otpRepository.save(any(OtpVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        otpService.createAndSendOtp(user);

        // Assert
        verify(emailService).sendOtp(eq("staff@example.com"), anyString(), eq(true));
        verify(smsService).sendOtp(eq("1234567890"), anyString(), eq(true));
    }

    @Test
    void testCreateAndSendOtp_Customer_SendsWithIsStaffFalse() {
        // Arrange
        AppUser user = new AppUser();
        user.setEmail("customer@example.com");
        user.setMobileNumber("0987654321");
        user.setRole(Role.ROLE_CUSTOMER);

        when(otpRepository.save(any(OtpVerification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        otpService.createAndSendOtp(user);

        // Assert
        verify(emailService).sendOtp(eq("customer@example.com"), anyString(), eq(false));
        verify(smsService).sendOtp(eq("0987654321"), anyString(), eq(false));
    }

    @Test
    void testSendOrResendOtp_Staff_ResendsWithIsStaffTrue() {
        // Arrange
        AppUser user = new AppUser();
        user.setEmail("staff@example.com");
        user.setMobileNumber("1234567890");
        user.setRole(Role.ROLE_INTERNAL_STAFF);

        when(otpRepository.getTotalOtpSendsSince(eq(user), any(LocalDateTime.class))).thenReturn(1);

        OtpVerification latestOtp = new OtpVerification();
        latestOtp.setEmailOtp("111111");
        latestOtp.setPhoneOtp("222222");
        latestOtp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        latestOtp.setUsed(false);

        when(otpRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(latestOtp));

        // Act
        otpService.sendOrResendOtp(user);

        // Assert
        verify(emailService).sendOtp(eq("staff@example.com"), eq("111111"), eq(true));
        verify(smsService).sendOtp(eq("1234567890"), eq("222222"), eq(true));
    }
}
