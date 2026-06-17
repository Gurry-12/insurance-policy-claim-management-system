package com.insurance.demo.serviceimpl;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.LoginRequestDTO;
import com.insurance.demo.dto.request.UserRequestDTO;
import com.insurance.demo.dto.request.VerifyOtpRequest;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.LoginResponseDTO;
import com.insurance.demo.dto.response.UserResponseDTO;
import com.insurance.demo.enums.Role;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.repository.AppUserRepository;
import com.insurance.demo.security.JwtService;
import com.insurance.demo.service.AuthService;
import com.insurance.demo.service.UserService;
import com.insurance.demo.verification.OtpService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final AuthenticationManager authenticationManager;
	private final AppUserRepository userRepository;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserService userService;
	private final OtpService otpService;

	@Override
	public LoginResponseDTO login(LoginRequestDTO requestDto) {

		log.info("Login attempt received. Email={}", requestDto.getEmail());
		String email = requestDto.getEmail().toLowerCase();
		AppUser appUser = userRepository.findByEmail(email).orElseThrow(() -> {
			log.warn("Login failed due to invalid credentials. Email={}", requestDto.getEmail());
			throw new BadRequestException("Invalid email or password");
		});

		if (!appUser.isEmailVerified()) {
			log.warn("Login blocked. Email not verified. UserId={}", appUser.getId());
			throw new BadRequestException("Please verify email before logging in.");
		}
		
		if (!appUser.isPhoneVerified()) {
			log.warn("Login blocked. Phone not verified. UserId={}", appUser.getId());
			throw new BadRequestException("Please verify phone before logging in.");
		}

		if (Boolean.FALSE.equals(appUser.getIsActive())) {
			log.warn("Login blocked. Inactive account. UserId={}", appUser.getId());
			throw new BadRequestException("Your account is deactivated. Please contact support.");
		}

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		String token = jwtService.generateToken(userDetails);

		UserResponseDTO dto = userService.findByEmail(userDetails.getUsername());

		log.info("User login successful. UserId={}, Role={}", appUser.getId(), appUser.getRole());

		return new LoginResponseDTO(dto.getId(), dto.getFullName(), dto.getEmail(), dto.getRole(), token,
				"User logged in successfully.", "Bearer");
	}

	@Override
	@Transactional
	public ApiResponseDTO<UserResponseDTO> registerUser(UserRequestDTO dto) {

		if (userRepository.existsByEmail(dto.getEmail())) {
			log.warn("Registration failed. Email already exists. Email={}", dto.getEmail());
			throw new DuplicateResourceException("Email Address is already registered.");
		}

		if (userRepository.existsByMobileNumber(dto.getMobileNumber())) {
			throw new DuplicateResourceException("Duplicate user found with mobile Number - " + dto.getMobileNumber());
		}
		AppUser user = modelMapper.map(dto, AppUser.class);
		user.setEmail(dto.getEmail().toLowerCase());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(Role.ROLE_CUSTOMER);
		user.setIsActive(false);
		user.setEmailVerified(false);
		user.setPhoneVerified(false);

		AppUser savedUser = userRepository.save(user);
		otpService.createAndSendOtp(savedUser);

		UserResponseDTO responseDTO = modelMapper.map(savedUser, UserResponseDTO.class);
		log.info("Customer registration successful. UserId={}, Email={}", user.getId(), user.getEmail());
		return new ApiResponseDTO<>("Customer registered successfully. OTP sent to email and phone.", true,
				responseDTO, LocalDateTime.now());

	}

	@Override
	public ApiResponseDTO<UserResponseDTO> verifyOtp(@Valid VerifyOtpRequest request) {
		AppUser user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found with the provided details."));

		if (Boolean.TRUE.equals(user.getIsActive())) {
			throw new BadRequestException("user is already verified");
		}

		otpService.verifyOtp(user, request.getEmailOtp(), request.getPhoneOtp());

		user.setEmailVerified(true);
        user.setPhoneVerified(true);
		user.setIsActive(true);

		AppUser saved = userRepository.save(user);

		return new ApiResponseDTO<>("User account activated successfully.", true, modelMapper.map(saved, UserResponseDTO.class),
				LocalDateTime.now());
	}

}
