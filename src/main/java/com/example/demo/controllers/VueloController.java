package com.example.demo.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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


@RestController
@RequestMapping("/vuelos")
public class VueloController {
    
    @Autowired
    private VueloService vueloService;
    

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Vuelo> buscarVuelos(
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam String fecha) {
        return vueloService.findByOrigenAndDestinoAndFecha(
                origen, destino, LocalDate.parse(fecha));
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
    

    
}
