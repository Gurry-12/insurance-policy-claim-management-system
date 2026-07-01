package com.insurance.demo.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.ProductRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.ProductResponseDTO;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.ProductNotFoundException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.InsuranceProduct;
import com.insurance.demo.repository.InsuranceProductRepository;
import com.insurance.demo.service.InsuranceProductService;
import com.insurance.demo.util.PaginationValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceProductServiceImpl implements InsuranceProductService {

	private final ModelMapper modelMapper;
	private final InsuranceProductRepository productRepository;

	@Override
	@Transactional
	public ApiResponseDTO<ProductResponseDTO> createProduct(ProductRequestDTO dto) {

		if (productRepository.existsByProductNameIgnoreCase(dto.getProductName())) {
			throw new DuplicateResourceException("An insurance product with this name already exists: - " + dto.getProductName());
		}

		InsuranceProduct product = new InsuranceProduct();

		product.setProductName(dto.getProductName().toLowerCase());
		product.setProductType(dto.getProductType());
		product.setDescription(dto.getDescription());
		product.setIsActive(dto.getActiveStatus() != null ? dto.getActiveStatus() : true);

		InsuranceProduct savedProduct = productRepository.save(product);

		ProductResponseDTO response = modelMapper.map(savedProduct, ProductResponseDTO.class);

		return new ApiResponseDTO<>("Insurance product created successfully.", true, response, LocalDateTime.now());
	}

	@Override
	@Transactional
	public ApiResponseDTO<ProductResponseDTO> deactivateProduct(Long id) {

		InsuranceProduct product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

		if (!product.getIsActive()) {
			throw new BadRequestException("The selected insurance product is already marked as inactive.");
		}

		product.setIsActive(false);

		InsuranceProduct updatedProduct = productRepository.save(product);

		ProductResponseDTO response = modelMapper.map(updatedProduct, ProductResponseDTO.class);

		return new ApiResponseDTO<>("Insurance product deactivated successfully", true, response, LocalDateTime.now());
	}

	@Override
	@Transactional
	public PageResponseDTO<ProductResponseDTO> getAllProductsWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection, String productType, Boolean isActive) {

		log.info("Fetching products with pagination. pageNumber: {}, pageSize: {}, sortBy: {}, sortDirection: {}, type: {}, active: {}",
				pageNumber, pageSize, sortBy, sortDirection, productType, isActive);
		PaginationValidator.validate(pageNumber, pageSize);
		PaginationValidator.validateSortField(sortBy, Set.of("id", "productName", "productType"));

		com.insurance.demo.enums.ProductType typeEnum = null;
		if (productType != null && !productType.trim().isEmpty()) {
			try {
				typeEnum = com.insurance.demo.enums.ProductType.valueOf(productType.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new BadRequestException("Invalid product type filter: " + productType);
			}
		}

		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(getSortDirection(sortDirection), sortBy));
		Page<InsuranceProduct> productPage;
		if (typeEnum != null && isActive != null) {
			productPage = productRepository.findByProductTypeAndIsActive(typeEnum, isActive, pageable);
		} else if (typeEnum != null) {
			productPage = productRepository.findByProductType(typeEnum, pageable);
		} else if (isActive != null) {
			productPage = productRepository.findByIsActive(isActive, pageable);
		} else {
			productPage = productRepository.findAll(pageable);
		}
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

	
	
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<ProductResponseDTO>> viewActiveProducts() throws ResourceNotFoundException {

		log.info("fatching all active products");
		List<InsuranceProduct> products = productRepository.findByIsActiveTrue();

		if (products.isEmpty()) {
			log.warn("No active products found");
			throw new ResourceNotFoundException("No active insurance products found");
		}

		List<ProductResponseDTO> productResponseDTOs = products.stream().map(product -> {

			ProductResponseDTO dto = modelMapper.map(product, ProductResponseDTO.class);

			dto.setActive(product.getIsActive());

			return dto;
		}).toList();

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

		InsuranceProduct existingProduct = productRepository.findById(productId).orElseThrow(() -> {
			log.error("Product not found with ID: {}", productId);
			return new ResourceNotFoundException("Product not found with ID: " + productId);
		});

		// checking the duplicate product name
		Optional<InsuranceProduct> productWithSameName = productRepository
				.findByProductNameIgnoreCase(requestDTO.getProductName());

		if (productWithSameName.isPresent() && !productWithSameName.get().getId().equals(productId)) {

			log.warn("Duplicate product name '{}' found", requestDTO.getProductName());

			throw new DuplicateResourceException("Product name already exists: " + requestDTO.getProductName());
		}

		existingProduct.setProductName(requestDTO.getProductName().trim().toLowerCase());
		existingProduct.setProductType(requestDTO.getProductType());
		existingProduct.setDescription(requestDTO.getDescription().trim());
		if (requestDTO.getActiveStatus() != null) {
			existingProduct.setIsActive(requestDTO.getActiveStatus());
		}

		InsuranceProduct updatedProduct = productRepository.save(existingProduct);

		log.info("Product updated successfully. Product ID: {}", productId);

		return modelMapper.map(updatedProduct, ProductResponseDTO.class);
	}

	
	
	
	@Override
	@Transactional
	public ApiResponseDTO<ProductResponseDTO> activateProduct(Long id) {

		log.info("Activating product with id: {}", id);

		InsuranceProduct product = productRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

		if (Boolean.TRUE.equals(product.getIsActive())) {
			throw new BadRequestException("Product is already active");
		}

		product.setIsActive(true);

		InsuranceProduct updatedProduct = productRepository.save(product);

		ProductResponseDTO dto = modelMapper.map(updatedProduct, ProductResponseDTO.class);

		log.info("Product activated successfully with id: {}", id);

		return new ApiResponseDTO<>("Product activated successfully", true, dto, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<ProductResponseDTO> getProductById(Long id) {
		log.info("Fetching product with id: {}", id);
		InsuranceProduct product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
		ProductResponseDTO dto = modelMapper.map(product, ProductResponseDTO.class);
		return new ApiResponseDTO<>("Product details retrieved successfully", true, dto, LocalDateTime.now());
	}

}
