package com.scb.job.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.scb.job.constants.ResponseCodeConstants;
import com.scb.job.model.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR = "error";
    
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";

        return new ResponseEntity<>(ErrorResponse.builder().errorCode(ResponseCodeConstants.INVALID_REQUEST_PARAMETER)
            .errorMessage(error)
            .build(),BAD_REQUEST);

    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        return new ResponseEntity<>(ErrorResponse.builder().errorCode(ResponseCodeConstants.INVALID_REQUEST_PARAMETER)
            .errorMessage(ex.getBindingResult().getFieldError().getField().concat(" "+ex.getBindingResult().getFieldError().getDefaultMessage()))
            .build(),BAD_REQUEST);


    }


    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(
            javax.validation.ConstraintViolationException ex) {

        return new ResponseEntity<>(ErrorResponse.builder().errorCode(ResponseCodeConstants.CREDENTIAL_ERROR)
            .errorMessage(ex.getConstraintViolations().toString())
            .build(),BAD_REQUEST);

    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        log.info("{} to {}", servletWebRequest.getHttpMethod(), servletWebRequest.getRequest().getServletPath());
        String error = "Malformed JSON request";

        return new ResponseEntity<>(ErrorResponse.builder().errorCode(ResponseCodeConstants.CREDENTIAL_ERROR)
            .errorMessage(error)
            .build(),BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        return new ResponseEntity<>(ErrorResponse.builder().errorCode(ResponseCodeConstants.UNEXPECTED_EXCEPTION_ERROR)
            .errorMessage("Unexpected Exception has occured")
            .build(),INTERNAL_SERVER_ERROR);

    }

    @ExceptionHandler({JobCreationException.class,DataIntegrityViolationException.class})
    protected ResponseEntity<Object> handleDataIntegrityViolation(JobCreationException ex,
                                                                  WebRequest request) {

        return new ResponseEntity<>(ErrorResponse.builder().errorCode(ResponseCodeConstants.CREATE_JOB_FAILED)
            .errorMessage("Error while creating Job in database")
            .build(),INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({LocationDeliveryListException.class})
    protected ResponseEntity<Object> handleLocationDeliveryListException(LocationDeliveryListException ex,
        WebRequest request) {

        return new ResponseEntity<>(ErrorResponse.builder().errorCode(ResponseCodeConstants.LOCATION_LIST_ERROR)
            .errorMessage("Location delivery list size should be 2")
            .build(),BAD_REQUEST);

    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                      WebRequest request) {

        return new ResponseEntity<>(ErrorResponse.builder().errorCode(ResponseCodeConstants.INVALID_REQUEST_PARAMETER)
            .errorMessage(ex.getMessage())
            .build(),BAD_REQUEST);
    }

    @ExceptionHandler(JobRequestFieldException.class)
    protected ResponseEntity<Object> handleValidationException(JobRequestFieldException ex,
        WebRequest request) {

        return new ResponseEntity<>(ErrorResponse.builder().errorCode(ResponseCodeConstants.INVALID_REQUEST_PARAMETER)
            .errorMessage(ex.getMessage())
            .build(),BAD_REQUEST);
    }

    @ExceptionHandler(EstimatePriceException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(EstimatePriceException ex,
        WebRequest request) {

        return new ResponseEntity<>(ErrorResponse.builder()
            .errorCode(ex.getErrorCode())
            .errorMessage(ex.getErrorMessage())
            .build(),INTERNAL_SERVER_ERROR);
    }
    
    
    @ExceptionHandler({DataNotFoundException.class})
    public ResponseEntity<Object> handleDataNotFoundException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        logger.error(ERROR, ex);
        
        return new ResponseEntity<>(ErrorResponse.builder()
            .errorCode(HttpStatus.NOT_FOUND.name())
            .errorMessage(ex.getMessage())
            .build(),BAD_REQUEST);
        
    }
    
    
    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<Object> handleResourceNotFoundException(final Exception ex, final WebRequest request) {
        logger.info(ex.getMessage());
        
        return new ResponseEntity<>(ErrorResponse.builder()
            .errorCode(HttpStatus.NOT_FOUND.name())
            .errorMessage(ex.getMessage())
            .build(),HttpStatus.NOT_FOUND);
        
    }

    @ExceptionHandler(SubDistrictNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleSubDistrictNotFoundException(SubDistrictNotFoundException ex,
                                                                      WebRequest request) {

        return new ResponseEntity<>(ErrorResponse.builder()
                .errorCode("100")
                .errorMessage("Sub District Not found")
                .build(),INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler({InvalidJobStateException.class})
    public ResponseEntity<Object> handleInvalidJobStateException(final Exception ex, final WebRequest request) {
        logger.info(ex.getClass().getName());
        return new ResponseEntity<>(ErrorResponse.builder()
            .errorCode(HttpStatus.EXPECTATION_FAILED.name())
            .errorMessage(ex.getMessage())
            .build(),HttpStatus.NOT_FOUND);
    }
}
