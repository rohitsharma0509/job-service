package com.scb.job.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Getter
public class ApiError
{
	private HttpStatus status;
	private String message;
	private List<String> errors;
	
	public ApiError(final HttpStatus status, final String message, final String error) {
		super();
		this.status = status;
		this.message=message;
		errors = Arrays.asList(error);
	}

}