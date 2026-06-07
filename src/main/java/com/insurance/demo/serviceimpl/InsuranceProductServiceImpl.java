package com.insurance.demo.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.insurance.demo.dto.request.ProductRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.ProductResponseDTO;
import com.insurance.demo.enums.ProductType;
import com.insurance.demo.enums.Role;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.ProductNotFoundException;
import com.insurance.demo.model.InsuranceProduct;
import com.insurance.demo.repository.InsurenceProductRepository;
import com.insurance.demo.service.InsuranceProductService;

import jakarta.validation.Valid;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceProductServiceImpl implements InsuranceProductService {

	private final ModelMapper modelMapper;
	private final InsurenceProductRepository productRepository;

	@Override
	@Transactional
	public ApiResponseDTO<ProductResponseDTO> createProduct(ProductRequestDTO dto) {

		if (productRepository.existsByProductNameIgnoreCase(dto.getProductName())) {
			throw new DuplicateResourceException("Duplicate product found with name - " + dto.getProductName());
		}

		InsuranceProduct product = new InsuranceProduct();

		product.setProductName(dto.getProductName());
		product.setProductType(dto.getProductType());
		product.setDescription(dto.getDescription());
		product.setIsActive(true);

		InsuranceProduct savedProduct = productRepository.save(product);

		ProductResponseDTO response = modelMapper.map(savedProduct, ProductResponseDTO.class);

		return new ApiResponseDTO<>("Product Created Successfully", true, response, LocalDateTime.now());
	}

	@Override
	public void deactivateProduct(Long id) {

		InsuranceProduct product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

		if (!product.getIsActive()) {
			throw new RuntimeException("Product is already inactive");
		}

		product.setIsActive(false);
		productRepository.save(product);
	}

	@Override
	@Transactional
	public PageResponseDTO<ProductResponseDTO> getAllProductsWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection) {

		log.info("Fetching Users with pagination. pageNumber: {}, pageSize: {}, sortBy: {}, sortDirection: {}",
				pageNumber, pageSize, sortBy, sortDirection);
		validatePagination(pageNumber, pageSize);
		validateUserSortField(sortBy);
		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(getSortDirection(sortDirection), sortBy));
		Page<InsuranceProduct> productPage = productRepository.findAll(pageable);
		List<ProductResponseDTO> content = productPage.getContent().stream()
				.map(product -> modelMapper.map(product, ProductResponseDTO.class)).toList();
		return new PageResponseDTO<>(content, productPage.getNumber(), productPage.getSize(),
				productPage.getTotalElements(), productPage.getTotalPages(), productPage.isLast(), sortDirection);
	}

	private Direction getSortDirection(String sortDirection) {
		if (sortDirection == null || sortDirection.equalsIgnoreCase("asc"))
			return Sort.Direction.ASC;
		if (sortDirection.equalsIgnoreCase("desc"))
			return Sort.Direction.DESC;
		throw new BadRequestException("Sort direction must be asc or desc.");
	}

	private void validateUserSortField(String sortBy) {
		if (!List.of("id", "productName", "productType").contains(sortBy)) {
			throw new BadRequestException("Invalid sort field for product: " + sortBy);
		}
	}

	private void validatePagination(int pageNumber, int pageSize) {
		if (pageNumber < 0)
			throw new BadRequestException("Page number cannot be negative.");
		if (pageSize <= 0)
			throw new BadRequestException("Page size must be greater than 0.");
		if (pageSize > 100)
			throw new BadRequestException("Page size cannot be greater than 100.");
	}

	@Transactional(readOnly = true)
	public ApiResponseDTO<List<ProductResponseDTO>> viewActiveProducts() throws ProductNotFoundException{

		log.info("fatching all active products");
		List<InsuranceProduct> products = productRepository.findByIsActiveTrue();
		
		 if (products.isEmpty()) {
		        log.warn("No active products found");
		        throw new ProductNotFoundException("No active insurance products found");
		    }

		List<ProductResponseDTO> productResponseDTOs = products.stream()
				.map(product -> modelMapper.map(product, ProductResponseDTO.class)).toList();

		ApiResponseDTO<List<ProductResponseDTO>> apiResponseDTO = new ApiResponseDTO<>();

		apiResponseDTO.setData(productResponseDTOs);
		apiResponseDTO.setMessage("Active products fetched successfully");
		apiResponseDTO.setSuccess(true);
		apiResponseDTO.setTimeStamp(LocalDateTime.now());
		
	    log.info("Retrieved {} active products", productResponseDTOs.size());
		return apiResponseDTO;

	}


	
	@Override
	public ProductResponseDTO updateProduct(Long productId, ProductRequestDTO requestDTO) {

	    log.info("Updating product with ID: {}", productId);

	    InsuranceProduct existingProduct = productRepository.findById(productId)
	            .orElseThrow(() -> {
	                log.error("Product not found with ID: {}", productId);
	                return new ProductNotFoundException(
	                        "Product not found with ID: " + productId);
	            });

	    //checking the duplicate product name 
	    Optional<InsuranceProduct> productWithSameName =
	            productRepository.findByProductNameIgnoreCase(requestDTO.getProductName());

	    if (productWithSameName.isPresent()
	            && !productWithSameName.get().getId().equals(productId)) {

	        log.warn("Duplicate product name '{}' found",
	                requestDTO.getProductName());

	        throw new IllegalArgumentException(
	                "Product name already exists: " + requestDTO.getProductName());
	    }

	    existingProduct.setProductName(requestDTO.getProductName().trim());
	    existingProduct.setProductType(requestDTO.getProductType());
	    existingProduct.setDescription(requestDTO.getDescription().trim());

	    InsuranceProduct updatedProduct = productRepository.save(existingProduct);

	    log.info("Product updated successfully. Product ID: {}", productId);

	    return modelMapper.map(updatedProduct, ProductResponseDTO.class);
	}
	
}
