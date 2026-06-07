package com.insurance.demo.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.ClaimDocumentRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.Claim;
import com.insurance.demo.model.ClaimDocument;
import com.insurance.demo.repository.ClaimDocumentRepository;
import com.insurance.demo.repository.ClaimRepository;
import com.insurance.demo.service.ClaimDocumentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimDocumentServiceImpl implements ClaimDocumentService {

    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository claimDocumentRepository;

    @Override
    @Transactional
    public ApiResponseDTO<String> addDocumentsToClaim(Long claimId, List<ClaimDocumentRequestDTO> documentDTOs) {

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        // Security Check - Only owner or agent/admin can add documents
        if (!claim.getPolicy().getCustomer().getUser().getEmail().equals(currentUserEmail)) {
            throw new BadRequestException("You can only add documents to your own claim");
        }

        if (documentDTOs == null || documentDTOs.isEmpty()) {
            throw new BadRequestException("At least one document is required");
        }

        for (ClaimDocumentRequestDTO docDTO : documentDTOs) {
            ClaimDocument document = new ClaimDocument();
            document.setClaim(claim);
            document.setName(docDTO.getDocumentName());
            document.setType(docDTO.getDocumentType());
            document.setDocumentReference(docDTO.getDocumentReference());
            document.setUploadedDate(LocalDateTime.now());

            claimDocumentRepository.save(document);
        }

        log.info("{} documents added to claim {}", documentDTOs.size(), claimId);

        return new ApiResponseDTO<>("Documents uploaded successfully", true, 
                documentDTOs.size() + " documents added", LocalDateTime.now());
    }
}