package com.insurance.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private AppUser user;

	@Column(name = "date_of_birth", nullable = false)
	@Past(message = "Date of birth must be in the past")
	@NotNull(message = "Date of birth can not be null")
	private LocalDate dateOfBirth;

	@Column(name = "address", nullable = false)
	@NotBlank(message = "Address is required")
	private String address;

	@Column(name = "city", nullable = false)
	@NotBlank(message = "City is required")
	private String city;

	@Column(name = "state", nullable = false)
	@NotBlank(message = "State can not be blank")
	private String state;

	@Column(name = "pin_code", nullable = false)
	@NotBlank(message = "Pin code is required")
	@Size(min = 6, message = "pin code should be min 6 chars")
	private String pinCode;

	@Column(name = "nominee_name", nullable = false)
	@NotBlank(message = "Nominee name can not be blank")
	private String nomineeName;

	@Column(name = "nominee_relation", nullable = false)
	@NotBlank(message = "Nominee relation can not be blank")
	private String nomineeRelation;

	@Column(name = "created_date", updatable = false)
	@CreationTimestamp
	private LocalDateTime createdDate;

	@Column(name = "updated_date")
	@UpdateTimestamp
	private LocalDateTime updatedDate;

}
