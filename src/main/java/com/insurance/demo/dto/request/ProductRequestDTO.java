package com.insurance.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
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

	@NotBlank(message = "Product type is required")
	private String productType;

	@NotBlank(message = "Description is required")
	private String description;
}