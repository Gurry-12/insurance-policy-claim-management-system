package com.insurance.demo.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.insurance.demo.enums.PremiumType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "policy_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPlan {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private InsuranceProduct insuranceProduct;

	@NotBlank(message = "name can't be blank")
	@Column(name = "plan_name", nullable = false)
	@Size(min = 2, max = 100, message = "name should be beteeen 2 - 100 characters")
	private String planName;

	@Positive(message = "amount should be positive")
	@Column(name = "coverage_amount", nullable = false)
	@NotNull(message = "amount can't be null")
	private Double coverageAmount;

	@Positive(message = "amount should be positive")
	@Column(name = "premium_amount", nullable = false)
	@NotNull(message = "premium amount can't be null")
	private Double premiumAmount;

	@Enumerated(EnumType.STRING)
	@NotNull(message = "premium type can't be null")
	@Column(name = "premium_type", nullable = false)
	private PremiumType premiumType;

	@Positive(message = "duration should be positive")
	@Column(name = "duration", nullable = false)
	@NotNull(message = "duration can't be null")
	private Integer duration;

	@NotBlank(message = "T & C can't be blank")
	@Column(name = "terms_conditions", nullable = false, length = 3000)
	private String termsAndConditions;

	@NotNull(message = "status can't be null")
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

	@Column(name = "created_date", updatable = false)
	@CreationTimestamp
	private LocalDateTime createdDate;

	@Column(name = "updated_date")
	@UpdateTimestamp
	private LocalDateTime updatedDate;

	@OneToMany(mappedBy = "policyPlan")
	private List<Policy> policies;
}