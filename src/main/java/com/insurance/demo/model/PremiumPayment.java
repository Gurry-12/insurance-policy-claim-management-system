package com.insurance.demo.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "premium_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "policy")
public class PremiumPayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;

	@ManyToOne
	@JoinColumn(name = "policy_id", nullable = false)
	private Policy policy;

	@Positive
	@Column(nullable = false)
	private Double amount;

	private LocalDateTime paymentDate = LocalDateTime.now();

	@NotBlank
	@Column(nullable = false)
	private String paymentMode;

	@NotBlank
	@Column(nullable = false, unique = true)
	private String transactionReference;

	@NotBlank
	@Column(nullable = false)
	private String paymentStatus;

	private LocalDateTime createdDate = LocalDateTime.now();
}