package com.example.demo.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listener de debug para verificar el ruteo del core.
 * Muestra los mensajes que llegan a los destinos configurados.
 */
@Component
public class DestinationsListener {

    // 🔹 Reenvío hacia el servicio de reservas
    @KafkaListener(topics = "reservations.events", groupId = "flights-catalog-debug-res", properties = { "auto.offset.reset=latest" })
    public void onReservations(String message) {
        System.out.println("📦 [Kafka] Mensaje recibido en reservations.events:");
        System.out.println(message);
        System.out.println("------------------------------------------------------");
    }

    // 🔹 Reenvío hacia el servicio de búsqueda
    @KafkaListener(topics = "search.events", groupId = "flights-catalog-debug-search", properties = { "auto.offset.reset=latest" })
    public void onSearch(String message) {
        System.out.println("🔎 [Kafka] Mensaje recibido en search.events:");
        System.out.println(message);
        System.out.println("------------------------------------------------------");
    }

    // 🔹 Reenvío hacia el servicio de métricas
    @KafkaListener(topics = "metrics.events", groupId = "flights-catalog-debug-metrics", properties = { "auto.offset.reset=latest" })
    public void onMetrics(String message) {
        System.out.println("📈 [Kafka] Mensaje recibido en metrics.events:");
        System.out.println(message);
        System.out.println("------------------------------------------------------");
    }
}

