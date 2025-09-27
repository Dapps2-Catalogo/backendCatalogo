package com.example.demo.controllers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PingControllerTest {

    private final PingController controller = new PingController();

    @Test
    void debeResponderPong() {
        String respuesta = controller.ping();
        assertThat(respuesta).isEqualTo("pong");
    }
}
