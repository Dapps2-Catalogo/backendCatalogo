package com.example.demo.auxiliar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightCreatedEvent {
    private String flightId;        // mapeamos desde Vuelo.idVuelo
    private String flightNumber;    // mapeamos desde Vuelo.idVuelo (o tu código público)
    private String origin;          // IATA de 3 letras
    private String destination;     // IATA de 3 letras
    private String aircraftModel;   // Vuelo.tipoAvion
    private String departureAt;     // ISO 8601 (OffsetDateTime.toString())
    private String arrivalAt;       // ISO 8601
    private String status;          // Vuelo.estadoVuelo.name()
    private double price;           // Vuelo.precio.doubleValue()
    private String currency;        // "ARS", "USD", etc.
}
