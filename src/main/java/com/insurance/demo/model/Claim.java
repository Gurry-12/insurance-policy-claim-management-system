package com.insurance.demo.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "claim")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Claim {
	
	@Id
	@Column(name = "claim_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "claim_number",unique = true, nullable = false)
	private Long claimNumber;
	
	@Column(name = "claim_amount")
	@Positive(message = "amount must be greater than zero")
	private double claimAmount;
	
	@Column(name = "claim_reason")
	@NotNull(message = "reason is required")
	private String claimReason;
	
	@Column(name = "incident_date")
	private LocalDateTime incidentDate;
	
	@Column(name = "claim_status")
	private boolean claimStatus;
	
	@Column(name = "agent_remark")
	private String agentRemark;
	
	@Column(name = "admin_remark")
	private String adminRemark;
	
	@Column(name = "created_date")
	private LocalDateTime createdDate;
	
	@Column(name = "updated_date")
	private LocalDateTime updatedDate;
	
//	@ManyToOne
//	@JoinColumn(name = "policy_id",nullable = false)
//	private Policy policy;
	
	@OneToMany(mappedBy = "claim",cascade = CascadeType.ALL)
	@JsonBackReference
	private List<ClaimDocument> claimDocument;
	
	@OneToMany(mappedBy = "claim",cascade = CascadeType.ALL)
	@JsonBackReference
	private List<ClaimStatusHistory> claimStatusHistory ;
	


}
