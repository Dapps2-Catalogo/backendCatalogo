package com.example.demo.service;

import java.util.List;

import com.example.demo.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.auxiliar.LoginRequest;
import com.example.demo.auxiliar.LoginResponse;
import com.example.demo.auxiliar.RegisterRequest;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!loginRequest.getPassword().equals(user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        LoginResponse resp = new LoginResponse();
        resp.setDisplayed_name(
            user.getDisplayName() != null && !user.getDisplayName().isBlank()
                ? user.getDisplayName()
                : user.getUsername()
        );
        resp.setToken(null); // no generamos token todavia
        return resp;
    }







    public Object register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username ya registrado");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            
            throw new BadRequestException("Email ya registrado");
        }

        // Crear entidad
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(registerRequest.getPassword()); 
        user.setDisplayName(registerRequest.getDisplayed_name()); 

        // Guardar
        User saved = userRepository.save(user);
        return saved.getId();
    }


    public List<User> getUsers (){
        return userRepository.findAll();
    }
    
}
