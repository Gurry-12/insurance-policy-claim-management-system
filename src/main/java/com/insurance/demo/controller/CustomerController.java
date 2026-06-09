package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.insurance.demo.dto.request.CustomerRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.CustomerResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.service.CustomerService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

	private final CustomerService customerService;

	@PostMapping("/{userId}")
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('CUSTOMER')")
	public ApiResponseDTO<CustomerResponseDTO> createCustomer(@PathVariable Long userId,
			@Valid @RequestBody CustomerRequestDTO requestDTO) {

		return customerService.createCustomer(userId, requestDTO);
	}

	@GetMapping("/{customerId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	public ApiResponseDTO<CustomerResponseDTO> getCustomerById(@PathVariable Long customerId) {

		return customerService.getCustomerById(customerId);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	public ApiResponseDTO<List<CustomerResponseDTO>> getAllCustomers() {

		return customerService.getAllCustomers();
	}

	@PutMapping("/{customerId}")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ApiResponseDTO<CustomerResponseDTO> updateCustomer(@PathVariable Long customerId,
			@Valid @RequestBody CustomerRequestDTO requestDTO) {

		return customerService.updateCustomer(customerId, requestDTO);
	}

	@GetMapping("/page")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	public PageResponseDTO<CustomerResponseDTO> getAllCustomersWithPagination(
			@RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDirection,
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String state) {

		return customerService.getAllCustomersWithPagination(pageNumber, pageSize, sortBy, sortDirection, city, state);
	}
	
	@GetMapping("/profile")
	@PreAuthorize("hasRole('CUSTOMER')")
	public ApiResponseDTO<CustomerResponseDTO> getCustomerProfile() {

		return customerService.getCustomerProfile();
	}
}