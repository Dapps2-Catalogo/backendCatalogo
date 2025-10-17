package com.example.demo.auxiliar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class FlightUpdatedEvent {
    private String flightId;        // requerido
    private String newStatus;       // requerido
    private String newDepartureAt;  // opcional ISO 8601
    private String newArrivalAt;    // opcional ISO 8601
}
