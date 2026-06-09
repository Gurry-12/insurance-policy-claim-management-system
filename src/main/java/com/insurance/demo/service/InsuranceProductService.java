package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.request.ProductRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.ProductResponseDTO;
import com.insurance.demo.dto.response.UserResponseDTO;
import com.insurance.demo.model.InsuranceProduct;

import jakarta.validation.Valid;

public interface InsuranceProductService {

	ApiResponseDTO<ProductResponseDTO> createProduct(ProductRequestDTO productDto);

	ApiResponseDTO<ProductResponseDTO> deactivateProduct(Long id);

	PageResponseDTO<ProductResponseDTO> getAllProductsWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection, String productType, Boolean isActive);

	ProductResponseDTO updateProduct(Long id, @Valid ProductRequestDTO requestDTO);

	ApiResponseDTO<List<ProductResponseDTO>> viewActiveProducts();

	ApiResponseDTO<ProductResponseDTO> activateProduct(Long id);

	ApiResponseDTO<ProductResponseDTO> getProductById(Long id);

}
