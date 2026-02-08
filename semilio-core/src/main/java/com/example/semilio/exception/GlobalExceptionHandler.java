package com.example.semilio.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponse response = new ErrorResponse(
                errorCode.getCode(),
                errorCode.getDefaultMessage()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    public record ErrorResponse(String code, String message) {
    }
}
