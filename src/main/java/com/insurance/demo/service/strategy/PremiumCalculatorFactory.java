package com.insurance.demo.service.strategy;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.insurance.demo.enums.ProductType;

@Component
public class PremiumCalculatorFactory {

	private final Map<String, PremiumCalculator> calculators;

	@Autowired
	public PremiumCalculatorFactory(Map<String, PremiumCalculator> calculators) {
		this.calculators = calculators;
	}

	public PremiumCalculator getCalculator(ProductType productType) {
		String beanName = productType.name() + "_CALCULATOR";
		PremiumCalculator calculator = calculators.get(beanName);
		if (calculator == null) {
			return calculators.get("DEFAULT_CALCULATOR");
		}
		return calculator;
	}
}
