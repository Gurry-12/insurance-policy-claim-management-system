package com.insurance.demo.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.enums.Role;
import com.insurance.demo.dto.request.CustomerRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.CustomerResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.model.Customer;
import com.insurance.demo.repository.AppUserRepository;
import com.insurance.demo.repository.CustomerRepository;
import com.insurance.demo.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

	private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

	private final CustomerRepository customerRepository;
	private final AppUserRepository appUserRepository;
	private final ModelMapper modelMapper;

	@Override
	public ApiResponseDTO<CustomerResponseDTO> createCustomer(Long userId, CustomerRequestDTO requestDTO) {

		logger.info("Creating customer profile for userId: {}", userId);

		if (requestDTO.getDateOfBirth().isAfter(LocalDate.now().minusYears(18))) {

			throw new BadRequestException("Customer must be at least 18 years old");
		}

		AppUser user = appUserRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		if (user.getRole() != Role.ROLE_CUSTOMER) {
			throw new BadRequestException("Only users with customer role can have a customer profile");
		}

		if (customerRepository.existsByUserId(userId)) {
			throw new BadRequestException("Customer profile already exists for this user");
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String loggedInEmail = authentication.getName();
		
		AppUser loggedInUser = appUserRepository.findByEmail(loggedInEmail)
				.orElseThrow(() -> new ResourceNotFoundException("user's profile not found"));
		
		if (!user.getId().equals(loggedInUser.getId())) {
			throw new BadRequestException("You can not create another user as customer");
		}

		Customer customer = modelMapper.map(requestDTO, Customer.class);

		customer.setUser(user);

		Customer savedCustomer = customerRepository.save(customer);

		CustomerResponseDTO dto = convertToResponseDTO(savedCustomer);

		logger.info("Customer profile created successfully with id: {}", savedCustomer.getId());

		return new ApiResponseDTO<>("Customer profile Created Successfully", true, dto, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<CustomerResponseDTO> getCustomerById(Long customerId) {

		logger.info("Fetching customer with id: {}", customerId);

		Customer customer = findCustomerById(customerId);

		validateCustomerAccess(customer);

		CustomerResponseDTO dto = convertToResponseDTO(customer);

		return new ApiResponseDTO<>("Customer details retrieved successfully.", true, dto, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<CustomerResponseDTO>> getAllCustomers() {

		logger.info("Fetching all customers");

		List<CustomerResponseDTO> customers = customerRepository.findAll().stream().map(this::convertToResponseDTO)
				.toList();

		return new ApiResponseDTO<>("Customer details retrieved successfully.", true, customers, LocalDateTime.now());
	}

	@Override
	public ApiResponseDTO<CustomerResponseDTO> updateCustomer(Long customerId, CustomerRequestDTO requestDTO) {

		logger.info("Updating customer with id: {}", customerId);

		Customer customer = findCustomerById(customerId);

		validateCustomerAccess(customer);

		modelMapper.map(requestDTO, customer);

		Customer updatedCustomer = customerRepository.save(customer);

		CustomerResponseDTO dto = convertToResponseDTO(updatedCustomer);

		logger.info("Customer updated successfully with id: {}", customerId);

		return new ApiResponseDTO<>("Customer profile Updated Successfully", true, dto, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<CustomerResponseDTO> getAllCustomersWithPagination(int pageNumber, int pageSize,
			String sortBy, String sortDirection, String city, String state) {

		logger.info(
				"Fetching customers with pagination. pageNumber: {}, pageSize: {}, sortBy: {}, sortDirection: {}, city: {}, state: {}",
				pageNumber, pageSize, sortBy, sortDirection, city, state);

		validatePagination(pageNumber, pageSize);
		validateCustomerSortField(sortBy);

		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(getSortDirection(sortDirection), sortBy));

		Page<Customer> customerPage;
		boolean hasCity = city != null && !city.trim().isEmpty();
		boolean hasState = state != null && !state.trim().isEmpty();

		if (hasCity && hasState) {
			customerPage = customerRepository.findByCityContainingIgnoreCaseAndStateContainingIgnoreCase(city.trim(), state.trim(), pageable);
		} else if (hasCity) {
			customerPage = customerRepository.findByCityContainingIgnoreCase(city.trim(), pageable);
		} else if (hasState) {
			customerPage = customerRepository.findByStateContainingIgnoreCase(state.trim(), pageable);
		} else {
			customerPage = customerRepository.findAll(pageable);
		}

		List<CustomerResponseDTO> content = customerPage.getContent().stream().map(this::convertToResponseDTO).toList();

		return new PageResponseDTO<>(content, customerPage.getNumber(), customerPage.getSize(),
				customerPage.getTotalElements(), customerPage.getTotalPages(), customerPage.isLast(), sortDirection);
	}
	private Customer findCustomerById(Long customerId) {

		return customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
	}

	private void validateCustomerAccess(Customer customer) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String loggedInEmail = authentication.getName();

		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {

			if (!customer.getUser().getEmail().equals(loggedInEmail)) {

				throw new BadRequestException("You are not allowed to access another customer's profile");
			}
		}
	}

	private void validatePagination(int pageNumber, int pageSize) {

		if (pageNumber < 0) {
			throw new BadRequestException("Page number cannot be negative.");
		}

		if (pageSize <= 0) {
			throw new BadRequestException("Page size must be greater than 0.");
		}

		if (pageSize > 100) {
			throw new BadRequestException("Page size cannot be greater than 100.");
		}
	}

	private void validateCustomerSortField(String sortBy) {

		if (!List.of("id", "city", "state", "pinCode", "createdDate").contains(sortBy)) {

			throw new BadRequestException("Invalid sort field: " + sortBy);
		}
	}

	private Sort.Direction getSortDirection(String sortDirection) {

		if (sortDirection == null || sortDirection.equalsIgnoreCase("asc")) {
			return Sort.Direction.ASC;
		}

		if (sortDirection.equalsIgnoreCase("desc")) {
			return Sort.Direction.DESC;
		}

		throw new BadRequestException("Sort direction must be asc or desc.");
	}

	private CustomerResponseDTO convertToResponseDTO(Customer customer) {

		CustomerResponseDTO dto = modelMapper.map(customer, CustomerResponseDTO.class);

		dto.setCustomerId(customer.getId());

		if (customer.getUser() != null) {

			dto.setUserId(customer.getUser().getId());
			dto.setFullName(customer.getUser().getFullName());
			dto.setEmail(customer.getUser().getEmail());
			dto.setMobileNumber(customer.getUser().getMobileNumber());
		}

		return dto;
	}

	@Override
	public ApiResponseDTO<CustomerResponseDTO> getCustomerProfile() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String loggedInEmail = authentication.getName();

		if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
			throw new BadRequestException("Permission Denied ");
		}
		Customer customer = customerRepository.findByUserEmail(loggedInEmail)
				.orElseThrow(() -> new ResourceNotFoundException("customer's profile not found"));

		CustomerResponseDTO dto = convertToResponseDTO(customer);

		return new ApiResponseDTO<>("CCustomer details retrieved successfully.", true, dto, LocalDateTime.now());
	}

}