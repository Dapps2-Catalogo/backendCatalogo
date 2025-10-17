package com.example.demo.service;

import com.example.demo.auxiliar.AircraftOrAirlineUpdatedEvent;
import com.example.demo.auxiliar.FlightCreatedEvent;
import com.example.demo.auxiliar.FlightUpdatedEvent;
import com.example.demo.models.Vuelo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Servicio responsable de emitir eventos Kafka del dominio Flights.
 * Envia los tipos:
 *  - flights.flight.created
 *  - flights.flight.updated
 *  - flights.aircraft_or_airline.updated
 */
@Service
@RequiredArgsConstructor
public class FlightEventProducer {

    private static final String TOPIC = "flights.events";
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    // === helpers ===
    private static String iso(Object o) { return o == null ? null : o.toString(); }
    private static String up3(String s) { return s == null ? null : s.trim().toUpperCase(); }

    /**
     * Envía un evento flights.flight.created v1.0
     */
    public void sendFlightCreated(Vuelo v) {
        FlightCreatedEvent payload = new FlightCreatedEvent(
                v.getId().toString(),           // flightId = ID autoincremental (Integer)
                v.getIdVuelo(),                 // flightNumber = código del vuelo
                up3(v.getOrigen()),
                up3(v.getDestino()),
                v.getTipoAvion(),
                iso(v.getDespegue()),
                iso(v.getAterrizajeLocal()),
                v.getEstadoVuelo().name(),
                v.getPrecio().doubleValue(),
                up3(v.getMoneda())
        );

        sendWrapped("flights.flight.created", v.getIdVuelo(), payload);
    }

    /**
     * Envía un evento flights.flight.updated v1.0
     */
    public void sendFlightUpdated(Vuelo v, String nuevoEstado) {
        FlightUpdatedEvent payload = new FlightUpdatedEvent(
                v.getId().toString(),
                nuevoEstado,
                iso(v.getDespegue()),
                iso(v.getAterrizajeLocal())
        );

        sendWrapped("flights.flight.updated", v.getIdVuelo(), payload);
    }

    /**
     * Envía un evento flights.aircraft_or_airline.updated v1.0
     */
    public void sendAircraftOrAirlineUpdated(Vuelo v) {
        AircraftOrAirlineUpdatedEvent payload = new AircraftOrAirlineUpdatedEvent(
                v.getAerolinea(),
                v.getTipoAvion(),        // usamos el tipo de avión como ID
                v.getCapacidadAvion(),
                null                     // seatMapId (no disponible en tu modelo)
        );

        sendWrapped("flights.aircraft_or_airline.updated", v.getTipoAvion(), payload);
    }

    // === método genérico ===
    private void sendWrapped(String eventType, String key, Object payload) {
        try {
            String message = mapper.writeValueAsString(
                    Map.of(
                            "event_type", eventType,
                            "schema_version", "1.0",
                            "payload", payload
                    )
            );

            kafka.send(TOPIC, key, message);
            System.out.printf("✅ Enviado evento Kafka [%s] key=%s%n", eventType, key);

        } catch (Exception e) {
            System.err.printf("❌ Error enviando evento [%s]: %s%n", eventType, e.getMessage());
        }
    }
}