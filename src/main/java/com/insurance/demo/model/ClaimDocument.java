package com.insurance.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Table(name = "claim_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClaimDocument{
	
	@Id
	@Column(name = "document_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "document_name",nullable = false)
	@NotNull(message = "document name is required")
	private String name;
	
	@Column(name = "document_type")
	@NotNull(message = "document type is required")
	private String type;
	
	@Column(name = "document_reference")
	private String documentReferences;
	
	@Column(name = "uploaded_date")
	private LocalDateTime uploadedDate;
	
	@ManyToOne
	@JoinColumn(name = "claim_id", nullable = false)
	private Claim claim;

}
