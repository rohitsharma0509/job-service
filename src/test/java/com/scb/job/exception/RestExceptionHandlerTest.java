package com.scb.job.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import java.util.HashSet;

import javax.validation.ConstraintViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.scb.job.model.exception.ErrorResponse;

@ExtendWith(MockitoExtension.class)
public class RestExceptionHandlerTest {

  private RestExceptionHandler restExceptionHandler= new RestExceptionHandler();


  @Test
  public void handleValidationExceptionTest(){

    WebRequest webRequest = Mockito.mock(WebRequest.class);

    ResponseEntity<ErrorResponse> responseEntity =  restExceptionHandler
        .handleValidationException(new EstimatePriceException("100", "error"), webRequest);

    assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    assertEquals("100", responseEntity.getBody().getErrorCode());

  }

  @Test
  void handleMissingServletRequestParameter_Test() {
       MissingServletRequestParameterException ex = new MissingServletRequestParameterException("parameterName", "parameterType");
       HttpHeaders headers = mock(HttpHeaders.class);
       WebRequest request =  mock(WebRequest.class);
       ResponseEntity<Object> response = restExceptionHandler.handleMissingServletRequestParameter(ex, headers, HttpStatus.CONTINUE, request);
       assertEquals(BAD_REQUEST, response.getStatusCode());
  }
  

  @Test
  void handleConstraintViolation_Test() {
       ConstraintViolationException ex = new ConstraintViolationException("message", new HashSet<>());
       ResponseEntity<Object> response = restExceptionHandler.handleConstraintViolation(ex);
       assertEquals(BAD_REQUEST, response.getStatusCode());
  }
  
  
  @Test
  void handleNoHandlerFoundException_Test() {
	  HttpHeaders headers = mock(HttpHeaders.class);
	  WebRequest request =  mock(WebRequest.class);
	  NoHandlerFoundException ex=mock(NoHandlerFoundException.class);
//      NoHandlerFoundException ex = new NoHandlerFoundException("GET", "requestURL", headers);
      ResponseEntity<Object> response = restExceptionHandler.handleNoHandlerFoundException(ex, headers, HttpStatus.CONTINUE, request);
      assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void handleDataIntegrityViolation_Test() {
	  WebRequest request =  mock(WebRequest.class);
      JobCreationException ex = new JobCreationException();
      ResponseEntity<Object> response = restExceptionHandler.handleDataIntegrityViolation(ex, request);
      assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void handleLocationDeliveryListException_Test() {
	  WebRequest request =  mock(WebRequest.class);
      LocationDeliveryListException ex = new LocationDeliveryListException();
      ResponseEntity<Object> response = restExceptionHandler.handleLocationDeliveryListException(ex, request);
      assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void handleMethodArgumentTypeMismatch_Test() {
		WebRequest request =  mock(WebRequest.class);
		MethodArgumentTypeMismatchException ex =  mock(MethodArgumentTypeMismatchException.class);
	    ResponseEntity<Object> response = restExceptionHandler.handleMethodArgumentTypeMismatch(ex, request);
	    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void handleValidationException_Test() {
	  WebRequest request =  mock(WebRequest.class);
      JobRequestFieldException ex = new JobRequestFieldException("message");
      ResponseEntity<Object> response = restExceptionHandler.handleValidationException(ex, request);
      assertEquals(BAD_REQUEST, response.getStatusCode());
  }  
  
  
  @Test
  void handleDataNotFoundException_Test() {
	  WebRequest request =  mock(WebRequest.class);
	  DataNotFoundException ex = new DataNotFoundException("message");
      ResponseEntity<Object> response = restExceptionHandler.handleDataNotFoundException(ex, request);
      assertEquals(BAD_REQUEST, response.getStatusCode());
  }  
  
  @Test
  void handleResourceNotFoundException_Test() {
	  WebRequest request =  mock(WebRequest.class);
	  ResourceNotFoundException ex = new ResourceNotFoundException("message");
      ResponseEntity<Object> response = restExceptionHandler.handleResourceNotFoundException(ex, request);
      assertEquals(NOT_FOUND, response.getStatusCode());
  }  
  
}
