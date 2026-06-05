package com.insurance.demo.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

	private Long id;

	private String fullName;

	private String email;

	private String mobileNumber;

	private String role;

	private Boolean activeStatus;

	private LocalDateTime createdDate;

	private LocalDateTime updatedDate;
}