package com.insurance.demo.dto.request;

import com.insurance.demo.enums.ProductType;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceRequestDTO {

	@NotBlank(message = "Insurance name is required")
	private String productName;

	@NotBlank(message = "Insurance type is required")
	private ProductType productType;

	@NotBlank(message = "Description is required")
	private String description;
}