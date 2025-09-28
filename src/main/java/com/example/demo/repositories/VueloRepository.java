package com.example.demo.repositories;

import com.example.demo.models.Vuelo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
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



    /**
     * Normaliza tipo_avion y capacidad_avion:
     *  - si contiene "boeing" -> tipo_avion='B737', capacidad_avion=180
     *  - si contiene "airbus" -> tipo_avion='A320', capacidad_avion=288
     * Devuelve la cantidad de filas afectadas.
     */
    @Modifying
    @Query(value = """
        UPDATE public.vuelo
        SET
            tipo_avion = CASE
            WHEN lower(tipo_avion) LIKE '%boeing%' THEN 'B737'
            WHEN lower(tipo_avion) LIKE '%airbus%' THEN 'A320'
            ELSE tipo_avion
            END,
            capacidad_avion = CASE
            WHEN lower(tipo_avion) LIKE '%boeing%' THEN 180
            WHEN lower(tipo_avion) LIKE '%airbus%' THEN 288
            ELSE capacidad_avion
            END
        WHERE lower(tipo_avion) LIKE '%boeing%'
            OR lower(tipo_avion) LIKE '%airbus%';
        """, nativeQuery = true)
    int normalizeTiposAvionMasivo();
}

