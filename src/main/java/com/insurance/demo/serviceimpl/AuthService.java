package com.insurance.demo.serviceImpl;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.UserRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.UserResponseDTO;
import com.insurance.demo.enums.Role;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements com.insurance.demo.service.AuthService {

	@Autowired
	private AuthenticationManager authenticationManager;
	private final AppUserRepository userRepository;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;

	public String verify(String username, String password) {

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));

		if (authentication.isAuthenticated()) {
			return "Login Successful";
		}

		return "Invalid Credentials";
	}

	@Transactional
	public ApiResponseDTO<UserResponseDTO> registerUser(UserRequestDTO dto) {

		log.info("creating user by email: {}", dto.getEmail());
		if (userRepository.existsByEmail(dto.getEmail())) {
			throw new DuplicateResourceException("Duplicate user found with email - " + dto.getEmail());
		}
		AppUser user = modelMapper.map(dto, AppUser.class);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setRole(Role.ROLE_COSTOMER);
		user.setIsActive(true);

		AppUser savedUser = userRepository.save(user);
		UserResponseDTO responseDTO = modelMapper.map(savedUser, UserResponseDTO.class);
		return new ApiResponseDTO<>("User Created", true, responseDTO, LocalDateTime.now());

	}
}
