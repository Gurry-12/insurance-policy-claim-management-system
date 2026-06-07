package com.insurance.demo.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.demo.dto.request.PaymentRequestDTO;
import com.insurance.demo.dto.response.ApiResponseDTO;
import com.insurance.demo.dto.response.PageResponseDTO;
import com.insurance.demo.dto.response.PaymentResponseDTO;
import com.insurance.demo.dto.response.ProductResponseDTO;
import com.insurance.demo.enums.PaymentStatus;
import com.insurance.demo.enums.PolicyStatus;
import com.insurance.demo.exception.BadRequestException;
import com.insurance.demo.exception.DuplicateResourceException;
import com.insurance.demo.exception.ResourceNotFoundException;
import com.insurance.demo.model.InsuranceProduct;
import com.insurance.demo.model.Policy;
import com.insurance.demo.model.PremiumPayment;
import com.insurance.demo.repository.PolicyRepository;
import com.insurance.demo.repository.PremiumPaymentRepository;
import com.insurance.demo.service.PremiumPaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremiumPaymentServiceImpl implements PremiumPaymentService {

	@Autowired
	private PremiumPaymentRepository paymentRepository;

	@Autowired
	private PolicyRepository policyRepository;

	@Autowired
	private ModelMapper modelMapper;

	
	
	@Override
	@jakarta.transaction.Transactional
	public ApiResponseDTO<PaymentResponseDTO> recordPayment(@Valid PaymentRequestDTO dto) {

		log.info("Recording payment for policy id: {}", dto.getId());

		// Validate policy exists
		Policy policy = policyRepository.findById(dto.getId()).orElseThrow(() -> {
			log.error("Policy not found with id: {}", dto.getId());
			return new ResourceNotFoundException("Policy not found");
		});

		// prevent duplicate payment reference
		if (paymentRepository.existsByTransactionReference(dto.getTransactionReference())) {

			log.warn("Duplicate transaction reference detected: {}", dto.getTransactionReference());
			throw new DuplicateResourceException("Transaction reference already exists");
		}

		PremiumPayment payment = modelMapper.map(dto, PremiumPayment.class);

		payment.setPolicy(policy);
		payment.setAmount(dto.getAmount());
		payment.setPaymentMode(dto.getPaymentMode());
		payment.setTransactionReference(dto.getTransactionReference());

		payment.setPaymentStatus(PaymentStatus.SUCCESS);
		payment.setPaymentDate(LocalDateTime.now());

		PremiumPayment savedPayment = paymentRepository.save(payment);

		log.info("Payment saved successfully with id: {}", savedPayment.getId());

		// update total premium paid after successfull payment
		policy.setTotalPremiumPaid(policy.getTotalPremiumPaid() + dto.getAmount());

		Double requiredPremium = policy.getPolicyPlan().getPremiumAmount();

		// activate policy after payment
		if (policy.getTotalPremiumPaid() >= requiredPremium) {

			policy.setPolicyStatus(PolicyStatus.ACTIVE);

			log.info("Policy {} activated. Total paid: {}, Required premium: {}", policy.getId(),
					policy.getTotalPremiumPaid(), requiredPremium);
		}

		policyRepository.save(policy);

		log.info("Payment processing completed for policy id: {}", policy.getId());

		PaymentResponseDTO response = modelMapper.map(savedPayment, PaymentResponseDTO.class);

		return new ApiResponseDTO<>("Payment Recorded Successfully", true, response, LocalDateTime.now());

	}

	
	//Get Payment By Policy ID
	@Override
	@Transactional(readOnly = true)
	public ApiResponseDTO<List<PaymentResponseDTO>> getPaymentsByPolicy(Long id) {

		log.info("Fetching payments for policy id: {}", id);

		List<PremiumPayment> payments = paymentRepository.findByPolicyId(id);

		if (payments.isEmpty()) {
			log.warn("No payments found for policy id: {}", id);
			throw new ResourceNotFoundException("Payments not found for policy id " + id);
		}

		List<PaymentResponseDTO> responseList = payments.stream()
				.map(payment -> modelMapper.map(payment, PaymentResponseDTO.class)).toList();

		log.info("Found {} payments for policy id: {}", responseList.size(), id);

		return new ApiResponseDTO<>("Payments fetched successfully", true, responseList, LocalDateTime.now());
	}

	
	
	
	
	//  Get Payment By PaymentId
	@Override
	public ApiResponseDTO<PaymentResponseDTO> getPaymentById(Long paymentId) {
		log.info("Fetching payments by paymentid: {}", paymentId);

		Optional<PremiumPayment> payment = paymentRepository.findById(paymentId);

		if (payment.isEmpty()) {
			log.warn("No payments found for paymentId: {}", paymentId);
			throw new ResourceNotFoundException("Payment not found for paymentId " + paymentId);
		}

		PaymentResponseDTO response = modelMapper.map(payment, PaymentResponseDTO.class);
		log.info("Found {} payments for policy id: {}", response, paymentId);

		return new ApiResponseDTO<>("Payments fetched successfully", true, response, LocalDateTime.now());

	}

	@Override
	@Transactional
	public PageResponseDTO<PaymentResponseDTO> getAllPaymentsWithPagination(int pageNumber, int pageSize, String sortBy,String sortDirection ) {

			log.info("Fetching Users with pagination. pageNumber: {}, pageSize: {}, sortBy: {}, sortDirection: {}",
					pageNumber, pageSize, sortBy, sortDirection);
			validatePagination(pageNumber, pageSize);
			validateUserSortField(sortBy);
			
			Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(getSortDirection(sortDirection), sortBy));
			Page<PremiumPayment> paymentPage = paymentRepository.findAll(pageable);
			
			List<PaymentResponseDTO> content = paymentPage.getContent().stream()
					.map(payment -> modelMapper.map(payment, PaymentResponseDTO.class)).toList();
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

		private void validateUserSortField(String sortBy) {
			if (!List.of("id", "productName", "productType").contains(sortBy)) {
				throw new BadRequestException("Invalid sort field for product: " + sortBy);
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

	

}
