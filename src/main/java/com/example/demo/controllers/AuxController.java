package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.demo.repositories.AirlineSequenceRepository;
import com.example.demo.repositories.VueloRepository;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/aux")
@RequiredArgsConstructor
public class AuxController {

    
    @Autowired
    VueloRepository vueloRepository;

    @Autowired
    AirlineSequenceRepository airlineSequenceRepository;

    @DeleteMapping("/vuelos")
    public void auxDeleteAll() {
        // This is an auxiliary endpoint for testing purposes.
        // Implement any necessary logic here.
        vueloRepository.deleteAll();
    }
    @DeleteMapping("/airlines")
    public void auxDeleteAllAirlines() {
        // This is an auxiliary endpoint for testing purposes.
        // Implement any necessary logic here.
        airlineSequenceRepository.deleteAll();
    }
}
