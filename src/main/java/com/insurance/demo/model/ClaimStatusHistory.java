package com.insurance.demo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "claim_status_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClaimStatusHistory {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "previous_status")
	private String previousStatus;
	
	@Column(name = "new_status")
	@NotNull(message = "new status is requied")
	private String newStatus;
	
	@Column(name = "remarks")
	private String remarks;
	
	@Column(name = "updated_by")
	private String updatedBy;
	
	@Column(name = "updated_date")
	private LocalDateTime updatedDate;
	
	@ManyToOne
	@JoinColumn(name = "claim_id", nullable = false)
	private Claim claim;
	

}
