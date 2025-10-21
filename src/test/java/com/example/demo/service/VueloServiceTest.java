package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.demo.auxiliar.EstadoVuelo;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ConflictException;
import com.example.demo.models.Vuelo;
import com.example.demo.repositories.VueloRepository;

@ExtendWith(MockitoExtension.class)
public class VueloServiceTest {

    @Mock
    private VueloRepository vueloRepository;

    @InjectMocks
    private VueloService vueloService;

    private Vuelo vuelo;

    @BeforeEach
    void setUp() {
        vuelo = new Vuelo();
        vuelo.setId(1);
        vuelo.setIdVuelo("IB123");
        vuelo.setAerolinea("Iberia");
        vuelo.setOrigen("MAD");
        vuelo.setDestino("EZE");
    OffsetDateTime despegue = OffsetDateTime.now().plusDays(1);
    vuelo.setDespegue(despegue);
    vuelo.setAterrizajeLocal(despegue.plusHours(12));
        vuelo.setPrecio(new BigDecimal("500.00"));
    vuelo.setMoneda("USD");
        vuelo.setEstadoVuelo(EstadoVuelo.EN_HORA);
        vuelo.setTipoAvion("A350");
        vuelo.setCapacidadAvion(300);
    }

    @Test
    void testCreateVuelo_Success() {
        when(vueloRepository.existsByIdVuelo(anyString())).thenReturn(false);
        when(vueloRepository.existsByIdVueloAndDespegue(anyString(), any(OffsetDateTime.class))).thenReturn(false);
        when(vueloRepository.save(any(Vuelo.class))).thenReturn(vuelo);

        Vuelo createdVuelo = vueloService.createVuelo(vuelo);

        assertNotNull(createdVuelo);
        assertEquals(vuelo.getIdVuelo(), createdVuelo.getIdVuelo());
        verify(vueloRepository, times(1)).save(any(Vuelo.class));
    }

    @Test
    void testCreateVuelo_ConflictById() {
        when(vueloRepository.existsByIdVuelo(anyString())).thenReturn(true);
        
        assertThrows(ConflictException.class, () -> {
            vueloService.createVuelo(vuelo);
        });

        verify(vueloRepository, never()).save(any(Vuelo.class));
    }
    
    @Test
    void testCreateVuelo_ConflictByIdAndDespegue() {
        when(vueloRepository.existsByIdVuelo(anyString())).thenReturn(false);
        when(vueloRepository.existsByIdVueloAndDespegue(anyString(), any(OffsetDateTime.class))).thenReturn(true);

        assertThrows(ConflictException.class, () -> {
            vueloService.createVuelo(vuelo);
        });

        verify(vueloRepository, never()).save(any(Vuelo.class));
    }

    @Test
    void testDeleteVuelo_Success() {
        when(vueloRepository.findById(1)).thenReturn(Optional.of(vuelo));
        doNothing().when(vueloRepository).delete(any(Vuelo.class));

        vueloService.deleteVuelo(1);

        verify(vueloRepository, times(1)).delete(vuelo);
    }

    @Test
    void testDeleteVuelo_NotFound() {
        when(vueloRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> {
            vueloService.deleteVuelo(1);
        });

        verify(vueloRepository, never()).delete(any(Vuelo.class));
    }

