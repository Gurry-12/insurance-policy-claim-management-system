package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.request.PlanRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PlanResponseDTO;
import com.insurance.demo.service.PolicyPlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PolicyPlanController {

    private final PolicyPlanService policyPlanService;

    //  ADMIN ONLY 

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Admin creates a new Policy Plan")
    public ApiResponseDTO<PlanResponseDTO> createPolicyPlan(@Valid @RequestBody PlanRequestDTO dto) {
        return policyPlanService.createPolicyPlan(dto);
    }

    @PutMapping("/update/{planId}")
    @Operation(summary = "Admin updates an existing Policy Plan")
    public ApiResponseDTO<PlanResponseDTO> updatePolicyPlan(
            @PathVariable Long planId,
            @Valid @RequestBody PlanRequestDTO dto) {
        return policyPlanService.updatePolicyPlan(planId, dto);
    }

    @PatchMapping("/deactivate/{planId}")
    @Operation(summary = "Admin deactivates a Policy Plan")
    public ApiResponseDTO<PlanResponseDTO> deactivatePolicyPlan(@PathVariable Long planId) {
        return policyPlanService.deactivatePolicyPlan(planId);
    }

    //  PUBLIC / ALL ROLES 

    @GetMapping("/active")
    @Operation(summary = "All users can view active Policy Plans")
    public ApiResponseDTO<List<PlanResponseDTO>> getAllActivePlans() {
        return policyPlanService.viewActivePlans();
    }

    @GetMapping("/product/{productId}/active")
    @Operation(summary = "View active plans under a specific Insurance Product")
    public ApiResponseDTO<List<PlanResponseDTO>> getActivePlansByProduct(@PathVariable Long productId) {
        return policyPlanService.viewActivePlansUnderInsuranceProduct(productId);
    }

    // PAGINATION (Admin/Agent) 

    @GetMapping("/page")
    @Operation(summary = "Paginated list of active Policy Plans")
    public PageResponseDTO<PlanResponseDTO> getAllPlansWithPagination(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        return policyPlanService.getAllPlansWithPagination(pageNumber, pageSize, sortBy, sortDirection);
    }

    @GetMapping("/{planId}")
    @Operation(summary = "Get details of a specific Policy Plan")
    public ApiResponseDTO<PlanResponseDTO> getPlanById(@PathVariable Long planId) {
        
        return policyPlanService.getPlanById(planId); 
    }
}