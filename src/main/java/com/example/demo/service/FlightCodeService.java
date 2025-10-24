package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.repositories.AirlineSequenceRepository;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlightCodeService {
    private final AirlineSequenceRepository seqRepo;

    public String nextFlightCode(String rawPrefix) {
        if (rawPrefix == null || rawPrefix.isBlank())
            throw new IllegalArgumentException("idVuelo (prefijo) requerido");

        // Normalizá prefijo (por ej. "AF")
        String prefix = rawPrefix.trim().toUpperCase();

        // Validaciones mínimas (ajustá a tu negocio)
        if (!prefix.matches("^[A-Z0-9]{2,4}$")) {
            throw new IllegalArgumentException("Prefijo inválido para idVuelo: " + prefix);
        }

        // 🔹 Genera el próximo número usando la secuencia
        int n = Optional.ofNullable(seqRepo.nextNumberForPrefix(prefix))
                        .orElseThrow(() -> new IllegalStateException("No se pudo obtener secuencia para " + prefix));

        // 🔹 Formatea tipo AF0001, AA0002, etc.
        return String.format("%s%04d", prefix, n);
    }
}

