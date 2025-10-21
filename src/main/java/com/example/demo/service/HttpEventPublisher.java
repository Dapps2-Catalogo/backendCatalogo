package com.example.demo.service;

import com.example.demo.models.Vuelo;
import com.example.demo.auxiliar.EstadoVuelo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HttpEventPublisher {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Clock clock = Clock.systemUTC();

    @Value("${events.base-url:http://34.172.179.60}")
    private String baseUrl;

    @Value("${events.path:/events}")
    private String eventsPath;

    @Value("${events.api-key:microservices-api-key-2024-secure}")
    private String apiKey;

    private static final DateTimeFormatter ISO_INSTANT =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    /**
     * Publica el evento de creación de vuelo con:
     * - payload STRING (no objeto)
     * - messageId = UUID v4 por intento
     * - occurredAt = now(UTC)
     * - correlationId = propagado o generado
     * - idempotencyKey = determinística por (idVuelo + despegue)
     *
     * @param vuelo           entidad recién creada/guardada
     * @param correlationIdIn opcional, propagado desde el request; si null/vacío se genera uno
     * @return respuesta del gateway de eventos como String
     */
    public String publishFlightCreated(Vuelo vuelo, String correlationIdIn) throws JsonProcessingException {
        // 1) Payload interno (solo claves del contrato)
        ObjectNode inner = mapper.createObjectNode();
        inner.put("flightId", String.valueOf(vuelo.getId()));                    // ej: FL-YYYYMMDD-<idVuelo>
        inner.put("flightNumber", safe(vuelo.getIdVuelo()));
        inner.put("origin", upper(vuelo.getOrigen()));
        inner.put("destination", upper(vuelo.getDestino()));
        inner.put("aircraftModel", safe(vuelo.getTipoAvion()));
        inner.put("departureAt", toIsoInstant(vuelo.getDespegue()));
        inner.put("arrivalAt", toIsoInstant(vuelo.getAterrizajeLocal()));
        inner.put("status", mapStatus(vuelo.getEstadoVuelo()));
        inner.put("price", numberOrZero(vuelo.getPrecio()));
        inner.put("currency", safeOrDefault(vuelo.getMoneda(), "ARS"));

        // 2) Serializar payload a STRING (queda escapado dentro de "payload")
        String payloadAsString = mapper.writeValueAsString(inner);

        // 3) Envelope del evento
        String messageId = "msg-" + UUID.randomUUID().toString(); // único por intento
        String occurredAt = ISO_INSTANT.format(Instant.now(clock));
        String correlationId = StringUtils.hasText(correlationIdIn) ? correlationIdIn : "corr-" + UUID.randomUUID();
        String idempotencyKey = buildIdempotencyKeyForCreate(vuelo);

        ObjectNode envelope = mapper.createObjectNode();
        envelope.put("messageId", messageId);
        envelope.put("eventType", "flights.flight.created");
        envelope.put("schemaVersion", "1.0");
        envelope.put("occurredAt", occurredAt);
        envelope.put("producer", "flights-service");
        envelope.put("correlationId", correlationId);
        envelope.put("idempotencyKey", idempotencyKey);
        envelope.put("payload", payloadAsString); // STRING, no objeto

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        return client.post()
                .uri(eventsPath)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-Key", apiKey)
                .bodyValue(envelope)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    //EDITADO: nuevo método para UPDATE de vuelo

    public String publishFlightUpdated(Vuelo vuelo, String correlationIdIn) throws JsonProcessingException {
        // 1) Payload exacto al schema
        ObjectNode inner = mapper.createObjectNode();
        inner.put("flightId", String.valueOf(vuelo.getId())); // Ej: FL-20251115-AA991
        inner.put("newStatus", mapStatus(vuelo.getEstadoVuelo()));

        if (vuelo.getDespegue() != null) {
            inner.put("newDepartureAt", toIsoInstant(vuelo.getDespegue()));
        }
        if (vuelo.getAterrizajeLocal() != null) {
            inner.put("newArrivalAt", toIsoInstant(vuelo.getAterrizajeLocal()));
        }

        // 2) Serializar payload a STRING (queda escapado dentro del envelope)
        String payloadAsString = mapper.writeValueAsString(inner);

        // 3) Envelope del evento
        String messageId = "msg-" + UUID.randomUUID().toString();
        String occurredAt = ISO_INSTANT.format(Instant.now(clock));
        String correlationId = StringUtils.hasText(correlationIdIn)
                ? correlationIdIn
                : "corr-" + UUID.randomUUID();

        // Idempotencia para UPDATE (determinística por idVuelo + despegue + hash del payload)
        String idemKey = "flight:update:" 
                + safe(vuelo.getIdVuelo()) + ":" 
                + (vuelo.getDespegue() != null ? vuelo.getDespegue().toInstant().getEpochSecond() : -1)
                + ":" + Integer.toHexString(payloadAsString.hashCode());

        ObjectNode envelope = mapper.createObjectNode();
        envelope.put("messageId", messageId);
        envelope.put("eventType", "flights.flight.updated");
        envelope.put("schemaVersion", "1.0");
        envelope.put("occurredAt", occurredAt);
        envelope.put("producer", "flights-service");
        envelope.put("correlationId", correlationId);
        envelope.put("idempotencyKey", idemKey);
        envelope.put("payload", payloadAsString); // STRING

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        return client.post()
                .uri(eventsPath)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-Key", apiKey)
                .bodyValue(envelope)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }


    // ====================== Helpers ======================

    
    private String buildFlightId(Vuelo v) {
        // FL-YYYYMMDD-<idVuelo>
        String yyyyMMdd = (v.getDespegue() != null)
                ? v.getDespegue().toLocalDate().toString().replace("-", "")
                : "NA";
        return "FL-" + yyyyMMdd + "-" + safe(v.getIdVuelo());
    }

    private String buildIdempotencyKeyForCreate(Vuelo v) {
        // Determinística para "create" en base a tu unique (id_vuelo + despegue)
        long epochSec = (v.getDespegue() != null)
                ? v.getDespegue().toInstant().getEpochSecond()
                : -1L;
        return "flight:create:" + safe(v.getIdVuelo()) + ":" + epochSec;
    }

    private String toIsoInstant(OffsetDateTime odt) {
        if (odt == null) return null;
        return ISO_INSTANT.format(odt.toInstant());
    }

    private String mapStatus(EstadoVuelo estado) {
        if (estado == null) return "EN_HORA";
        // Ajustá si tus consumidores esperan otros literales
        return switch (estado) {
            case EN_HORA -> "EN_HORA";
            case DEMORADO -> "DEMORADO";
            case CANCELADO -> "CANCELADO";
        };
    }

    private String upper(String s) {
        return s == null ? null : s.toUpperCase(Locale.ROOT);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeOrDefault(String s, String def) {
        return StringUtils.hasText(s) ? s : def;
    }

    private double numberOrZero(BigDecimal n) {
        return n == null ? 0.0 : n.doubleValue();
    }
}
