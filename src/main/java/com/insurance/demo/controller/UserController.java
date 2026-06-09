package com.insurance.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.demo.dto.request.CreateAgentRequestDTO;
import com.insurance.demo.dto.request.UserStatusUpdateRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.UserResponseDTO;
import com.insurance.demo.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final UserService userService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponseDTO<List<UserResponseDTO>> viewAllUsers() {
		return userService.viewAllUsers();
	}

	@PatchMapping("/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponseDTO<UserResponseDTO> activateUser(
			@PathVariable Long id) {
		return userService.activateUser(id);
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponseDTO<UserResponseDTO> deactivateUser(
			@PathVariable Long id) {
		return userService.deactivateUser(id);
	}

	@PostMapping("/agent")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDTO<UserResponseDTO> createAgentUser(@Valid @RequestBody CreateAgentRequestDTO agentRequestDTO) {
		return userService.createAgentUser(agentRequestDTO);
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponseDTO<UserResponseDTO> findUserById(@PathVariable Long id){
		return userService.findUserById(id);
	}
	
	@GetMapping("/page")
	@PreAuthorize("hasRole('ADMIN')")
	public PageResponseDTO<UserResponseDTO> getAllUsersWithPagination(
			@RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
		return userService.getAllUsersWithPagination(pageNumber, pageSize, sortBy, sortDirection, role, isActive);
	}
}
