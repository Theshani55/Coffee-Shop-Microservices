package com.ioidigital.orderservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String RESOURCE_EXCEPTION = "Resource Exception : ";
    private static final String ORDER_EXCEPTION = "ORDER Exception : ";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        LOGGER.error(RESOURCE_EXCEPTION + ex.getMessage());
        final ErrorResponse errorInfo = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorInfo, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderException(InvalidOrderException ex) {
        LOGGER.error(ORDER_EXCEPTION + ex.getMessage());
        final ErrorResponse errorInfo = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorInfo, HttpStatus.BAD_REQUEST);

    }


}
