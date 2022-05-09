package com.scb.job.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) 
public class ResourceNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 3915686765945468468L;

	public ResourceNotFoundException(String message)   
	{  
		super(message);  
	}  
}
