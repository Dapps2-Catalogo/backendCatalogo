package com.example.demo.system;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.demo.service.FlightCodeService;
import com.example.demo.service.HttpEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(VueloSystemTest.TestOverrides.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class VueloSystemTest {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY_VALUE = "test-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpEventPublisher eventPublisher;

    @Autowired
    private FlightCodeService flightCodeService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(eventPublisher, flightCodeService);
    }

    @Test
    void createUpdateAndQueryFlightFlow() throws Exception {
        OffsetDateTime departure = OffsetDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime arrival = departure.plusHours(10);

        when(flightCodeService.nextFlightCode("IB")).thenReturn("IB0001");
        when(eventPublisher.publishFlightCreated(any(), isNull())).thenReturn("ok");
        Map<String, Object> payload = new HashMap<>();
        payload.put("idVuelo", "IB");
        payload.put("aerolinea", "Iberia");
        payload.put("origen", "MAD");
        payload.put("destino", "EZE");
        payload.put("precio", new BigDecimal("850.50"));
        payload.put("moneda", "USD");
        payload.put("despegue", departure);
        payload.put("aterrizajeLocal", arrival);
        payload.put("estadoVuelo", "EN_HORA");
        payload.put("capacidadAvion", 250);
        payload.put("tipoAvion", "A350");

        String requestJson = objectMapper.writeValueAsString(payload);

        MvcResult creation = mockMvc.perform(post("/vuelos")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idVuelo").value("IB0001"))
            .andExpect(jsonPath("$.origen").value("MAD"))
            .andReturn();

        int vueloId = objectMapper.readTree(creation.getResponse().getContentAsString()).get("id").asInt();

        verify(eventPublisher).publishFlightCreated(any(), isNull());

        mockMvc.perform(get("/vuelos")
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .param("origen", "MAD")
                .param("destino", "EZE")
        .param("fecha", departure.toLocalDate().toString())
        .param("sort", "despegue,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(vueloId))
            .andExpect(jsonPath("$.content[0].idVuelo").value("IB0001"));

        Map<String, Object> updatePayload = Map.of("estadoVuelo", "CANCELADO");

        mockMvc.perform(put("/vuelos/{id}", vueloId)
                .header(API_KEY_HEADER, API_KEY_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePayload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estadoVuelo").value("CANCELADO"));

        verify(eventPublisher).publishFlightUpdated(any(), isNull());
    }

    @Test
    void rejectRequestsWithoutApiKey() throws Exception {
        mockMvc.perform(get("/ping"))
            .andExpect(status().isUnauthorized());
    }

    @TestConfiguration
    static class TestOverrides {
        @Bean
        @Primary
        HttpEventPublisher eventPublisherMock() {
            return Mockito.mock(HttpEventPublisher.class);
        }

        @Bean
        @Primary
        FlightCodeService flightCodeServiceMock() {
            return Mockito.mock(FlightCodeService.class);
        }
    }
}
