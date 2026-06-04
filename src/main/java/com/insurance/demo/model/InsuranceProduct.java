package com.insurance.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "insurance_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InsuranceProduct {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "product_name", nullable = false, unique = true)
	@NotBlank(message = "Product name is required")
	@NotEmpty(message = "Product name can not be blank")
	private String productName;
	
	@Column(name = "product_type" , nullable = false)
	@NotBlank(message = "Product type is required")
	@NotEmpty(message = "Product type can not be blank")
	private String productType;
	
	@Column(name = "description" , nullable = false)
	@NotBlank(message = "Description is required")
	@NotEmpty(message = "Description can not be blank")
	private String description;
	
	@Column(name = "active_status" , nullable = false)
	@NotEmpty(message = "Status is required")
	@NotBlank(message = "Status can not be blank")
	private boolean activeStatus;

	@Column(name = "created_date")
	@CreationTimestamp
	private LocalDateTime createdDate;

	@Column(name = "updated_date")
	@UpdateTimestamp
	private LocalDateTime updatedDate;
	
	@OneToMany(
	        mappedBy = "insuranceProduct",
	        cascade = CascadeType.ALL,
	        orphanRemoval = true
	)
	private List<PolicyPlan> policyPlans = new ArrayList<>();
}
