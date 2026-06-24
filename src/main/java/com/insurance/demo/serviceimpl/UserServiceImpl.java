package com.insurance.demo.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.CreateAgentRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.UserResponseDTO;
import com.insurance.demo.enums.Role;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.repository.AppUserRepository;
import com.insurance.demo.service.UserService;
import com.insurance.demo.verification.OtpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final AppUserRepository userRepository;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;
	private final OtpService otpService;

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<UserResponseDTO>> viewAllUsers() {

		log.info("fatching all users");
		List<AppUser> users = userRepository.findAll();

		List<UserResponseDTO> userResponseDTOs = users.stream()
				.map(user -> modelMapper.map(user, UserResponseDTO.class)).toList();

		ApiResponseDTO<List<UserResponseDTO>> apiResponseDTO = new ApiResponseDTO<>();

		apiResponseDTO.setData(userResponseDTOs);
		apiResponseDTO.setMessage("Get All Users");
		apiResponseDTO.setSuccess(true);
		apiResponseDTO.setTimeStamp(LocalDateTime.now());
		return apiResponseDTO;
	}

	@Override
	@Transactional
	public ApiResponseDTO<UserResponseDTO> activateUser(Long userId) {

		log.info("Activating user by id: {}", userId);

		if (userId.equals(currentUserId()))
			throw new AccessDeniedException("You cannot manually activate your own account.");

		AppUser user = getById(userId);

		if (Boolean.FALSE.equals(user.isEmailVerified())) {
			UserResponseDTO responseDto = modelMapper.map(user, UserResponseDTO.class);
			log.info("User is not verified by id: {}", userId);
			return new ApiResponseDTO<>("User is not verified", false, responseDto, LocalDateTime.now());
		}

		if (Boolean.TRUE.equals(user.getIsActive())) {
			UserResponseDTO responseDto = modelMapper.map(user, UserResponseDTO.class);
			log.info("user already active with id {} ", userId);
			return new ApiResponseDTO<>("User Already Active", false, responseDto, LocalDateTime.now());
		}

		user.setIsActive(true);

		AppUser retrivedUser = userRepository.save(user);

		UserResponseDTO responseDto = modelMapper.map(retrivedUser, UserResponseDTO.class);
		return new ApiResponseDTO<>("User Activated successfully", true, responseDto, LocalDateTime.now());
	}

	private Long currentUserId() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String email = authentication.getName();

		return findByEmail(email).getId();
	}

	@Override
	@Transactional
	public ApiResponseDTO<UserResponseDTO> deactivateUser(Long userId) {

		log.info("Deactivating user by id: {}", userId);

		if (userId.equals(currentUserId()))
			throw new AccessDeniedException("Cannot deactivate your own account");

		AppUser user = getById(userId);

		if (Boolean.FALSE.equals(user.isEmailVerified())) {
			UserResponseDTO responseDto = modelMapper.map(user, UserResponseDTO.class);
			log.info("User is not verified by id: {}", userId);
			return new ApiResponseDTO<>("User is not verified", false, responseDto, LocalDateTime.now());
		}

		if (Boolean.FALSE.equals(user.getIsActive())) {
			UserResponseDTO responseDto = modelMapper.map(user, UserResponseDTO.class);
			log.info("Already deactivated user by id: {}", userId);
			return new ApiResponseDTO<>("User Already Deactivated", false, responseDto, LocalDateTime.now());
		}

		user.setIsActive(false);

		AppUser retrivedUser = userRepository.save(user);

		UserResponseDTO responseDto = modelMapper.map(retrivedUser, UserResponseDTO.class);
		return new ApiResponseDTO<>("User Deactivated successfully", true, responseDto, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<UserResponseDTO> createAgentUser(CreateAgentRequestDTO agentRequestDTO) {

		log.info("creating agent by email: {}", agentRequestDTO.getEmail());

		if (userRepository.existsByEmail(agentRequestDTO.getEmail())) {
			throw new DuplicateResourceException("Duplicate user found with email - " + agentRequestDTO.getEmail());
		}

		if (userRepository.existsByMobileNumber(agentRequestDTO.getMobileNumber())) {
			throw new DuplicateResourceException(
					"Duplicate user found with mobile Number - " + agentRequestDTO.getMobileNumber());
		}

		AppUser user = modelMapper.map(agentRequestDTO, AppUser.class);
		user.setEmail(agentRequestDTO.getEmail().toLowerCase());
		user.setPassword(passwordEncoder.encode(agentRequestDTO.getPassword()));
		user.setRole(Role.ROLE_AGENT);
		user.setIsActive(false);
		user.setEmailVerified(false);
		AppUser retrivedUser = userRepository.save(user);

		otpService.createAndSendOtp(retrivedUser);

		UserResponseDTO dto = modelMapper.map(retrivedUser, UserResponseDTO.class);
		return new ApiResponseDTO<>("Account created. An OTP has been sent to the email to complete registration.",
				true, dto, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<UserResponseDTO> getAllUsersWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection, String role, Boolean isActive) {
		log.info(
				"Fetching Users with pagination. pageNumber: {}, pageSize: {}, sortBy: {}, sortDirection: {}, role: {}, isActive: {}",
				pageNumber, pageSize, sortBy, sortDirection, role, isActive);
		validatePagination(pageNumber, pageSize);
		validateUserSortField(sortBy);

		Role roleEnum = null;
		if (role != null && !role.trim().isEmpty()) {
			try {
				roleEnum = Role.valueOf(role.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new BadRequestException("Invalid role filter: " + role);
			}
		}

		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(getSortDirection(sortDirection), sortBy));

		Page<AppUser> userPage;
		if (roleEnum != null && isActive != null) {
			userPage = userRepository.findByRoleAndIsActive(roleEnum, isActive, pageable);
		} else if (roleEnum != null) {
			userPage = userRepository.findByRole(roleEnum, pageable);
		} else if (isActive != null) {
			userPage = userRepository.findByIsActive(isActive, pageable);
		} else {
			userPage = userRepository.findAll(pageable);
		}
		List<UserResponseDTO> content = userPage.getContent().stream()
				.map(user -> modelMapper.map(user, UserResponseDTO.class)).toList();
		return new PageResponseDTO<>(content, userPage.getNumber(), userPage.getSize(), userPage.getTotalElements(),
				userPage.getTotalPages(), userPage.isLast(), sortDirection);
	}

	@Override
	public UserResponseDTO findByEmail(String username) {

		AppUser user = userRepository.findByEmail(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email : " + username));

		return modelMapper.map(user, UserResponseDTO.class);
	}

	@Override
	public ApiResponseDTO<UserResponseDTO> findUserById(Long id) {

		log.info("Fetching User with id - {} ", id);
		AppUser appUser = getById(id);

		UserResponseDTO dto = modelMapper.map(appUser, UserResponseDTO.class);

		return new ApiResponseDTO<>("User found", true, dto, LocalDateTime.now());
	}

	private AppUser getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	private void validatePagination(int pageNumber, int pageSize) {
		if (pageNumber < 0)
			throw new BadRequestException("Page number cannot be negative.");
		if (pageSize <= 0)
			throw new BadRequestException("Page size must be greater than 0.");
		if (pageSize > 100)
			throw new BadRequestException("Page size cannot be greater than 100.");
	}

	private void validateUserSortField(String sortBy) {
		if (!List.of("id", "fullName", "email", "mobileNumber", "role", "isActive").contains(sortBy)) {
			throw new BadRequestException("Invalid sort field for course: " + sortBy);
		}
	}

	private Sort.Direction getSortDirection(String sortDirection) {
		if (sortDirection == null || sortDirection.equalsIgnoreCase("asc"))
			return Sort.Direction.ASC;
		if (sortDirection.equalsIgnoreCase("desc"))
			return Sort.Direction.DESC;
		throw new BadRequestException("Sort direction must be asc or desc.");
	}

}
