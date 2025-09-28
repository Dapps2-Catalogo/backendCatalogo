package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.demo.auxiliar.EstadoVuelo;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ConflictException;
import com.example.demo.models.Vuelo;
import com.example.demo.repositories.VueloRepository;
import jakarta.transaction.Transactional;



@Service
public class VueloService {
    private static OffsetDateTime startOfDayUtc(LocalDate d) {
        return d.atStartOfDay().atOffset(ZoneOffset.UTC);
    }
    private static OffsetDateTime nextDayUtc(LocalDate d) {
        return d.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
    }
    @Autowired
    VueloRepository vueloRepository;

    @Transactional
    public Vuelo createVuelo(Vuelo vuelo) {
        validarNuevoVuelo(vuelo);
        if (vueloRepository.existsByIdVuelo(vuelo.getIdVuelo())) {
            throw new ConflictException("Ya existe un vuelo con id " + vuelo.getIdVuelo());
        }
        // normalizaciones mínimas
        vuelo.setOrigen(vuelo.getOrigen().toUpperCase());
        vuelo.setDestino(vuelo.getDestino().toUpperCase());
        /* 
        if (vuelo.getDisponibilidad() == null) {
            vuelo.setDisponibilidad(Collections.emptyList());
        }

        */

        // conflicto: mismo idVuelo + fecha
        if (vueloRepository.existsByIdVueloAndDespegue(vuelo.getIdVuelo(), vuelo.getDespegue())) {
            throw new ConflictException("Ya existe un vuelo con ese id_vuelo en esa fecha");
        }

        return vueloRepository.save(vuelo);
    }

    @Transactional
    public void deleteVuelo(Integer id) {
        var vuelo = vueloRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Vuelo no encontrado con id " + id));
        vueloRepository.delete(vuelo);
    }

    @Transactional
    public Vuelo updateVuelo(Integer id, Vuelo request) {
        var actual = vueloRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Vuelo no encontrado con id " + id));

        validarCamposEditables(request);

        // <<< Regla: si está CANCELADO, no se puede volver atrás
        if (actual.getEstadoVuelo() == EstadoVuelo.CANCELADO) {
            // si intentan cambiar el estado a algo distinto de CANCELADO, lo bloqueamos
            if (request.getEstadoVuelo() != null && request.getEstadoVuelo() != EstadoVuelo.CANCELADO) {
                throw new ConflictException("El vuelo ya está CANCELADO y no puede cambiar de estado");
            }
            // (opcional) bloquear cualquier edición de otros campos:
            throw new ConflictException("El vuelo está CANCELADO y no admite modificaciones");
        }

        // si cambian idVuelo o fecha, chequeá conflicto con otro registro
        boolean cambiaClave = (request.getIdVuelo() != null && !request.getIdVuelo().equals(actual.getIdVuelo()))
                           || (request.getDespegue() != null && !request.getDespegue().equals(actual.getDespegue()));
        if (cambiaClave) {
            String nuevoIdVuelo = request.getIdVuelo() != null ? request.getIdVuelo() : actual.getIdVuelo();
            OffsetDateTime nuevaFecha = request.getDespegue() != null ? request.getDespegue() : actual.getDespegue();
            var choque = vueloRepository.findByIdVueloAndDespegue(nuevoIdVuelo, nuevaFecha);
            if (choque.isPresent() && !choque.get().getId().equals(id)) {
                throw new ConflictException("Otro vuelo ya usa ese id_vuelo y fecha");
            }
        }

        // merge de campos permitidos
        if (request.getIdVuelo() != null) actual.setIdVuelo(request.getIdVuelo());
        if (request.getAerolinea() != null) actual.setAerolinea(request.getAerolinea());
        if (request.getOrigen() != null) actual.setOrigen(request.getOrigen().toUpperCase());
        if (request.getDestino() != null) actual.setDestino(request.getDestino().toUpperCase());
        if (request.getPrecio() != null) actual.setPrecio(request.getPrecio());
        if (request.getDespegue() != null) actual.setDespegue(request.getDespegue());
        if (request.getAterrizajeLocal() != null) actual.setAterrizajeLocal(request.getAterrizajeLocal());
        if (request.getEstadoVuelo() != null) actual.setEstadoVuelo(request.getEstadoVuelo());
        if (request.getTipoAvion() != null) actual.setTipoAvion(request.getTipoAvion());
        if (request.getCapacidadAvion() != null) actual.setCapacidadAvion(request.getCapacidadAvion());
        if (request.getMoneda() != null) actual.setMoneda(request.getMoneda());
        
        //if (request.getDisponibilidad() != null) actual.setDisponibilidad(request.getDisponibilidad());

        return vueloRepository.save(actual);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<Vuelo> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate fecha, Pageable pageable) {
        if (origen == null || destino == null || fecha == null)
            throw new BadRequestException("Parámetros origen, destino y fecha son obligatorios");
        if (origen.length() != 3 || destino.length() != 3)
            throw new BadRequestException("Origen y destino deben ser códigos IATA de 3 letras");
        if (fecha.isBefore(LocalDate.now()))
            throw new BadRequestException("La fecha de búsqueda no puede ser anterior a hoy");

        OffsetDateTime desde = startOfDayUtc(fecha);
        OffsetDateTime hasta = nextDayUtc(fecha);

        return vueloRepository.findByOrigenAndDestinoAndDespegueBetween(
                origen.toUpperCase(),
                destino.toUpperCase(),
                desde,
                hasta,
                pageable
        );
    }












