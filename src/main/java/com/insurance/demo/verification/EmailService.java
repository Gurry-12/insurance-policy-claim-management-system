package com.insurance.demo.verification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	public void sendOtp(String toEmail, String otp) {

		if (!StringUtils.hasText(fromEmail)) {
			throw new IllegalStateException("Email service is not configured. Please set spring.mail.username.");
		}

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail.trim());
			message.setTo(toEmail);
			message.setSubject("User Registration Email OTP");
			message.setText("Dear User,\n\n" + "Your email verification OTP is: " + otp + "\n\n"
					+ "This OTP is valid for 5 minutes.\n\n" + "Regards,\n" + "Insurance Verification Team");

			mailSender.send(message);

		} catch (MailException ex) {
			Throwable rootCause = ex;

			while (rootCause.getCause() != null) {
				rootCause = rootCause.getCause();
			}

			rootCause.printStackTrace();

			throw new IllegalStateException("Unable to send email OTP. Root cause: "
					+ rootCause.getClass().getSimpleName() + " - " + rootCause.getMessage(), ex);
		}
	}
}