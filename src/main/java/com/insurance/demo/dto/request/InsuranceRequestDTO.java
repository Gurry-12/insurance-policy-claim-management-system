package com.insurance.demo.dto.request;

import com.insurance.demo.enums.ProductType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceRequestDTO {

	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Only letters and spaces are allowed")
	@NotBlank(message = "Insurance name is required")
	private String productName;

	@NotBlank(message = "Insurance type is required")
	private ProductType productType;

	@NotBlank(message = "Description is required")
	private String description;
}