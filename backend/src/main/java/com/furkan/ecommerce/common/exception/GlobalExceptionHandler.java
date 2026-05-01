package com.furkan.ecommerce.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, ex.code(), ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    ProblemDetail handleBusiness(BusinessException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.code(), ex.getMessage(), req);
    }

    @ExceptionHandler(UnauthorizedException.class)
    ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNAUTHORIZED, ex.code(), ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleForbidden(AccessDeniedException ex, HttpServletRequest req) {
        return problem(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var detail = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", detail, req);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleException(Exception ex, HttpServletRequest req) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", req);
    }

    private ProblemDetail problem(HttpStatus status, String code, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create("https://api.ecommerce.local/errors/" + code.toLowerCase()));
        pd.setTitle(code);
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }
}
