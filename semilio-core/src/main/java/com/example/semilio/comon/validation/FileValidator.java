package com.example.semilio.comon.validation;

import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;

@Component
public class FileValidator {

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");

    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
    
}