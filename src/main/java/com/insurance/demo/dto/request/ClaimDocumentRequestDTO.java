package com.insurance.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocumentRequestDTO {

    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotBlank(message = "Document type is required")
    private String documentType;

    private String documentReference;   // File name, URL or reference
}