    public Page<Vuelo> buscarPorRango(
        String origen,
        String destino,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        Pageable pageable
    ) {
        if (origen == null || destino == null || fechaDesde == null || fechaHasta == null) {
            throw new BadRequestException("origen, destino, fechaDesde y fechaHasta son obligatorios");
        }
        if (origen.length() != 3 || destino.length() != 3) {
            throw new BadRequestException("Origen/Destino deben ser códigos IATA de 3 letras");
        }
        if (fechaDesde.isAfter(fechaHasta)) {
            throw new BadRequestException("fechaDesde no puede ser posterior a fechaHasta");
        }

        OffsetDateTime desde = startOfDayUtc(fechaDesde);
        OffsetDateTime hasta = nextDayUtc(fechaHasta);

        return vueloRepository.findByOrigenAndDestinoAndDespegueBetween(
                origen.toUpperCase(),
                destino.toUpperCase(),
                desde,
                hasta,
                pageable
        );
    }








    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<Vuelo> buscarVuelosPaginado(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String aerolinea,
            String origen,
            String destino,
            BigDecimal precioMin,
            BigDecimal precioMax,
            Pageable pageable
    ) {
        String aerolineaNorm = nullIfBlank(aerolinea);
        String origenNorm    = nullIfBlank(origen);
        String destinoNorm   = nullIfBlank(destino);

        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta))
            throw new BadRequestException("fechaDesde no puede ser posterior a fechaHasta");
        if (origenNorm != null && origenNorm.length() != 3)
            throw new BadRequestException("origen debe ser código IATA de 3 letras");
        if (destinoNorm != null && destinoNorm.length() != 3)
            throw new BadRequestException("destino debe ser código IATA de 3 letras");
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0)
            throw new BadRequestException("precioMin no puede ser mayor que precioMax");

        // 👇 clave: forzar el tipo genérico
        Specification<Vuelo> spec = Specification.<Vuelo>where(null)
            .and((root, cq, cb) -> {
                if (fechaDesde == null && fechaHasta == null) return null;
                var path = root.<LocalDate>get("fecha");
                if (fechaDesde != null && fechaHasta != null) return cb.between(path, fechaDesde, fechaHasta);
                if (fechaDesde != null) return cb.greaterThanOrEqualTo(path, fechaDesde);
                return cb.lessThanOrEqualTo(path, fechaHasta);
            })
            .and(ciLike("aerolinea", aerolineaNorm))
            .and((root, cq, cb) -> origenNorm == null ? null :
                    cb.equal(cb.upper(root.get("origen")), origenNorm.toUpperCase()))
            .and((root, cq, cb) -> destinoNorm == null ? null :
                    cb.equal(cb.upper(root.get("destino")), destinoNorm.toUpperCase()))
            .and((root, cq, cb) -> precioMin == null ? null :
                    cb.greaterThanOrEqualTo(root.get("precio"), precioMin))
            .and((root, cq, cb) -> precioMax == null ? null :
                    cb.lessThanOrEqualTo(root.get("precio"), precioMax));

        return vueloRepository.findAll(spec, pageable);
    }

    @org.springframework.transaction.annotation.Transactional
    public void normalizaciones (){
        vueloRepository.normalizeTiposAvionMasivo();
    }
    

















    // ---------- helpers de validación ----------

    private void validarNuevoVuelo(Vuelo v) {
        if (v == null) throw new BadRequestException("Body vacío");
        if (v.getIdVuelo() == null || v.getIdVuelo().isBlank())
            throw new BadRequestException("id_vuelo es obligatorio");
        if (v.getAerolinea() == null || v.getAerolinea().isBlank())
            throw new BadRequestException("aerolinea es obligatoria");
        if (v.getOrigen() == null || v.getOrigen().length() != 3)
            throw new BadRequestException("origen debe ser código IATA de 3 letras");
        if (v.getDestino() == null || v.getDestino().length() != 3)
            throw new BadRequestException("destino debe ser código IATA de 3 letras");
        if (v.getDespegue().toLocalDate().isBefore(LocalDate.now()))
            throw new BadRequestException("La fecha del vuelo no puede ser anterior a hoy");
        if (v.getPrecio() == null || menorQueCero(v.getPrecio()))
            throw new BadRequestException("precio debe ser >= 0");
        if (v.getDespegue() == null || v.getDespegue() == null)
            throw new BadRequestException("horas de despegue y aterrizaje son obligatorias");
        if (v.getEstadoVuelo() == null)
            throw new BadRequestException("estado_vuelo es obligatorio");
    }

    private void validarCamposEditables(Vuelo v) {
        if (v.getOrigen() != null && v.getOrigen().length() != 3)
            throw new BadRequestException("origen debe ser código IATA de 3 letras");
        if (v.getDestino() != null && v.getDestino().length() != 3)
            throw new BadRequestException("destino debe ser código IATA de 3 letras");
        if (v.getDespegue() != null && v.getDespegue().toLocalDate().isBefore(LocalDate.now()))
            throw new BadRequestException("La fecha del vuelo no puede ser anterior a hoy");
        if (v.getPrecio() != null && menorQueCero(v.getPrecio()))
            throw new BadRequestException("precio debe ser >= 0");
    }

    private boolean menorQueCero(BigDecimal n) {
        return n.compareTo(BigDecimal.ZERO) < 0;
    }




    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static Specification<Vuelo> ciLike(String field, String value) {
        return (root, cq, cb) -> {
            if (value == null) return null;
            return cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
        };
    }
}