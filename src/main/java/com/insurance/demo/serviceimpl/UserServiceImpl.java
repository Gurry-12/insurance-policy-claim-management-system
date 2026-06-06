package com.insurance.demo.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.CreateAgentRequestDTO;
import com.insurance.demo.dto.request.UserRequestDTO;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final AppUserRepository userRepository;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;

	
	

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<UserResponseDTO>> viewAllUsers() {

		log.info("fatching all users");
		List<AppUser> users = userRepository.findByRoleIn(
			    List.of(Role.ROLE_COSTOMER, Role.ROLE_AGENT)
				);

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

		AppUser user = findUserById(userId);

		if (Boolean.TRUE.equals(user.getIsActive())) {
			UserResponseDTO dto = modelMapper.map(user, UserResponseDTO.class);
			log.info("user already active with id {} ", userId);
			return new ApiResponseDTO<>("User Already Active", false, dto, LocalDateTime.now());
		}

		user.setIsActive(true);

		AppUser retrivedUser = userRepository.save(user);

		UserResponseDTO dto = modelMapper.map(retrivedUser, UserResponseDTO.class);
		return new ApiResponseDTO<>("User Actived", true, dto, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<UserResponseDTO> deactivateUser(Long userId) {

		log.info("Deactivating user by id: {}", userId);

		AppUser user = findUserById(userId);

		if (Boolean.FALSE.equals(user.getIsActive())) {
			UserResponseDTO dto = modelMapper.map(user, UserResponseDTO.class);
			log.info("Already deactivated user by id: {}", userId);
			return new ApiResponseDTO<>("User Already Deactivated", false, dto, LocalDateTime.now());
		}

		user.setIsActive(false);

		AppUser retrivedUser = userRepository.save(user);

		UserResponseDTO dto = modelMapper.map(retrivedUser, UserResponseDTO.class);
		return new ApiResponseDTO<>("User Deactived", true, dto, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<UserResponseDTO> createAgentUser(CreateAgentRequestDTO agentRequestDTO) {

		log.info("creating agent by email: {}", agentRequestDTO.getEmail());

		if (userRepository.existsByEmail(agentRequestDTO.getEmail())) {
			throw new DuplicateResourceException("Duplicate user found with email - " + agentRequestDTO.getEmail());
		}

		AppUser user = modelMapper.map(agentRequestDTO, AppUser.class);
		user.setPassword(passwordEncoder.encode(agentRequestDTO.getPassword()));
		user.setRole(Role.ROLE_AGENT);
		user.setIsActive(true);
		AppUser retrivedUser = userRepository.save(user);

		UserResponseDTO dto = modelMapper.map(retrivedUser, UserResponseDTO.class);
		return new ApiResponseDTO<>("Agent Created", true, dto, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<UserResponseDTO> getAllUsersWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection) {
		log.info("Fetching Users with pagination. pageNumber: {}, pageSize: {}, sortBy: {}, sortDirection: {}",
				pageNumber, pageSize, sortBy, sortDirection);
		validatePagination(pageNumber, pageSize);
		validateUserSortField(sortBy);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(getSortDirection(sortDirection), sortBy));
		Page<AppUser> userPage = userRepository.findAll(pageable);
		List<UserResponseDTO> content = userPage.getContent().stream()
				.map(user -> modelMapper.map(user, UserResponseDTO.class)).toList();
		return new PageResponseDTO<>(content, userPage.getNumber(), userPage.getSize(), userPage.getTotalElements(),
				userPage.getTotalPages(), userPage.isLast(), sortDirection);
	}

	private AppUser findUserById(Long id) {
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
		if (!List.of("id", "fullName", "email", "mobileNumber").contains(sortBy)) {
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
