package com.radom07.medicalclinic.exception;

import com.radom07.medicalclinic.model.dto.ErrorMessageDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MedicalClinicException.class)
    public ErrorMessageDto handleMedicalClinicException(MedicalClinicException e, HttpServletRequest request, HttpServletResponse response) {
        log.warn("Request {} failed with status {}: {}", request.getRequestURI(), e.getHttpStatus().value(), e.getMessage());
        response.setStatus(e.getHttpStatus().value());
        return new ErrorMessageDto(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                e.getHttpStatus().value(),
                e.getHttpStatus().getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessageDto handleUnknownError(Exception e, HttpServletRequest request) {
        log.error("Unexpected error on {}", request.getRequestURI(), e);
        return new ErrorMessageDto(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unknown error",
                request.getRequestURI()
        );
    }

    // https://medium.com/@kalanamalshan98/exception-handling-and-validation-in-spring-boot-explained-simply-with-real-world-api-examples-ed7aaac26f0b
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessageDto handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });
        log.warn("Validation failed on {} {}: fieldErrors={}", request.getMethod(), request.getRequestURI(), fieldErrors);
        return new ErrorMessageDto(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessageDto handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = null;
            for (Path.Node node : violation.getPropertyPath()) {
                fieldName = node.getName();
            }
            fieldErrors.put(fieldName, violation.getMessage());
        }
        log.warn("Constraint violation on {} {}: fieldErrors={}", request.getMethod(), request.getRequestURI(), fieldErrors);
        return new ErrorMessageDto(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for request parameters",
                request.getRequestURI(),
                fieldErrors
        );
    }
}
