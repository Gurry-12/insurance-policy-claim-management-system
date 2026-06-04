package com.insurance.demo.model;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "policy_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "insuranceProduct", "policies" })
public class PolicyPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long planId;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private InsuranceProduct insuranceProduct;

	@NotBlank
	@Column(nullable = false)
	private String planName;

	@Positive
	@Column(nullable = false)
	private Double coverageAmount;

	@Positive
	@Column(nullable = false)
	private Double premiumAmount;

	@NotBlank
	@Column(nullable = false)
	private String premiumType;

	@Positive
	@Column(nullable = false)
	private Integer duration;

	@NotBlank
	@Column(nullable = false, length = 3000)
	private String termsAndConditions;

	private boolean activeStatus;

	private LocalDateTime createdDate = LocalDateTime.now();

	private LocalDateTime updatedDate;

	@OneToMany(mappedBy = "policyPlan")
	private List<Policy> policies;
}