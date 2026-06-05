package com.insurence.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.demo.model.ClaimDocument;
@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Integer>{

}
