package com.insurance.demo.dto.request;

import com.insurance.demo.enums.ProductType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

	@NotBlank(message = "Product name is required")
	private String productName;

	@NotNull(message = "Product type is required")
	private ProductType productType;

	@NotBlank(message = "Description is required")
	private String description;

	@NotNull(message = "Active status is required")
	private Boolean activeStatus;
}