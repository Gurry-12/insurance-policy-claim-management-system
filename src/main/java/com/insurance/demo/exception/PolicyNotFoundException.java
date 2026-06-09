package com.insurance.demo.exception;

public class PolicyNotFoundException extends RuntimeException {

	public PolicyNotFoundException(Long id) {
		super("Policy not found with id : " + id);
	}
}