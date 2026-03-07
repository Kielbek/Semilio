package com.example.semilio.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {

    // --- AUTHENTICATION & USER ---
    ACCESS_DENIED("ACCESS_DENIED", "You are not a participant", FORBIDDEN),
    EMAIL_ALREADY_EXISTS("ERR_EMAIL_EXISTS", "Email already exists", CONFLICT),
    PHONE_ALREADY_EXISTS("ERR_PHONE_EXISTS", "Phone number already exists", CONFLICT),
    NICKNAME_ALREADY_EXISTS("NICKNAME_ALREADY_EXISTS", "Username is already taken", CONFLICT),
    PASSWORD_MISMATCH("ERR_PASSWORD_MISMATCH", "The password and confirmation do not match", BAD_REQUEST),
    CHANGE_PASSWORD_MISMATCH("ERR_PASSWORD_MISMATCH", "New password and confirmation do not match", BAD_REQUEST),
    INVALID_CURRENT_PASSWORD("INVALID_CURRENT_PASSWORD", "The current password is incorrect", BAD_REQUEST),
    ERR_USER_DISABLED("ERR_USER_DISABLED", "User account is disabled, please activate your account or contact the administrator", UNAUTHORIZED),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Username and/or password is incorrect", UNAUTHORIZED),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", NOT_FOUND),
    USERNAME_NOT_FOUND("USERNAME_NOT_FOUND", "Cannot find user with the provided username", NOT_FOUND),
    ACCOUNT_ALREADY_DEACTIVATED("ACCOUNT_ALREADY_DEACTIVATED", "Account has been deactivated", BAD_REQUEST),

    // --- PRODUCT & CATEGORY ---
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "The product with this id does not exist", NOT_FOUND),
    FORBIDDEN_ACTION("FORBIDDEN_ACTION", "You do not have permission to modify or delete this product", FORBIDDEN),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Category not found", NOT_FOUND),
    CATEGORY_ALREADY_EXISTS_FOR_USER("CATEGORY_ALREADY_EXISTS_FOR_USER", "Category already exists for this user", CONFLICT),

    // --- CHAT & MESSAGING ---
    CHAT_NOT_FOUND("CHAT_NOT_FOUND", "Chat not found!", NOT_FOUND),
    CHAT_ACCESS_DENIED("CHAT_ACCESS_DENIED", "You are not a participant of this chat", FORBIDDEN),
    INVALID_CHAT_REQUEST("INVALID_CHAT_REQUEST", "ChatId or ProductId must be provided", BAD_REQUEST),
    INVALID_MESSAGE_TYPE("INVALID_MESSAGE_TYPE", "Operation not allowed for this message type", BAD_REQUEST),
    MESSAGE_NOT_FOUND("MESSAGE_NOT_FOUND", "The requested message was not found", NOT_FOUND),
    PROPOSAL_ALREADY_PROCESSED("PROPOSAL_ALREADY_PROCESSED", "This offer has already been accepted or rejected", CONFLICT),

    // --- FILES & UPLOADS ---
    INVALID_FILE_TYPE("ERR_INVALID_FILE_TYPE", "The selected file must be an image (JPG, PNG, WEBP)", BAD_REQUEST),
    FILE_TOO_LARGE("ERR_FILE_TOO_LARGE", "The image is too large, maximum size is 15MB", BAD_REQUEST),
    FILE_EMPTY("ERR_FILE_EMPTY", "Cannot send an empty file", BAD_REQUEST),
    FILE_UPLOAD_ERROR("ERR_FILE_UPLOAD", "An error occurred while uploading the file", INTERNAL_SERVER_ERROR),

    // --- VERIFICATION & TOKENS ---
    INVALID_VERIFICATION_CODE("INVALID_VERIFICATION_CODE", "Invalid verification code", BAD_REQUEST),
    EXPIRED_VERIFICATION_CODE("EXPIRED_VERIFICATION_CODE", "Verification code has expired", BAD_REQUEST),
    INVALID_RESET_TOKEN("INVALID_RESET_TOKEN", "The password reset link is invalid", BAD_REQUEST),
    EXPIRED_RESET_TOKEN("EXPIRED_RESET_TOKEN", "The password reset link has expired", BAD_REQUEST),
    ERR_SENDING_ACTIVATION_EMAIL("ERR_SENDING_ACTIVATION_EMAIL", "An error occurred while sending the activation email", INTERNAL_SERVER_ERROR),

    // --- SYSTEM & UTILS ---
    SEARCH_QUERY_TOO_SHORT("SEARCH_QUERY_TOO_SHORT", "Search query is too short (minimum 2 characters)", BAD_REQUEST),
    REPORT_ALREADY_EXISTS("REPORT_ALREADY_EXISTS", "You have already reported this content.", CONFLICT),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "An internal exception occurred, please try again or contact the admin", INTERNAL_SERVER_ERROR),
    ERROR("ERROR", "An unexpected error occurred", CONFLICT);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    ErrorCode(final String code, final String defaultMessage, final HttpStatus status) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }
}