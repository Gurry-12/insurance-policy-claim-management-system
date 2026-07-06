package com.insurance.demo.dto.request;

import com.insurance.demo.enums.PremiumType;

import com.insurance.demo.util.MessageConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequestDTO {

	@NotNull(message = MessageConstants.Validation.PRODUCT_ID_REQUIRED)
	private Long productId;

	@Pattern(regexp = "^[a-zA-Z\\s]*$", message = MessageConstants.Validation.LETTERS_SPACES_ONLY)
	@NotBlank(message = MessageConstants.Validation.PLAN_NAME_REQUIRED)
	private String planName;

	@Positive(message = MessageConstants.Validation.COVERAGE_REQUIRED)
	private BigDecimal coverageAmount;

	@Positive(message = MessageConstants.Validation.PREMIUM_REQUIRED)
	private BigDecimal premiumAmount;

	@NotNull(message = MessageConstants.Validation.PREMIUM_TYPE_REQUIRED)
	private PremiumType premiumType;

	@Positive(message = MessageConstants.Validation.DURATION_REQUIRED)
	@Max(value = 40, message = MessageConstants.Validation.DURATION_MAX)
	private Integer duration;

	@NotBlank(message = MessageConstants.Validation.TERMS_REQUIRED)
	private String termsAndConditions;

	@NotNull(message = MessageConstants.Validation.ACTIVE_STATUS_REQUIRED)
	private Boolean activeStatus;
}