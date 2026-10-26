package com.radom07.medicalclinic.exception;

import com.radom07.medicalclinic.model.dto.ErrorMessageDto;
import com.radom07.medicalclinic.model.dto.ValidationErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // request.getRequestURI -> "path": "/patients/mail%40mai.pl"
    @ExceptionHandler(MedicalClinicException.class)
    public ResponseEntity<ErrorMessageDto> handleMedicalClinicException(MedicalClinicException e, HttpServletRequest request) {
        ErrorMessageDto errorMessage = new ErrorMessageDto(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                e.getHttpStatus().value(),
                e.getHttpStatus().getReasonPhrase(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(e.getHttpStatus()).body(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageDto> handleUnknownError(Exception e, HttpServletRequest request) {
        ErrorMessageDto errorMessage = new ErrorMessageDto(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unknown error",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    // Walidacja z RequestBody
    // z artykułu https://medium.com/@kalanamalshan98/exception-handling-and-validation-in-spring-boot-explained-simply-with-real-world-api-examples-ed7aaac26f0b
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDto> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        });
        ValidationErrorDto validationError = new ValidationErrorDto(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
    }

    // Walidacja z PathVariable w sieżce
    // Tutaj wspomogłem się chatem aby ConstraintViolation było spójne z implementacją konceptu z MethodArgumentNotValidException z artykułu
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorDto> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = null;
            for (Path.Node node : violation.getPropertyPath()) {
                fieldName = node.getName();
            }
            fieldErrors.put(fieldName, violation.getMessage());
        }

        ValidationErrorDto validationError = new ValidationErrorDto(
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for request parameters",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
    }
}
