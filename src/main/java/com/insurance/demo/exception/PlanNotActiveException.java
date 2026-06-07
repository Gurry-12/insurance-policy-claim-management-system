package com.insurance.demo.exception;

public class PlanNotActiveException extends RuntimeException {

	public PlanNotActiveException() {
		super("Selected plan is not active");
	}
}