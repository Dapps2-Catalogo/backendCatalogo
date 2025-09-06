package com.example.demo.repositories;

import com.example.demo.models.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;



public interface VueloRepository extends JpaRepository<Vuelo, Integer> {
    // Buscar vuelos por origen, destino y fecha
    List<Vuelo> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate fecha);
}
