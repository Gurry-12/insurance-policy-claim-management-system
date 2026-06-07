package com.insurance.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.insurance.demo.dto.response.UserResponseDTO;
import com.insurance.demo.exception.ProductNotFoundException;
import com.insurance.demo.service.InsuranceProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class InsuranceProductController {
	
	@Autowired
	private InsuranceProductService productService;
	
	@PostMapping("/create")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO dto){
		
		return  productService.createProduct(dto);
		
	}
	
	@PatchMapping("/deactivate/{id}")
	public void deactivateProduct(@PathVariable Long id) {
		
		productService.deactivateProduct(id);
	}
	
	
	@GetMapping("/active")
	public ApiResponseDTO<List<ProductResponseDTO>> viewActiveProducts() throws ProductNotFoundException{
		return productService.viewActiveProducts();
	}
	
	
	@PutMapping("/update/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductResponseDTO> updateProduct(
	        @PathVariable Long id,
	        @Valid @RequestBody ProductRequestDTO requestDTO) {

	    ProductResponseDTO response = productService.updateProduct(id, requestDTO);

	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("/page")
	public PageResponseDTO<ProductResponseDTO> getAllProductsWithPagination(
			@RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
		return productService.getAllProductsWithPagination(pageNumber, pageSize, sortBy, sortDirection);
	}

	
}
	

