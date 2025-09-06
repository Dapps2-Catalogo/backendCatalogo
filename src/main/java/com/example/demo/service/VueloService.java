package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ConflictException;
import com.example.demo.models.Vuelo;
import com.example.demo.repositories.VueloRepository;

import jakarta.transaction.Transactional;

@Service
public class VueloService {

    @Autowired
    VueloRepository vueloRepository;

    @Transactional
    public Vuelo createVuelo(Vuelo vuelo) {
        validarNuevoVuelo(vuelo);

        // normalizaciones mínimas
        vuelo.setOrigen(vuelo.getOrigen().toUpperCase());
        vuelo.setDestino(vuelo.getDestino().toUpperCase());
        if (vuelo.getDisponibilidad() == null) {
            vuelo.setDisponibilidad(Collections.emptyList());
        }


        // conflicto: mismo idVuelo + fecha
        if (vueloRepository.existsByIdVueloAndFecha(vuelo.getIdVuelo(), vuelo.getFecha())) {
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

        // si cambian idVuelo o fecha, chequeá conflicto con otro registro
        boolean cambiaClave = (request.getIdVuelo() != null && !request.getIdVuelo().equals(actual.getIdVuelo()))
                           || (request.getFecha() != null && !request.getFecha().equals(actual.getFecha()));
        if (cambiaClave) {
            String nuevoIdVuelo = request.getIdVuelo() != null ? request.getIdVuelo() : actual.getIdVuelo();
            LocalDate nuevaFecha = request.getFecha() != null ? request.getFecha() : actual.getFecha();
            var choque = vueloRepository.findByIdVueloAndFecha(nuevoIdVuelo, nuevaFecha);
            if (choque.isPresent() && !choque.get().getId().equals(id)) {
                throw new ConflictException("Otro vuelo ya usa ese id_vuelo y fecha");
            }
        }

        // merge de campos permitidos
        if (request.getIdVuelo() != null) actual.setIdVuelo(request.getIdVuelo());
        if (request.getAerolinea() != null) actual.setAerolinea(request.getAerolinea());
        if (request.getFecha() != null) actual.setFecha(request.getFecha());
        if (request.getOrigen() != null) actual.setOrigen(request.getOrigen().toUpperCase());
        if (request.getDestino() != null) actual.setDestino(request.getDestino().toUpperCase());
        if (request.getPrecio() != null) actual.setPrecio(request.getPrecio());
        if (request.getHoraDespegueUtc() != null) actual.setHoraDespegueUtc(request.getHoraDespegueUtc());
        if (request.getHoraAterrizajeLocal() != null) actual.setHoraAterrizajeLocal(request.getHoraAterrizajeLocal());
        if (request.getEstadoVuelo() != null) actual.setEstadoVuelo(request.getEstadoVuelo());
        if (request.getDisponibilidad() != null) actual.setDisponibilidad(request.getDisponibilidad());

        return vueloRepository.save(actual);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Vuelo> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate fecha) {
        if (origen == null || destino == null || fecha == null) {
            throw new BadRequestException("Parámetros origen, destino y fecha son obligatorios");
        }
        if (origen.length() != 3 || destino.length() != 3) {
            throw new BadRequestException("Origen y destino deben ser códigos IATA de 3 letras");
        }
        // si no querés restringir búsquedas a futuro, quitá este check
        if (fecha.isBefore(LocalDate.now())) {
            throw new BadRequestException("La fecha de búsqueda no puede ser anterior a hoy");
        }
        return vueloRepository.findByOrigenAndDestinoAndFecha(origen.toUpperCase(), destino.toUpperCase(), fecha);
    }


    public Page<Vuelo> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate fecha, Pageable pageable) {
        if (origen == null || destino == null || fecha == null)
            throw new BadRequestException("Parámetros origen, destino y fecha son obligatorios");
        if (origen.length() != 3 || destino.length() != 3)
            throw new BadRequestException("Origen/Destino deben ser códigos IATA (3 letras)");
        // opcional: bloquear pasado
        // if (fecha.isBefore(LocalDate.now())) throw new BadRequestException("Fecha no puede ser pasada");

        return vueloRepository.findByOrigenAndDestinoAndFecha(
                origen.toUpperCase(), destino.toUpperCase(), fecha, pageable);
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
        if (v.getFecha() == null)
            throw new BadRequestException("fecha es obligatoria");
        if (v.getFecha().isBefore(LocalDate.now()))
            throw new BadRequestException("La fecha del vuelo no puede ser anterior a hoy");
        if (v.getPrecio() == null || menorQueCero(v.getPrecio()))
            throw new BadRequestException("precio debe ser >= 0");
        if (v.getHoraDespegueUtc() == null || v.getHoraAterrizajeLocal() == null)
            throw new BadRequestException("horas de despegue y aterrizaje son obligatorias");
        if (v.getEstadoVuelo() == null)
            throw new BadRequestException("estado_vuelo es obligatorio");
    }

    private void validarCamposEditables(Vuelo v) {
        if (v.getOrigen() != null && v.getOrigen().length() != 3)
            throw new BadRequestException("origen debe ser código IATA de 3 letras");
        if (v.getDestino() != null && v.getDestino().length() != 3)
            throw new BadRequestException("destino debe ser código IATA de 3 letras");
        if (v.getFecha() != null && v.getFecha().isBefore(LocalDate.now()))
            throw new BadRequestException("La fecha del vuelo no puede ser anterior a hoy");
        if (v.getPrecio() != null && menorQueCero(v.getPrecio()))
            throw new BadRequestException("precio debe ser >= 0");
    }

    private boolean menorQueCero(BigDecimal n) {
        return n.compareTo(BigDecimal.ZERO) < 0;
    }
}
