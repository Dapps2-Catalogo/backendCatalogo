package com.example.demo.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listener simple para debug.
 * Escucha los mensajes publicados en el topic flights.events
 * y los imprime por consola.
 */
@Component
public class DemoListener {

    @KafkaListener(topics = "flights.events", groupId = "flights-catalog-debug")
    public void onMessage(String message) {
        System.out.println("📩 [Kafka] Mensaje recibido en flights.events:");
        System.out.println(message);
        System.out.println("------------------------------------------------------");
    }
}

