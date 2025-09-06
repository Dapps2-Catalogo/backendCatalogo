package com.example.demo.auxiliar;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private Integer code;

    public ErrorResponse(String message, Integer code) {
        this.message = message;
        this.code = code;
    }
    
}
