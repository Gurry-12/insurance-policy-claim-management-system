package com.insurance.demo.dto.request;

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
public class UserStatusUpdateRequestDTO {

	@NotNull(message = "Active status is required")
	private Boolean isActive;

	@NotBlank(message = "Remarks are required")
	private String remarks;
}
