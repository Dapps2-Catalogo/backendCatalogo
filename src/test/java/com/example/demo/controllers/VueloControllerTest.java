package com.example.demo.controllers;

import com.example.demo.auxiliar.EstadoVuelo;
import com.example.demo.models.Vuelo;
import com.example.demo.service.VueloService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VueloController.class)
@AutoConfigureMockMvc(addFilters = false)
class VueloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @SuppressWarnings("removal")
        @MockBean
    private VueloService vueloService;

    private Vuelo vuelo;
    private OffsetDateTime despegue;

    @BeforeEach
    void setUp() {
        despegue = OffsetDateTime.of(2025, 9, 27, 10, 0, 0, 0, ZoneOffset.UTC);
        vuelo = new Vuelo();
        vuelo.setId(1);
        vuelo.setIdVuelo("IB123");
        vuelo.setAerolinea("Iberia");
        vuelo.setOrigen("MAD");
        vuelo.setDestino("EZE");
        vuelo.setPrecio(new BigDecimal("1200.00"));
        vuelo.setMoneda("USD");
        vuelo.setDespegue(despegue);
        vuelo.setAterrizajeLocal(despegue.plusHours(12));
        vuelo.setEstadoVuelo(EstadoVuelo.EN_HORA);
        vuelo.setCapacidadAvion(300);
        vuelo.setTipoAvion("A350");
    }

    @Test
    void buscarVuelos_devuelvePagina() throws Exception {
        Page<Vuelo> page = new PageImpl<>(Collections.singletonList(vuelo));
        when(vueloService.findByOrigenAndDestinoAndFecha(
                eq("MAD"),
                eq("EZE"),
                eq(despegue.toLocalDate()),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/vuelos")
                        .param("origen", "MAD")
                        .param("destino", "EZE")
                        .param("fecha", despegue.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idVuelo").value("IB123"));

        verify(vueloService).findByOrigenAndDestinoAndFecha(
                eq("MAD"),
                eq("EZE"),
                eq(despegue.toLocalDate()),
                any(Pageable.class)
        );
    }

    @Test
    void buscarVuelosPorRango_devuelvePagina() throws Exception {
        LocalDate desde = LocalDate.of(2025, 9, 27);
        LocalDate hasta = desde.plusDays(2);
        Page<Vuelo> page = new PageImpl<>(Collections.singletonList(vuelo));
        when(vueloService.buscarPorRango(
                eq("MAD"),
                eq("EZE"),
                eq(desde),
                eq(hasta),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/vuelos/rango")
                        .param("origen", "MAD")
                        .param("destino", "EZE")
                        .param("fechaDesde", desde.toString())
                        .param("fechaHasta", hasta.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].aerolinea").value("Iberia"));

        verify(vueloService).buscarPorRango(
                eq("MAD"),
                eq("EZE"),
                eq(desde),
                eq(hasta),
                any(Pageable.class)
        );
    }

    @Test
    void search_devuelvePaginaFiltrada() throws Exception {
        LocalDate desde = despegue.toLocalDate();
        Page<Vuelo> page = new PageImpl<>(Collections.singletonList(vuelo));
        when(vueloService.buscarVuelosPaginado(
                eq(desde),
                eq(desde.plusDays(1)),
                eq("Iberia"),
                eq("MAD"),
                eq("EZE"),
                eq(new BigDecimal("500")),
                eq(new BigDecimal("1500")),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/vuelos/search")
                        .param("fechaDesde", desde.toString())
                        .param("fechaHasta", desde.plusDays(1).toString())
                        .param("aerolinea", "Iberia")
                        .param("origen", "MAD")
                        .param("destino", "EZE")
                        .param("precioMin", "500")
                        .param("precioMax", "1500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].destino").value("EZE"));

        verify(vueloService).buscarVuelosPaginado(
                eq(desde),
                eq(desde.plusDays(1)),
                eq("Iberia"),
                eq("MAD"),
                eq("EZE"),
                eq(new BigDecimal("500")),
                eq(new BigDecimal("1500")),
                any(Pageable.class)
        );
    }

    @Test
    void crearVuelo_devuelveCreado() throws Exception {
        when(vueloService.createVuelo(any(Vuelo.class))).thenReturn(vuelo);

        mockMvc.perform(post("/vuelos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vuelo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idVuelo").value("IB123"));

        verify(vueloService).createVuelo(any(Vuelo.class));
    }

    @Test
    void actualizarVuelo_devuelveActualizado() throws Exception {
        when(vueloService.updateVuelo(eq(1), any(Vuelo.class))).thenReturn(vuelo);

        mockMvc.perform(put("/vuelos/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vuelo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aerolinea").value("Iberia"));

        verify(vueloService).updateVuelo(eq(1), any(Vuelo.class));
    }

    @Test
    void eliminarVuelo_respondeNoContent() throws Exception {
        mockMvc.perform(delete("/vuelos/{id}", 1))
                .andExpect(status().isNoContent());

        verify(vueloService).deleteVuelo(1);
    }
}
