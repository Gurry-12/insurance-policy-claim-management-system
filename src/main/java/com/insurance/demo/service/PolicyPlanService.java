package com.insurance.demo.service;

import com.insurance.demo.dto.request.PlanRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PlanResponseDTO;

import java.util.List;

public interface PolicyPlanService {

    ApiResponseDTO<PlanResponseDTO> createPolicyPlan(PlanRequestDTO planRequestDTO);

    ApiResponseDTO<PlanResponseDTO> updatePolicyPlan(Long planId, PlanRequestDTO planRequestDTO);

    ApiResponseDTO<PlanResponseDTO> deactivatePolicyPlan(Long planId);

    ApiResponseDTO<PlanResponseDTO> activatePolicyPlan(Long planId);

    ApiResponseDTO<List<PlanResponseDTO>> viewActivePlans();

    ApiResponseDTO<List<PlanResponseDTO>> viewActivePlansUnderInsuranceProduct(Long productId);

    PageResponseDTO<PlanResponseDTO> getAllPlansWithPagination(int pageNumber, int pageSize, 
                                                              String sortBy, String sortDirection, Long productId, Boolean isActive);

	ApiResponseDTO<PlanResponseDTO> getPlanById(Long planId);
}