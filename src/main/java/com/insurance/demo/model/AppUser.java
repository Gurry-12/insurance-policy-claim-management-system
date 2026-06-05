package com.insurance.demo.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.insurance.demo.enums.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "full_name", nullable = false)
	@NotBlank(message = "name can not be blank")
	@Size(min = 2, max = 100, message = "name should be beteeen 2 - 100 characters")
	private String fullName;

	@Column(name = "email", unique = true, nullable = false)
	@Email(message = "enter a valid email")
	@NotBlank(message = "email can not be blank")
	private String email;

	@Column(name = "password", nullable = false)
	@NotBlank(message = "password can not be blank")
	@Size(min = 6, max = 15, message = "password should be between 6 to 15")
	private String password;

	@Column(name = "mobile_number", nullable = false, unique = true)
	@NotBlank(message = "mobile number can not be blank")
	@Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter valid 10 digit mobile number")
	private String mobileNumber;

	@Column(name = "is_active", nullable = false)
	@NotNull(message = "status can not be null")
	private Boolean isActive;

	@Column(name = "created_date")
	@CreationTimestamp
	private LocalDateTime createdDate;

	@Column(name = "updated_date")
	@UpdateTimestamp
	private LocalDateTime updatedDate;

	@Column(name = "role", nullable = false)
	@Enumerated(EnumType.STRING)
	@NotNull(message = "role can not be null")
	private Role role;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private Customer customer;

}
