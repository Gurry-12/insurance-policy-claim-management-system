package com.insurance.demo.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.insurance.demo.model.ClaimDocument;
import com.insurance.demo.service.ClaimDocumentService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/document")
@AllArgsConstructor
public class ClaimDocumentController {
	
	private final ClaimDocumentService claimDocumentService;
	
	@PostMapping(value = "/upload/{claimId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ClaimDocument> uploadDocument(
	        @PathVariable Long claimId,
	        @RequestParam("file") MultipartFile file) throws IOException {

	    ClaimDocument document =
	            claimDocumentService.uploadDocument(claimId, file);

	    return ResponseEntity.ok(document);
	}
	
	
    @DeleteMapping("/{documentId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable Long documentId)
            throws IOException {

    	claimDocumentService.deleteDocument(documentId);

        return ResponseEntity.ok(
                "Document deleted successfully");
    }

}
