package com.insurance.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "customer", "policyPlan" })
public class Policy {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long policyId;

	@NotBlank
	@Column(nullable = false, unique = true)
	private String policyNumber;

	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@ManyToOne
	@JoinColumn(name = "plan_id", nullable = false)
	private PolicyPlan policyPlan;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private String policyStatus;

	@Column(nullable = false)
	private Double totalPremiumPaid = 0.0;

	private LocalDateTime createdDate = LocalDateTime.now();

	private LocalDateTime updatedDate;

	@OneToMany(mappedBy = "policy")
	private List<PremiumPayment> payments;

	@OneToMany(mappedBy = "policy")
	private List<Claim> claims;
}