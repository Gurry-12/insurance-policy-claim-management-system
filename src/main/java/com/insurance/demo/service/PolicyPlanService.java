package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.request.PlanRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PlanResponseDTO;

public interface PolicyPlanService {

	ApiResponseDTO<PlanResponseDTO> createPolicyPlan(PlanRequestDTO planRequestDTO);

	ApiResponseDTO<PlanResponseDTO> updatePolicyPlan(PlanRequestDTO planRequestDTO);

	ApiResponseDTO<PlanResponseDTO> deactivatePolicyPlan(Long planId);

	ApiResponseDTO<List<PlanResponseDTO>> viewActivePlans();

	ApiResponseDTO<List<PlanResponseDTO>> viewActivePlansUnderInsuranceProduct(Long productId);

	PageResponseDTO<PlanResponseDTO> getAllActivePlansWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection);
}
