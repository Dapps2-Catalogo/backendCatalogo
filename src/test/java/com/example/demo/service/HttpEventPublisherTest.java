package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.auxiliar.EstadoVuelo;
import com.example.demo.models.Vuelo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class HttpEventPublisherTest {

    @Mock
    private WebClient.Builder builder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private HttpEventPublisher publisher;

    private Object capturedBody;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any(MediaType.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), anyString())).thenReturn(requestBodyUriSpec);

        when(requestBodyUriSpec.bodyValue(any())).thenAnswer(invocation -> {
            capturedBody = invocation.getArgument(0);
            return requestHeadersSpec;
        });

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(eq(String.class))).thenReturn(Mono.just("ack"));

        publisher = new HttpEventPublisher(builder);
        ReflectionTestUtils.setField(publisher, "baseUrl", "http://example.com");
        ReflectionTestUtils.setField(publisher, "eventsPath", "/events");
        ReflectionTestUtils.setField(publisher, "apiKey", "test-key");
    }

    @Test
    void publishFlightCreated_generatesNormalizedEnvelope() throws Exception {
        Vuelo vuelo = buildVuelo();
        vuelo.setEstadoVuelo(EstadoVuelo.DEMORADO);
        vuelo.setTipoAvion("A350");
        vuelo.setMoneda("USD");

        String response = publisher.publishFlightCreated(vuelo, null);

        assertEquals("ack", response);
        verify(builder).baseUrl("http://example.com");
        verify(requestBodyUriSpec).header("X-API-Key", "test-key");

        ObjectNode envelope = extractEnvelope();
        assertEquals("flights.flight.created", envelope.get("eventType").asText());
        assertTrue(envelope.get("correlationId").asText().startsWith("corr-"));

        String payloadRaw = envelope.get("payload").asText();
        JsonNode payload = objectMapper.readTree(payloadRaw);
        assertEquals("1", payload.get("flightId").asText());
        assertEquals("IB123", payload.get("flightNumber").asText());
        assertEquals("MAD", payload.get("origin").asText());
        assertEquals("EZE", payload.get("destination").asText());
        assertEquals("A350", payload.get("aircraftModel").asText());
        assertEquals("DEMORADO", payload.get("status").asText());
        assertEquals("USD", payload.get("currency").asText());
        assertEquals(850.5, payload.get("price").asDouble(), 0.0001);
        assertEquals("flight:create:IB123:" + vuelo.getDespegue().toInstant().getEpochSecond(),
                envelope.get("idempotencyKey").asText());
    }

    @Test
    void publishFlightCreated_respectsProvidedCorrelationAndDefaults() throws Exception {
        Vuelo vuelo = buildVuelo();
        vuelo.setEstadoVuelo(null); // fuerza valor por defecto
        vuelo.setMoneda(null);      // usa default "ARS"
        vuelo.setPrecio(null);      // se convierte en 0
        vuelo.setTipoAvion(null);   // safe -> ""

        String response = publisher.publishFlightCreated(vuelo, "corr-manual");

        assertEquals("ack", response);

        ObjectNode envelope = extractEnvelope();
        assertEquals("corr-manual", envelope.get("correlationId").asText());

        JsonNode payload = objectMapper.readTree(envelope.get("payload").asText());
        assertEquals("", payload.get("aircraftModel").asText());
        assertEquals("EN_HORA", payload.get("status").asText());
        assertEquals(0.0, payload.get("price").asDouble(), 0.0001);
        assertEquals("ARS", payload.get("currency").asText());
    }

    @Test
    void publishFlightUpdated_generatesDeterministicIdempotencyKey() throws Exception {
        Vuelo vuelo = buildVuelo();
        vuelo.setEstadoVuelo(EstadoVuelo.CANCELADO);

        String response = publisher.publishFlightUpdated(vuelo, null);

        assertEquals("ack", response);

        ObjectNode envelope = extractEnvelope();
        assertEquals("flights.flight.updated", envelope.get("eventType").asText());
        assertTrue(envelope.get("correlationId").asText().startsWith("corr-"));

        String payloadRaw = envelope.get("payload").asText();
        JsonNode payload = objectMapper.readTree(payloadRaw);
        assertEquals("CANCELADO", payload.get("newStatus").asText());
        assertTrue(payload.has("newDepartureAt"));
        assertTrue(payload.has("newArrivalAt"));

        String expectedKey = "flight:update:" + vuelo.getIdVuelo() + ":"
                + vuelo.getDespegue().toInstant().getEpochSecond() + ":"
                + Integer.toHexString(payloadRaw.hashCode());
        assertEquals(expectedKey, envelope.get("idempotencyKey").asText());
    }

    @Test
    void publishFlightUpdated_omitsOptionalFieldsWhenNull() throws Exception {
        Vuelo vuelo = buildVuelo();
        vuelo.setEstadoVuelo(EstadoVuelo.EN_HORA);
        vuelo.setDespegue(null);
        vuelo.setAterrizajeLocal(null);

        String response = publisher.publishFlightUpdated(vuelo, "corr-existing");

        assertEquals("ack", response);

        ObjectNode envelope = extractEnvelope();
        assertEquals("corr-existing", envelope.get("correlationId").asText());

        JsonNode payload = objectMapper.readTree(envelope.get("payload").asText());
        assertFalse(payload.has("newDepartureAt"));
        assertFalse(payload.has("newArrivalAt"));
        assertEquals("EN_HORA", payload.get("newStatus").asText());
        assertEquals("flight:update:" + vuelo.getIdVuelo() + ":-1:" +
                Integer.toHexString(envelope.get("payload").asText().hashCode()),
                envelope.get("idempotencyKey").asText());
    }

    private Vuelo buildVuelo() {
        Vuelo vuelo = new Vuelo();
        vuelo.setId(1);
        vuelo.setIdVuelo("IB123");
        vuelo.setAerolinea("Iberia");
        vuelo.setOrigen("mad");
        vuelo.setDestino("eze");
        vuelo.setPrecio(new BigDecimal("850.50"));
        vuelo.setMoneda("USD");
        OffsetDateTime departure = OffsetDateTime.now(ZoneOffset.UTC).withNano(0).plusDays(2);
        vuelo.setDespegue(departure);
        vuelo.setAterrizajeLocal(departure.plusHours(14));
        vuelo.setEstadoVuelo(EstadoVuelo.EN_HORA);
        vuelo.setCapacidadAvion(300);
        vuelo.setTipoAvion("A350");
        return vuelo;
    }

    private ObjectNode extractEnvelope() {
        assertNotNull(capturedBody, "No se capturó ninguna solicitud");
        assertTrue(capturedBody instanceof ObjectNode, "El cuerpo enviado debe ser ObjectNode");
        return (ObjectNode) capturedBody;
    }
}
