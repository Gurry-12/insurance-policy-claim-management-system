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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
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
@ToString
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
	@NotEmpty(message = "Date of birth is required")
	@NotBlank(message = "Date of birth can not be blank")
	private LocalDate dateOfBirth;

	@Column(name = "address", nullable = false)
	@NotBlank(message = "Address is required")
	@NotEmpty(message = "Address can not be blank")
	private String address;

	@Column(name = "city", nullable = false)
	@NotBlank(message = "City is required")
	@NotEmpty(message = "City can not be blank")
	private String city;

	@Column(name = "state", nullable = false)
	@NotEmpty(message = "State is required")
	@NotBlank(message = "State can not be blank")
	private String state;

	@Column(name = "pin_code", nullable = false)
	@NotBlank(message = "Pin code is required")
	@NotEmpty(message = "pin code can not be blank")
	private Long pinCode;

	@Column(name = "nominee_name", nullable = false)
	@NotEmpty(message = "Nominee name is required")
	@NotBlank(message = "Nominee name can not be blank")
	private String nomineeName;

	@Column(name = "nominee_relation", nullable = false)
	@NotEmpty(message = "Nominee relation is required")
	@NotBlank(message = "Nominee relation can not be blank")
	private String nomineeRelation;

	@Column(name = "created_date")
	@CreationTimestamp
	private LocalDateTime createdDate;

	@Column(name = "updated_date")
	@UpdateTimestamp
	private LocalDateTime updatedDate;

}
