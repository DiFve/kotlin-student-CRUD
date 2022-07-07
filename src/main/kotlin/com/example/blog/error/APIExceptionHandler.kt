package com.example.blog.error

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.validation.ConstraintViolationException

@ControllerAdvice
class APIExceptionHandler {
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleMethodArgumentNotValidException(exception: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = exception.constraintViolations.map {
            ErrorResponse.ApiError(
                "VALIDATION_ERROR",
                it.message ?: "Validation error."
            )
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.name,
                    errors
                )
            )
    }
}