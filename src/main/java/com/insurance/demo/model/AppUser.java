package com.insurance.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
	
	@Column(name= "full_name", nullable = false)
	@NotEmpty(message = "name can not be empty")
	private String fullName;
	
	@Column(name="email", unique = true, nullable = false)
	@Email(message = "enter a valid email")
	private String email;
	
	@Column(name = "password", nullable = false)
	@NotNull(message = "password can not be null")
	private String password;
	
	@Column(name="mobile_number", nullable = false)
	@NotNull(message = "mobile number can not be null")
	private String mobileNumber;
	
	@Column(name = "role", nullable = false)
	@NotNull(message = "role can not be null")
	private String role;
	
	@Column(name = "active_status")
	private boolean activeStatus;
	
	@Column(name= "created_date")
	private LocalDateTime createdDate = LocalDateTime.now();
	
	@Column(name="updated_date")
	private LocalDateTime updatedDate;
	
}
