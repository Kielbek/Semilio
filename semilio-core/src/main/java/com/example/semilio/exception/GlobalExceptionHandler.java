package com.example.semilio.exception;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {

    private MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException ex, Locale locale) {
        ErrorCode errorCode = ex.getErrorCode();

        String key = "error." + errorCode.getCode().toLowerCase();

        String localizedMessage = messageSource.getMessage(
                key,
                null,
                errorCode.getDefaultMessage(),
                locale
        );

        Map<String, String> response = new HashMap<>();
        response.put("code", errorCode.getCode());
        response.put("message", localizedMessage);

        return new ResponseEntity<>(response, errorCode.getStatus());
    }
}
