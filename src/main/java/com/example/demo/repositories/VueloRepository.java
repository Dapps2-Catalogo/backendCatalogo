package com.example.demo.repositories;

import com.example.demo.models.Vuelo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface VueloRepository extends JpaRepository<Vuelo, Integer>, JpaSpecificationExecutor<Vuelo> {

    boolean existsByIdVuelo(String idVuelo);

    // Unicidad compuesta (id_vuelo, despegue)
    boolean existsByIdVueloAndDespegue(String idVuelo, OffsetDateTime despegue);
    Optional<Vuelo> findByIdVueloAndDespegue(String idVuelo, OffsetDateTime despegue);

    

    // Búsqueda por rango de despegue (usar para “por fecha” convirtiendo a [00:00, 24:00))
    Page<Vuelo> findByOrigenAndDestinoAndDespegueBetween(
            String origen,
            String destino,
            OffsetDateTime desdeInclusive,
            OffsetDateTime hastaExclusive,
            Pageable pageable
    );
}

