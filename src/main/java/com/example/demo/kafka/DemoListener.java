package com.example.demo.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listener simple para debug.
 * Escucha mensajes publicados en topics relevantes y los imprime.
 */
@Component
public class DemoListener {

    // 🔹 Escucha los eventos directos del dominio Flights
    @KafkaListener(topics = "flights.events", groupId = "flights-catalog-debug", properties = { "auto.offset.reset=latest" })
    public void onFlightsEvents(String message) {
        System.out.println("📩 [Kafka] Mensaje recibido en flights.events:");
        System.out.println(message);
        System.out.println("------------------------------------------------------");
    }

    // 🔹 Escucha los mensajes que se envían al hub (core.ingress)
    @KafkaListener(topics = "core.ingress", groupId = "flights-catalog-debug-ingress", properties = { "auto.offset.reset=latest" })
    public void onCoreIngress(String message) {
        System.out.println("🌐 [Kafka] Mensaje recibido en core.ingress (hub):");
        System.out.println(message);
        System.out.println("------------------------------------------------------");
    }
}


