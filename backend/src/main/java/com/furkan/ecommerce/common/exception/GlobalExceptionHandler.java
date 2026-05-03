package com.furkan.ecommerce.common.exception;

import com.furkan.ecommerce.auth.internal.exception.EmailAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.context.i18n.LocaleContextHolder;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, ex.code(), req);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ProblemDetail handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, ex.code(), req);
    }

    @ExceptionHandler(BusinessException.class)
    ProblemDetail handleBusiness(BusinessException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.code(), req);
    }

    @ExceptionHandler(UnauthorizedException.class)
    ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNAUTHORIZED, ex.code(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN, "FORBIDDEN", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", req);
        pd.setProperty("fieldErrors", fieldErrors(ex.getBindingResult()));
        return pd;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleUnreadableMessage(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, "REQUEST_NOT_READABLE", req);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleException(Exception ex, HttpServletRequest req) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", req);
    }

    private ProblemDetail problem(HttpStatus status, String code, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message("error." + code, message("error.default")));
        pd.setTitle(message("error.title"));
        pd.setProperty("code", code);
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    private List<FieldValidationError> fieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(error -> new FieldValidationError(error.getField(), validationMessage(error)))
                .toList();
    }

    private String validationMessage(FieldError error) {
        String defaultMessage = error.getDefaultMessage();
        if (defaultMessage == null || defaultMessage.isBlank()) {
            return message("validation.invalidField");
        }

        if (defaultMessage.startsWith("{") && defaultMessage.endsWith("}")) {
            return message(defaultMessage.substring(1, defaultMessage.length() - 1), message("validation.invalidField"));
        }

        return defaultMessage;
    }

    private String message(String key) {
        return message(key, key);
    }

    private String message(String key, String fallback) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, fallback, locale);
    }

    private record FieldValidationError(String field, String message) {
    }
}
