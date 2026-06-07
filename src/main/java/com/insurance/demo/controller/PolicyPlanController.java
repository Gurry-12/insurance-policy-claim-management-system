package com.insurance.demo.controller;

import com.insurance.demo.dto.request.PlanRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PlanResponseDTO;
import com.insurance.demo.service.PolicyPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
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

//    @GetMapping("/{planId}")
//    @Operation(summary = "Get details of a specific Policy Plan")
//    public ResponseEntity<PlanResponseDTO> getPlanById(@PathVariable Long planId) {
//        // You can implement getById in service if needed
//        // For now, returning from active plans logic or extend service
//        return ResponseEntity.ok().build(); // Extend service if required
//    }
}