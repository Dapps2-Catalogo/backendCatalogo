package com.example.demo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "airline_sequence")
@Data
public class AirlineSequence {
    @Id
    @Column(name = "airline_prefix", length = 8)
    private String airlinePrefix;

    @Column(name = "last_number", nullable = false)
    private Integer lastNumber;
}
