package com.example.demo.controllers;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;





@RestController
@RequestMapping("/ping")
@CrossOrigin(origins = "*")
public class PingController {


    @GetMapping()
    public String ping() {
        return "pong";
    }

}
