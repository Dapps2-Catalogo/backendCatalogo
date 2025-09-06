package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.models.Vuelo;

public class VueloService {

    

    public List<Vuelo> findByOrigenAndDestinoAndFecha(String origen, String destino, LocalDate localDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByOrigenAndDestinoAndFecha'");
    }

    public Vuelo createVuelo(Vuelo vuelo) {
        return vuelo;
    }

    public void deleteVuelo(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteVuelo'");
    }

    public Vuelo updateVuelo(Integer id, Vuelo request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateVuelo'");
    }
    
}
