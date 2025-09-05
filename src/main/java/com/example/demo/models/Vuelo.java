package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import com.example.demo.auxiliar.EstadoVuelo;

@Data
@Entity
@Table(name = "vuelo",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_vuelo_codigo_fecha", columnNames = {"id_vuelo", "fecha"})
  },
  indexes = {
    @Index(name = "idx_vuelo_origen_destino", columnList = "origen,destino"),
    @Index(name = "idx_vuelo_fecha", columnList = "fecha")
  }
)
public class Vuelo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "id_vuelo", length = 10, nullable = false)
  private String idVuelo;               // p.ej. "AR1234"

  @Column(name = "aerolinea", length = 50, nullable = false)
  private String aerolinea;             // p.ej. "Aerolineas Argentinas" o "AR"

  @Column(name = "fecha", nullable = false)
  private LocalDate fecha;              // fecha de salida (en origen)

  @Column(name = "origen", length = 3, nullable = false)
  private String origen;                // IATA, p.ej. "EZE"

  @Column(name = "destino", length = 3, nullable = false)
  private String destino;               // IATA, p.ej. "COR"

  @Column(name = "precio", precision = 12, scale = 2, nullable = false)
  private BigDecimal precio;            // evita float/double para dinero

  // Timestamp con zona: en PG usarás timestamptz
  @Column(name = "hora_despegue_utc", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime horaDespegueUtc;     // recomendado guardar en UTC

  @Column(name = "hora_aterrizaje_local", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime horaAterrizajeLocal; // en zona del destino (o también UTC si preferís)

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_vuelo", length = 16, nullable = false)
  private EstadoVuelo estadoVuelo = EstadoVuelo.CONFIRMADO;

  // Asientos disponibles tipo "1A","1B",...
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "vuelo_asientos", joinColumns = @JoinColumn(name = "vuelo_id"))
  @Column(name = "asiento", length = 4, nullable = false)
  private Set<String> disponibilidad = new HashSet<>();
}

