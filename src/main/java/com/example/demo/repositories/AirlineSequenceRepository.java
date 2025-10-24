package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.models.AirlineSequence;

import jakarta.transaction.Transactional;

public interface AirlineSequenceRepository extends JpaRepository<AirlineSequence, String> {

    @Transactional // opcional si ya estás dentro de un @Transactional del service
    @Query(value = """
    INSERT INTO airline_sequence (airline_prefix, last_number)
    VALUES (:prefix, 1)
    ON CONFLICT (airline_prefix)
    DO UPDATE SET last_number = airline_sequence.last_number + 1
    RETURNING last_number
    """, nativeQuery = true)
    Integer nextNumberForPrefix(@Param("prefix") String prefix);

}