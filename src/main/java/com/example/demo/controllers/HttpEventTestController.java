package com.example.demo.controllers;

import com.example.demo.repositories.VueloRepository;
import com.example.demo.service.HttpEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/v1/test-events")
@RequiredArgsConstructor
public class HttpEventTestController {

    private final HttpEventPublisher publisher;
    @Autowired 
    private VueloRepository vueloRepository;

    /**
     * POST /v1/test-events/eze-mia
     * Dispara el POST al endpoint externo con el evento armado (payload como STRING).
     */
    @PostMapping("/eze-mia")
    public ResponseEntity<String> sendEzeMia() throws Exception {
        String resp = publisher.publishFlightCreated(vueloRepository.findById(5).orElseThrow(), null);
        return ResponseEntity.ok(resp);
    }
    @PutMapping("path/{id}")
    public ResponseEntity<String> putMethodName() throws JsonProcessingException {
        String resp = publisher.publishFlightUpdated(vueloRepository.findById(5).orElseThrow(), null);
        return ResponseEntity.ok(resp);
    }
}

