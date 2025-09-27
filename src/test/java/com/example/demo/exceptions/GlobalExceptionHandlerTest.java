package com.example.demo.exceptions;

import com.example.demo.auxiliar.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void debeGenerarRespuesta500ParaErroresGenericos() {
        ResponseEntity<ErrorResponse> response = handler.handleException(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Ocurrió un error interno");
        assertThat(response.getBody().getCode()).isEqualTo(500);
    }

    @Test
    void debeResponder404CuandoNoSeEncuentraElemento() {
        ResponseEntity<ErrorResponse> response = handler.handleNoSuchElementException(
                new NoSuchElementException("no existe"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("no existe");
        assertThat(response.getBody().getCode()).isEqualTo(404);
    }

    @Test
    void debeResponder409ParaConflictos() {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(
                new ConflictException("conflicto detectado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("conflicto detectado");
        assertThat(response.getBody().getCode()).isEqualTo(409);
    }

    @Test
    void debeResponder400ParaBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(
                new BadRequestException("petición inválida"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("petición inválida");
        assertThat(response.getBody().getCode()).isEqualTo(400);
    }

    @Test
    void debeResponder401ParaUnauthorized() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(
                new UnauthorizedException("sin permisos"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("sin permisos");
        assertThat(response.getBody().getCode()).isEqualTo(401);
    }
}
