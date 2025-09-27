package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        vuelo.setEstadoVuelo(EstadoVuelo.CONFIRMADO);
        vuelo.setTipoAvion("A350");
        vuelo.setCapacidadAvion(300);
    }

    @Test
    void testCreateVuelo_Success() {
        when(vueloRepository.existsByIdVuelo(anyString())).thenReturn(false);
    when(vueloRepository.existsByIdVueloAndFecha(anyString(), any(LocalDate.class))).thenReturn(false);
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
    void testCreateVuelo_ConflictByIdAndFecha() {
        when(vueloRepository.existsByIdVuelo(anyString())).thenReturn(false);
        when(vueloRepository.existsByIdVueloAndFecha(anyString(), any(LocalDate.class))).thenReturn(true);

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
        request.setEstadoVuelo(EstadoVuelo.CONFIRMADO);

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
        when(vueloRepository.findByOrigenAndDestinoAndFecha(origen, destino, fecha)).thenReturn(Arrays.asList(vuelo));

        List<Vuelo> vuelos = vueloService.findByOrigenAndDestinoAndFecha(origen, destino, fecha);

        assertNotNull(vuelos);
        assertEquals(1, vuelos.size());
        verify(vueloRepository, times(1)).findByOrigenAndDestinoAndFecha(origen, destino, fecha);
    }

    @Test
    void testFindByOrigenAndDestinoAndFecha_BadRequest() {
        assertThrows(BadRequestException.class, () -> {
            vueloService.findByOrigenAndDestinoAndFecha(null, "EZE", LocalDate.now());
        });
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
    void testUpdateVuelo_KeyConflict() {
        Vuelo otroVuelo = new Vuelo();
        otroVuelo.setId(2);
        otroVuelo.setIdVuelo("IB456");
    otroVuelo.setDespegue(vuelo.getDespegue());

        Vuelo request = new Vuelo();
        request.setIdVuelo("IB456"); // Intentamos cambiar a un ID que ya existe en esa fecha

        when(vueloRepository.findById(1)).thenReturn(Optional.of(vuelo));
    when(vueloRepository.findByIdVueloAndFecha("IB456", vuelo.getDespegue().toLocalDate())).thenReturn(Optional.of(otroVuelo));

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
}
