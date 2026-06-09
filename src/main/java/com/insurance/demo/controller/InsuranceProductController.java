package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.request.ProductRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.ProductResponseDTO;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.service.InsuranceProductService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class InsuranceProductController {

	private final InsuranceProductService productService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO dto) {
		return productService.createProduct(dto);
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponseDTO<ProductResponseDTO> deactivateProduct(@PathVariable Long id) {
		return productService.deactivateProduct(id);
	}

	@GetMapping("/active")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
	public ApiResponseDTO<List<ProductResponseDTO>> viewActiveProducts() throws ResourceNotFoundException {
		return productService.viewActiveProducts();
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id,
			@Valid @RequestBody ProductRequestDTO requestDTO) {
		ProductResponseDTO response = productService.updateProduct(id, requestDTO);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
	public ApiResponseDTO<ProductResponseDTO> getProductById(@PathVariable Long id) {
		return productService.getProductById(id);
	}
	
	@GetMapping("/page")
	@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
	public PageResponseDTO<ProductResponseDTO> getAllProductsWithPagination(
			@RequestParam(defaultValue = "0") int pageNumber,
			@RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDirection,
			@RequestParam(required = false) String productType,
			@RequestParam(required = false) Boolean isActive) {
		return productService.getAllProductsWithPagination(pageNumber, pageSize, sortBy, sortDirection, productType, isActive);
	}
	
	@PatchMapping("/{id}/active")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponseDTO<ProductResponseDTO> activateProduct(@PathVariable Long id) {
		return productService.activateProduct(id);
	}
}
