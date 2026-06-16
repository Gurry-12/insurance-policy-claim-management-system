package com.insurance.demo.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.PaymentRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PaymentResponseDTO;
import com.insurance.demo.enums.PaymentStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.AppUser;
import com.insurance.demo.model.Policy;
import com.insurance.demo.model.PremiumPayment;
import com.insurance.demo.repository.AppUserRepository;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.repository.PremiumPaymentRepository;
import com.insurance.demo.service.PremiumPaymentService;
import com.insurance.demo.util.TransactionReferenceGenerator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremiumPaymentServiceImpl implements PremiumPaymentService {

	private final PremiumPaymentRepository paymentRepository;
	private final PolicyRepository policyRepository;
	private final ModelMapper modelMapper;
	private final AppUserRepository userRepository;

	@Override
	@Transactional
	public ApiResponseDTO<PaymentResponseDTO> recordPayment(PaymentRequestDTO dto) {

	    log.info("Recording payment for policy id: {}", dto.getPolicyId());

	    Policy policy = policyRepository.findById(dto.getPolicyId())
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Policy not found with id: " + dto.getPolicyId()));

	    String email = SecurityContextHolder.getContext().getAuthentication().getName();

	    boolean isCustomer = SecurityContextHolder.getContext()
	            .getAuthentication()
	            .getAuthorities()
	            .stream()
	            .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

	    if (isCustomer && !policy.getCustomer().getUser().getEmail().equals(email)) {
	        throw new AccessDeniedException("You are not allowed to record payment for another customer's policy");
	    }

	    if (policy.getPolicyPlan().getPremiumAmount().compareTo(dto.getAmount()) != 0) {
	        throw new BadRequestException("Payment amount must match premium amount");
	    }
	    
	    if(PolicyStatus.CANCELLED.equals(policy.getPolicyStatus())) {
	    	throw new BadRequestException("you are restricted to make payment for a cancelled policy");
	    }
	    
	    if(PolicyStatus.EXPIRED.equals(policy.getPolicyStatus())) {
	    	throw new BadRequestException("you are restricted to make payment for a expired policy");
	    }

	    String transactionReferance = TransactionReferenceGenerator.generateTransactionReference();
	    
	    if (paymentRepository.existsByTransactionReference(transactionReferance)) {
	        throw new DuplicateResourceException("Transaction reference already exists");
	    }
	    
	    if(policy.getPolicyPlan().getCoverageAmount() <= (policy.getTotalPremiumPaid() + dto.getAmount())) {
	    	throw new BadRequestException("Required premium already paid. Policy is active.");
	    }

	    PremiumPayment payment = new PremiumPayment();
	    payment.setAmount(dto.getAmount());
	    payment.setPaymentMode(dto.getPaymentMode());
	    payment.setTransactionReference(transactionReferance);
	    payment.setPolicy(policy);
	    payment.setPaymentDate(LocalDateTime.now());

	    if(PaymentStatus.SUCCESS.equals(dto.getPaymentStatus())) {
	    payment.setPaymentStatus(PaymentStatus.SUCCESS);
	    }
	    
	    if(PaymentStatus.FAILED.equals(dto.getPaymentStatus())) {
		    payment.setPaymentStatus(PaymentStatus.FAILED);
		    }

	    PremiumPayment savedPayment = paymentRepository.save(payment);

	    if(PaymentStatus.SUCCESS.equals(dto.getPaymentStatus())) {
	    	policy.setTotalPremiumPaid(policy.getTotalPremiumPaid() + dto.getAmount());
		    policy.setPolicyStatus(PolicyStatus.ACTIVE);
		    }
	    
	    policyRepository.save(policy);

	    PaymentResponseDTO responseDTO = modelMapper.map(savedPayment, PaymentResponseDTO.class);
	    responseDTO.setPolicyNumber(policy.getPolicyNumber());

	    return new ApiResponseDTO<>("Payment recorded successfully", true, responseDTO, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<PaymentResponseDTO>> getPaymentsByPolicy(Long id) {
		log.info("Fetching payments by policy: {}", id);

		List<PremiumPayment> list = paymentRepository.findByPolicyId(id);

		List<PaymentResponseDTO> responseList = list.stream()
				.map(payment -> {
					PaymentResponseDTO dto = modelMapper.map(payment, PaymentResponseDTO.class);
					dto.setPolicyNumber(payment.getPolicy().getPolicyNumber());
					return dto;
				}).toList();

		return new ApiResponseDTO<>("Payments fetched successfully", true, responseList, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<PaymentResponseDTO> getPaymentById(Long paymentId) {
		log.info("Fetching payments by paymentid: {}", paymentId);

		PremiumPayment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		boolean isCustomer = authentication.getAuthorities()
				.stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

		if (isCustomer && (payment.getPolicy() == null || payment.getPolicy().getCustomer() == null ||
				payment.getPolicy().getCustomer().getUser() == null ||
				!payment.getPolicy().getCustomer().getUser().getEmail().equals(email))) {
			throw new AccessDeniedException("You are not allowed to view this payment");
		}

		PaymentResponseDTO responseDTO = modelMapper.map(payment, PaymentResponseDTO.class);
		responseDTO.setPolicyNumber(payment.getPolicy().getPolicyNumber());

		return new ApiResponseDTO<>("Payment fetched successfully", true, responseDTO, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponseDTO<PaymentResponseDTO> getAllPaymentsWithPagination(int pageNumber, int pageSize, String sortBy,
			String sortDirection, Long policyId, String paymentStatus) {

		log.info("Fetching Payments with pagination. pageNumber: {}, pageSize: {}, sortBy: {}, sortDirection: {}, policyId: {}, status: {}",
				pageNumber, pageSize, sortBy, sortDirection, policyId, paymentStatus);
		validatePagination(pageNumber, pageSize);
		validatePaymentSortField(sortBy);

		com.insurance.demo.enums.PaymentStatus statusEnum = null;
		if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
			try {
				statusEnum = com.insurance.demo.enums.PaymentStatus.valueOf(paymentStatus.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new BadRequestException("Invalid payment status filter: " + paymentStatus);
			}
		}

		Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(getSortDirection(sortDirection), sortBy));
		Page<PremiumPayment> paymentPage;
		if (policyId != null && statusEnum != null) {
			paymentPage = paymentRepository.findByPolicyIdAndPaymentStatus(policyId, statusEnum, pageable);
		} else if (policyId != null) {
			paymentPage = paymentRepository.findByPolicyId(policyId, pageable);
		} else if (statusEnum != null) {
			paymentPage = paymentRepository.findByPaymentStatus(statusEnum, pageable);
		} else {
			paymentPage = paymentRepository.findAll(pageable);
		}

		List<PaymentResponseDTO> content = paymentPage.getContent().stream()
				.map(payment -> {
					PaymentResponseDTO dto = modelMapper.map(payment, PaymentResponseDTO.class);
					dto.setPolicyNumber(payment.getPolicy().getPolicyNumber());
					return dto;
				}).toList();
		return new PageResponseDTO<>(content, paymentPage.getNumber(), paymentPage.getSize(),
				paymentPage.getTotalElements(), paymentPage.getTotalPages(), paymentPage.isLast(), sortDirection);
	}


	private Direction getSortDirection(String sortDirection) {
		if (sortDirection == null || sortDirection.equalsIgnoreCase("asc"))
			return Sort.Direction.ASC;
		if (sortDirection.equalsIgnoreCase("desc"))
			return Sort.Direction.DESC;
		throw new BadRequestException("Sort direction must be asc or desc.");
	}

	private void validatePaymentSortField(String sortBy) {
		if (!List.of("id", "amount", "paymentDate", "paymentMode", "paymentStatus").contains(sortBy)) {
			throw new BadRequestException("Invalid sort field for payment: " + sortBy);
		}
	}

	private void validatePagination(int pageNumber, int pageSize) {
		if (pageNumber < 0)
			throw new BadRequestException("Page number cannot be negative.");
		if (pageSize <= 0)
			throw new BadRequestException("Page size must be greater than 0.");
		if (pageSize > 100)
			throw new BadRequestException("Page size cannot be greater than 100.");
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<PaymentResponseDTO>> getMyPayments() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		AppUser user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		log.info("Fetching payment history for customer email: {}", email);
		List<PremiumPayment> payments = paymentRepository.findByPolicyCustomerUserId(user.getId());

		List<PaymentResponseDTO> responseList = payments.stream()
				.map(payment -> {
					PaymentResponseDTO dto = modelMapper.map(payment, PaymentResponseDTO.class);
					dto.setPolicyNumber(payment.getPolicy().getPolicyNumber());
					return dto;
				}).toList();

		return new ApiResponseDTO<>("Payment history fetched successfully", true, responseList, LocalDateTime.now());
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<PaymentResponseDTO>> getPaymentsByMyPolicy(Long policyId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		AppUser user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		log.info("Fetching payments for policy ID: {} by customer email: {}", policyId, email);
		List<PremiumPayment> payments = paymentRepository.findByPolicyIdAndPolicyCustomerUserId(policyId, user.getId());

		List<PaymentResponseDTO> responseList = payments.stream()
				.map(payment -> {
					PaymentResponseDTO dto = modelMapper.map(payment, PaymentResponseDTO.class);
					dto.setPolicyNumber(payment.getPolicy().getPolicyNumber());
					return dto;
				}).toList();

		return new ApiResponseDTO<>("Payments for policy fetched successfully", true, responseList, LocalDateTime.now());
	}

}
