package com.example.demo.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableEntityException extends RuntimeException {
    public UnprocessableEntityException(String message) {
        super(message);
    }
}