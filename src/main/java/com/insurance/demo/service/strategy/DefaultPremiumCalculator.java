package com.insurance.demo.service.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.insurance.demo.dto.PremiumCalculationRequest;
import com.insurance.demo.dto.PremiumQuote;
import com.insurance.demo.model.PricingRule;

@Component("DEFAULT_CALCULATOR")
public class DefaultPremiumCalculator implements PremiumCalculator {

	@Override
	public PremiumQuote calculatePremium(PremiumCalculationRequest request, PricingRule rule, BigDecimal coverageAmount) {
		
		// Base premium calculation
		BigDecimal riskRate = rule.getBaseRiskRate();
		BigDecimal basePremium = coverageAmount.multiply(riskRate).setScale(2, RoundingMode.HALF_UP);
		
		// Adjust for duration (annualized rate simplified here)
		BigDecimal annualPremium = basePremium.divide(BigDecimal.valueOf(request.getDuration()), 2, RoundingMode.HALF_UP);
		
		// Add processing fee
		BigDecimal processingFee = rule.getProcessingFee();
		
		// Calculate taxable amount
		BigDecimal taxableAmount = annualPremium.add(processingFee);
		
		// Calculate GST
		BigDecimal gstPercentage = rule.getGst();
		BigDecimal gstAmount = taxableAmount.multiply(gstPercentage).divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP);
		
		// Total Premium
		BigDecimal totalPremium = taxableAmount.add(gstAmount);
		
		return PremiumQuote.builder()
				.selectedCoverage(coverageAmount)
				.duration(request.getDuration())
				.premiumType(request.getPremiumType())
				.annualPremium(annualPremium)
				.processingFee(processingFee)
				.gst(gstAmount)
				.totalPremium(totalPremium)
				.build();
	}

}
