package com.example.demo.models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;



import com.example.demo.auxiliar.EstadoVuelo;

@Data
@Entity
@Table(name = "vuelo",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_vuelo_codigo_fecha", columnNames = {"id_vuelo", "despegue"})
  },
  indexes = {
    @Index(name = "idx_vuelo_origen_destino", columnList = "origen,destino"),
    @Index(name = "idx_vuelo_despegue", columnList = "despegue")
  }
)
public class Vuelo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "id_vuelo", length = 10, nullable = false, unique = false)
  private String idVuelo;

  @Column(name = "aerolinea", length = 50, nullable = false)
  private String aerolinea;

  @Column(name = "origen", length = 3, nullable = false)
  private String origen;

  @Column(name = "destino", length = 3, nullable = false)
  private String destino;

  @Column(name = "precio", precision = 12, scale = 2, nullable = false)
  private BigDecimal precio;

  @Column(name = "moneda", length = 3, nullable = false)
  private String moneda; // Ej: "ARS", "USD"

  @Column(name = "despegue", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime despegue;  // reemplaza a fecha + hora_despegue_utc

  @Column(name = "aterrizaje_local", columnDefinition = "timestamptz", nullable = false)
  private OffsetDateTime aterrizajeLocal;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_vuelo", length = 16, nullable = false)
  private EstadoVuelo estadoVuelo = EstadoVuelo.EN_HORA;

  @Column(name = "capacidad_avion", nullable = false)
  private Integer capacidadAvion;

  @Column(name = "tipo_avion", nullable = false)
  private String tipoAvion;
}


