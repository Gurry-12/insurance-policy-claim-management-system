package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.insurance.demo.dto.request.CustomerRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.CustomerResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

	private final CustomerService customerService;

	@PostMapping("/{userId}")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<CustomerResponseDTO> createCustomer(@PathVariable Long userId,
			@Valid @RequestBody CustomerRequestDTO requestDTO) {

		return customerService.createCustomer(userId, requestDTO);
	}

	@GetMapping("/{customerId}")
	public ApiResponseDTO<CustomerResponseDTO> getCustomerById(@PathVariable Long customerId) {

		return customerService.getCustomerById(customerId);
	}

	@GetMapping
	public ApiResponseDTO<List<CustomerResponseDTO>> getAllCustomers() {

		return customerService.getAllCustomers();
	}

	@PutMapping("/{customerId}")
	public ApiResponseDTO<CustomerResponseDTO> updateCustomer(@PathVariable Long customerId,
			@Valid @RequestBody CustomerRequestDTO requestDTO) {

		return customerService.updateCustomer(customerId, requestDTO);
	}

	@DeleteMapping("/{customerId}")
	public ApiResponseDTO<String> deleteCustomer(@PathVariable Long customerId) {

		return customerService.deleteCustomer(customerId);
	}

	@GetMapping("/paged")
	public PageResponseDTO<CustomerResponseDTO> getAllCustomersWithPagination(
			@RequestParam(defaultValue = "0") int pageNumber, @RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDirection) {

		return customerService.getAllCustomersWithPagination(pageNumber, pageSize, sortBy, sortDirection);
	}
}