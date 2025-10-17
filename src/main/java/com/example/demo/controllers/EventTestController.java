package com.example.demo.controllers;


import com.example.demo.models.Vuelo;
import com.example.demo.repositories.VueloRepository;
import com.example.demo.service.FlightEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vuelos/test-event")
@RequiredArgsConstructor
public class EventTestController {

    private final VueloRepository vueloRepository;
    private final FlightEventProducer producer;

    @PostMapping("/{id}/created")
    public String emitCreated(@PathVariable Integer id) {
        Vuelo v = vueloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vuelo no encontrado"));
        producer.sendFlightCreated(v);
        return "✅ Evento flights.flight.created emitido para vuelo " + id;
    }

    @PostMapping("/{id}/updated")
    public String emitUpdated(@PathVariable Integer id) {
        Vuelo v = vueloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vuelo no encontrado"));
        producer.sendFlightUpdated(v, v.getEstadoVuelo().name());
        return "✅ Evento flights.flight.updated emitido para vuelo " + id;
    }
}
