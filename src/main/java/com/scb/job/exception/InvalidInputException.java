package com.scb.job.exception;

/**
 * Custom runtime exception for invalid input
 * 
 * @Exception invalid input
 *
 */
public class InvalidInputException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidInputException(String message) {
		super(message);
	}

}
