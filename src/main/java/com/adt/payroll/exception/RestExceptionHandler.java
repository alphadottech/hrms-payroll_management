package com.adt.payroll.exception;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.adt.payroll.errorResponse.ApiError;
import com.adt.payroll.errorResponse.ErrorResponse;
import com.adt.payroll.errorResponse.FieldErrors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @Autowired
    private MessageSource messageSource;

    //Handle All Exceptions
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        String error = ex.getMessage();
        ApiError errors = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, error, ex);
        ErrorResponse errorResponse = new ErrorResponse(errors.getStatus().value(), errors.getMessage(), errors.getTimestamp());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, 
    		HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = "Malformed JSON request";
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, error, ex));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    //other exception handlers below
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(
            EntityNotFoundException ex) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<?> handleAccessDeniedException(
            AccessDeniedException ex) {
        String message = messageSource.getMessage("api.error.access.denied", null, Locale.ENGLISH);
        ApiError errors = new ApiError(HttpStatus.FORBIDDEN,message, ex);
        ErrorResponse errorResponse = new ErrorResponse(errors.getStatus().value(), errors.getMessage(), errors.getTimestamp());
        return new ResponseEntity<>(errorResponse, errors.getStatus());
    }

    @ExceptionHandler({ NoSuchFieldException.class })
    public ResponseEntity<?> handleNoSuchFieldException(NoSuchFieldException ex) {
        String error = ex.getLocalizedMessage();
        ApiError errors = new ApiError(HttpStatus.NOT_FOUND,error, ex);
        ErrorResponse errorResponse = new ErrorResponse(errors.getStatus().value(), errors.getMessage(), errors.getTimestamp());
        return new ResponseEntity<>(errorResponse, errors.getStatus());
    }

    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();

        String message = messageSource.getMessage("api.error.validation", null, Locale.ENGLISH);
        for(FieldError fieldError: fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        for (ObjectError objectError : globalErrors) {
            errors.put(objectError.getObjectName(), objectError.getDefaultMessage());
        }

        ApiError errorResponse = new ApiError(HttpStatus.BAD_REQUEST, message, ex);
        FieldErrors fieldError = new FieldErrors(errorResponse.getStatus().value(), message, errors);
        return new ResponseEntity<>(fieldError, errorResponse.getStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        ApiError errors = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(errors.getStatus().value(), errors.getMessage(), errors.getTimestamp());
        return new ResponseEntity<>(errorResponse, errors.getStatus());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleNullPointerException(NullPointerException ex) {
        String message = ex.getLocalizedMessage();
        ApiError errors = new ApiError(HttpStatus.BAD_REQUEST,message, ex);
        ErrorResponse errorResponse = new ErrorResponse(errors.getStatus().value(), errors.getMessage(), errors.getTimestamp());
        return new ResponseEntity<>(errorResponse, errors.getStatus());
    }

//    @ExceptionHandler({BadCredentialsException.class})
//    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
//        String message = ex.getLocalizedMessage();
//        ApiError errors = new ApiError(HttpStatus.BAD_REQUEST, message, ex);
//        ErrorResponse errorResponse = new ErrorResponse(errors.getStatus().value(), errors.getMessage(), errors.getTimestamp());
//        return new ResponseEntity<>(errorResponse, errors.getStatus());
//    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Object> handleSqlException(SQLException ex) {
        String message = ex.getMessage();
        ApiError errors = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message, ex);
        ErrorResponse errorResponse = new ErrorResponse(errors.getStatus().value(), errors.getMessage(), errors.getTimestamp());
        return new ResponseEntity<>(errorResponse, errors.getStatus());
    }
}

