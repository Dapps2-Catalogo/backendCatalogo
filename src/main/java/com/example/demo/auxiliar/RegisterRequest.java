package com.example.demo.auxiliar;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String displayed_name;
    private String email;
}
