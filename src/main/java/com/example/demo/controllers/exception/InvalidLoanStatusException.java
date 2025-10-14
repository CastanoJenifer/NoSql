package com.example.demo.controllers.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidLoanStatusException extends RuntimeException {
    public InvalidLoanStatusException(String message) {
        super(message);
    }
}

