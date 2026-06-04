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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
@ToString
public class AppUser {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "full_name", nullable = false)
	@NotEmpty(message = "name can not be empty")
	@NotBlank(message = "name can not be blank")
	private String fullName;

	@Column(name = "email", unique = true, nullable = false)
	@Email(message = "enter a valid email")
	@NotEmpty(message = "email can not be empty")
	@NotBlank(message = "email can not be blank")
	private String email;

	@Column(name = "password", nullable = false)
	@NotEmpty(message = "mobile number can not be empty")
	@NotBlank(message = "password can not be blank")
	private String password;

	@Column(name = "mobile_number", nullable = false)
	@NotEmpty(message = "mobile number can not be null")
	@NotBlank(message = "mobile number can not be blank")
	private String mobileNumber;

	@Column(name = "active_status", nullable = false)
	@NotEmpty(message = "status can not be empty")
	@NotBlank(message = "status can not be blank")
	private Boolean activeStatus;

	@Column(name = "created_date")
	@CreationTimestamp
	private LocalDateTime createdDate;

	@Column(name = "updated_date")
	@UpdateTimestamp
	private LocalDateTime updatedDate;

	@Enumerated(EnumType.STRING)
	private Role role;
	
	@OneToOne(mappedBy = "user",
			cascade = CascadeType.ALL,
			fetch = FetchType.LAZY,
			orphanRemoval = true)
	private Customer customer;

}