    @Test
    void testUpdateVuelo_Success() {
        Vuelo request = new Vuelo();
        request.setAerolinea("Aerolineas Argentinas");

        when(vueloRepository.findById(1)).thenReturn(Optional.of(vuelo));
        when(vueloRepository.save(any(Vuelo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vuelo updatedVuelo = vueloService.updateVuelo(1, request);

        assertNotNull(updatedVuelo);
        assertEquals("Aerolineas Argentinas", updatedVuelo.getAerolinea());
        verify(vueloRepository, times(1)).save(any(Vuelo.class));
    }

    @Test
    void testUpdateVuelo_NotFound() {
        Vuelo request = new Vuelo();
        when(vueloRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> {
            vueloService.updateVuelo(1, request);
        });
        
        verify(vueloRepository, never()).save(any(Vuelo.class));
    }

    @Test
    void testUpdateVuelo_WhenCancelled() {
        vuelo.setEstadoVuelo(EstadoVuelo.CANCELADO);
        Vuelo request = new Vuelo();
        request.setEstadoVuelo(EstadoVuelo.CANCELADO);

        when(vueloRepository.findById(1)).thenReturn(Optional.of(vuelo));

        assertThrows(ConflictException.class, () -> {
            vueloService.updateVuelo(1, request);
        });
    }

    @Test
    void testFindByOrigenAndDestinoAndFecha() {
        String origen = "MAD";
        String destino = "EZE";
        LocalDate fecha = LocalDate.now().plusDays(1);
        Pageable pageable = Pageable.unpaged();
        Page<Vuelo> page = new PageImpl<>(Arrays.asList(vuelo));
        when(vueloRepository.findByOrigenAndDestinoAndDespegueBetween(eq(origen), eq(destino), any(OffsetDateTime.class), any(OffsetDateTime.class), eq(pageable)))
                .thenReturn(page);

        Page<Vuelo> vuelos = vueloService.findByOrigenAndDestinoAndFecha(origen, destino, fecha, pageable);

        assertNotNull(vuelos);
        assertEquals(1, vuelos.getTotalElements());
        verify(vueloRepository, times(1)).findByOrigenAndDestinoAndDespegueBetween(eq(origen), eq(destino), any(OffsetDateTime.class), any(OffsetDateTime.class), eq(pageable));
    }

    @Test
    void testFindByOrigenAndDestinoAndFecha_BadRequest() {
        assertThrows(BadRequestException.class, () -> {
            vueloService.findByOrigenAndDestinoAndFecha(null, "EZE", LocalDate.now(), Pageable.unpaged());
        });
    }

    @Test
    void testFindByOrigenAndDestinoAndFecha_CodigoInvalido() {
        assertThrows(BadRequestException.class, () ->
                vueloService.findByOrigenAndDestinoAndFecha("MA", "EZE", LocalDate.now().plusDays(1), Pageable.unpaged()));
    }

    @Test
    void testFindByOrigenAndDestinoAndFecha_FechaPasada() {
        assertThrows(BadRequestException.class, () ->
                vueloService.findByOrigenAndDestinoAndFecha("MAD", "EZE", LocalDate.now().minusDays(1), Pageable.unpaged()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBuscarVuelosPaginado() {
        Pageable pageable = Pageable.unpaged();
        Page<Vuelo> page = new PageImpl<>(Arrays.asList(vuelo));
        when(vueloRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Vuelo> result = vueloService.buscarVuelosPaginado(LocalDate.now(), LocalDate.now().plusDays(2), "Iberia", "MAD", "EZE", null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(vueloRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testBuscarPorRango_Success() {
        Pageable pageable = Pageable.unpaged();
        Page<Vuelo> page = new PageImpl<>(Arrays.asList(vuelo));
        LocalDate desde = LocalDate.now().plusDays(1);
        LocalDate hasta = desde.plusDays(3);

        when(vueloRepository.findByOrigenAndDestinoAndDespegueBetween(anyString(), anyString(), any(OffsetDateTime.class), any(OffsetDateTime.class), eq(pageable)))
                .thenReturn(page);

        Page<Vuelo> resultado = vueloService.buscarPorRango("mad", "eze", desde, hasta, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());

        ArgumentCaptor<OffsetDateTime> desdeCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> hastaCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(vueloRepository).findByOrigenAndDestinoAndDespegueBetween(eq("MAD"), eq("EZE"), desdeCaptor.capture(), hastaCaptor.capture(), eq(pageable));
        assertEquals(desde.atStartOfDay().atOffset(ZoneOffset.UTC), desdeCaptor.getValue());
        assertEquals(hasta.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC), hastaCaptor.getValue());
    }

    @Test
    void testCreateVuelo_NullVuelo() {
        assertThrows(BadRequestException.class, () -> vueloService.createVuelo(null));
    }

    @Test
    void testCreateVuelo_EmptyIdVuelo() {
        Vuelo invalidVuelo = new Vuelo();
        assertThrows(BadRequestException.class, () -> vueloService.createVuelo(invalidVuelo));
    }

    @Test
    void testCreateVuelo_EmptyAerolinea() {
        Vuelo invalidVuelo = new Vuelo();
        invalidVuelo.setIdVuelo("OK");
        assertThrows(BadRequestException.class, () -> vueloService.createVuelo(invalidVuelo));
    }

    @Test
    void testCreateVuelo_InvalidOrigen() {
        Vuelo invalidVuelo = new Vuelo();
        invalidVuelo.setIdVuelo("OK");
        invalidVuelo.setAerolinea("OK");
        invalidVuelo.setOrigen("INVALID"); // más de 3 caracteres
        assertThrows(BadRequestException.class, () -> vueloService.createVuelo(invalidVuelo));
    }

    @Test
    void testCreateVuelo_InvalidDestino() {
        Vuelo invalidVuelo = buildVueloValido();
        invalidVuelo.setDestino("BA" );

        assertThrows(BadRequestException.class, () -> vueloService.createVuelo(invalidVuelo));
    }

    @Test
    void testCreateVuelo_FechaPasada() {
        Vuelo invalidVuelo = buildVueloValido();
        OffsetDateTime pasado = OffsetDateTime.now().minusDays(2);
        invalidVuelo.setDespegue(pasado);
        invalidVuelo.setAterrizajeLocal(pasado.plusHours(2));

        assertThrows(BadRequestException.class, () -> vueloService.createVuelo(invalidVuelo));
    }

    @Test
    void testCreateVuelo_PrecioNegativo() {
        Vuelo invalidVuelo = buildVueloValido();
        invalidVuelo.setPrecio(new BigDecimal("-10"));

        assertThrows(BadRequestException.class, () -> vueloService.createVuelo(invalidVuelo));
    }

    @Test
    void testUpdateVuelo_KeyConflict() {
        Vuelo otroVuelo = new Vuelo();
        otroVuelo.setId(2);
        otroVuelo.setIdVuelo("IB456");
    otroVuelo.setDespegue(vuelo.getDespegue());

        Vuelo request = new Vuelo();
        request.setIdVuelo("IB456"); // Intentamos cambiar a un ID que ya existe en esa fecha

        when(vueloRepository.findById(1)).thenReturn(Optional.of(vuelo));
        when(vueloRepository.findByIdVueloAndDespegue("IB456", vuelo.getDespegue())).thenReturn(Optional.of(otroVuelo));

        assertThrows(ConflictException.class, () -> {
            vueloService.updateVuelo(1, request);
        });
    }

    @Test
    void testUpdateVuelo_Validations() {
        when(vueloRepository.findById(1)).thenReturn(Optional.of(vuelo));
        
        // Test origen inválido (no tiene 3 caracteres)
        Vuelo requestOrigenInvalido = new Vuelo();
        requestOrigenInvalido.setOrigen("INVALID"); // más de 3 caracteres
        assertThrows(BadRequestException.class, () -> vueloService.updateVuelo(1, requestOrigenInvalido));

        // Test destino inválido (no tiene 3 caracteres)
        Vuelo requestDestinoInvalido = new Vuelo();
        requestDestinoInvalido.setDestino("INVALID"); // más de 3 caracteres
        assertThrows(BadRequestException.class, () -> vueloService.updateVuelo(1, requestDestinoInvalido));

        // Test fecha inválida (en el pasado)
        Vuelo requestFechaInvalida = new Vuelo();
    requestFechaInvalida.setDespegue(OffsetDateTime.now().minusDays(1));
        assertThrows(BadRequestException.class, () -> vueloService.updateVuelo(1, requestFechaInvalida));

        // Test precio inválido (negativo)
        Vuelo requestPrecioInvalido = new Vuelo();
        requestPrecioInvalido.setPrecio(new BigDecimal("-1"));
        assertThrows(BadRequestException.class, () -> vueloService.updateVuelo(1, requestPrecioInvalido));
    }

    @Test
    void testBuscarPorRango_BadRequest() {
        assertThrows(BadRequestException.class, () -> {
            vueloService.buscarPorRango("MAD", "EZE", LocalDate.now().plusDays(2), LocalDate.now().plusDays(1), Pageable.unpaged());
        });
    }

    @Test
    void testBuscarVuelosPaginado_BadRequest() {
         assertThrows(BadRequestException.class, () -> {
            vueloService.buscarVuelosPaginado(LocalDate.now().plusDays(2), LocalDate.now().plusDays(1), null, null, null, null, null, Pageable.unpaged());
        });
    }

    private Vuelo buildVueloValido() {
        Vuelo nuevo = new Vuelo();
        nuevo.setIdVuelo("UX321");
        nuevo.setAerolinea("Iberia");
        nuevo.setOrigen("MAD");
        nuevo.setDestino("EZE");
        OffsetDateTime futuro = OffsetDateTime.now().plusDays(5);
        nuevo.setDespegue(futuro);
        nuevo.setAterrizajeLocal(futuro.plusHours(12));
        nuevo.setPrecio(BigDecimal.TEN);
        nuevo.setMoneda("USD");
        nuevo.setEstadoVuelo(EstadoVuelo.EN_HORA);
        nuevo.setTipoAvion("A320");
        nuevo.setCapacidadAvion(180);
        return nuevo;
    }
}
