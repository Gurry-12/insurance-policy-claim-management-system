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
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.LoginResponseDTO;
import com.insurance.demo.dto.response.UserResponseDTO;
import com.insurance.demo.enums.Role;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.repository.AppUserRepository;
import com.insurance.demo.security.JwtService;
import com.insurance.demo.service.AuthService;
import com.insurance.demo.service.UserService;

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

	@Override
	public LoginResponseDTO login(LoginRequestDTO requestDto) {
		String email = requestDto.getEmail().toLowerCase();
		AppUser appUser = userRepository.findByEmail(email)
				.orElseThrow(() -> new BadRequestException("Invalid email or password"));

		if (Boolean.FALSE.equals(appUser.getIsActive())) {
			throw new BadRequestException("User is inactive can't login");
		}

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		
		String token = jwtService.generateToken(userDetails);

		UserResponseDTO dto = userService.findByEmail(userDetails.getUsername());
		
		log.info("JWT token generated successfully for email: {}", userDetails.getUsername());

		return new LoginResponseDTO(dto.getId(), dto.getFullName(), dto.getEmail(), dto.getRole(), token,
				"Jwt created", "Bearer");
	}

	@Override
	@Transactional
	public ApiResponseDTO<UserResponseDTO> registerUser(UserRequestDTO dto) {

		log.info("creating user by email: {}", dto.getEmail());
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new DuplicateResourceException("Duplicate user found with email - " + dto.getEmail());
		}
		AppUser user = modelMapper.map(dto, AppUser.class);
		user.setEmail(dto.getEmail().toLowerCase());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(Role.ROLE_CUSTOMER);
		user.setIsActive(true);

		AppUser savedUser = userRepository.save(user);
		UserResponseDTO responseDTO = modelMapper.map(savedUser, UserResponseDTO.class);
		return new ApiResponseDTO<>("User Created", true, responseDTO, LocalDateTime.now());

	}

}
