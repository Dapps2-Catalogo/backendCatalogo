package com.example.demo.controllers;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.Vuelo;
import com.example.demo.service.VueloService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("/vuelos")
public class VueloController {
    
    @Autowired
    private VueloService vueloService;
    

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<Vuelo> buscarVuelos(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam String fecha,
            @PageableDefault(size = 8, sort = "horaDespegueUtc") Pageable pageable
    ) {
        return vueloService.findByOrigenAndDestinoAndFecha(origen, destino, LocalDate.parse(fecha), pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vuelo creatVuelo(@RequestBody Vuelo request) {
        return vueloService.createVuelo(request);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVuelo(@PathVariable Integer id){
        vueloService.deleteVuelo(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Vuelo updateVuelo(@PathVariable Integer id, @RequestBody Vuelo request) {
        return vueloService.updateVuelo(id, request);
    }




    @GetMapping("/rango")
    @ResponseStatus(HttpStatus.OK)
    public Page<Vuelo> buscarVuelosPorRango(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam String fechaDesde,   // ISO yyyy-MM-dd
            @RequestParam String fechaHasta,   // ISO yyyy-MM-dd
            @PageableDefault(size = 8, sort = "horaDespegueUtc") Pageable pageable
    ) {
        return vueloService.buscarPorRango(
                origen,
                destino,
                LocalDate.parse(fechaDesde),
                LocalDate.parse(fechaHasta),
                pageable
        );
    }
    

    
}
