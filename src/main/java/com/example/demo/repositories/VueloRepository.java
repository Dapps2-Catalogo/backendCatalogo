package com.example.demo.repositories;

import com.example.demo.models.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



public interface VueloRepository extends JpaRepository<Vuelo, Integer> {
    
    List<Vuelo> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate fecha);

    boolean existsByIdVueloAndFecha(String idVuelo, LocalDate fecha);

    Optional<Vuelo> findByIdVueloAndFecha(String idVuelo, LocalDate fecha);

    Page<Vuelo> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate fecha, Pageable pageable);

}
