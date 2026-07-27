package com.insurance.demo.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.insurance.demo.dto.PremiumCalculationRequest;
import com.insurance.demo.dto.PremiumQuote;
import com.insurance.demo.model.PricingRule;

@Component("HEALTH_CALCULATOR")
public class HealthPremiumCalculator implements PremiumCalculator {

	@Override
	public PremiumQuote calculatePremium(PremiumCalculationRequest request, PricingRule rule, BigDecimal coverageAmount) {
		
		BigDecimal riskRate = rule.getBaseRiskRate();
		
		// Base premium calculation
		BigDecimal basePremium = coverageAmount.multiply(riskRate).setScale(2, RoundingMode.HALF_UP);
		
		BigDecimal durationFactor = BigDecimal.valueOf(request.getDuration());
		BigDecimal adjustedPremium = basePremium.divide(durationFactor, 2, RoundingMode.HALF_UP);
		
		BigDecimal processingFee = rule.getProcessingFee();
		
		BigDecimal taxableAmount = adjustedPremium.add(processingFee);
		
		BigDecimal gstPercentage = rule.getGst();
		BigDecimal gstAmount = taxableAmount.multiply(gstPercentage).divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
		
		BigDecimal totalPremium = taxableAmount.add(gstAmount);

		return PremiumQuote.builder()
				.selectedCoverage(coverageAmount)
				.duration(request.getDuration())
				.premiumType(request.getPremiumType())
				.annualPremium(adjustedPremium)
				.processingFee(processingFee)
				.gst(gstAmount)
				.totalPremium(totalPremium)
				.build();
	}
}
