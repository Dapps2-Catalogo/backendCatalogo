package com.example.demo.auxiliar;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload del schema flights.aircraft_or_airline.updated v1.0 */
@Data @NoArgsConstructor @AllArgsConstructor
public class AircraftOrAirlineUpdatedEvent {
    private String airlineBrand;  // Vuelo.aerolinea
    private String aircraftId;    // usamos Vuelo.tipoAvion como identificador del equipo
    private int capacity;         // Vuelo.capacidadAvion
    private String seatMapId;     // opcional (puede ir null)
}
