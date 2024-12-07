//Autor: Antonio Miguel Morales Caldero
package com.example.demo.controller;

import com.example.demo.model.VehiculoModel;
import com.example.demo.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final VehiculoService vehiculoService;

    @Autowired
    public CatalogoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public List<VehiculoModel> obtenerCatalogo() {
        return vehiculoService.getAllVehiculos();
    }

    @GetMapping("/{id}")
    public VehiculoModel obtenerDetalleVehiculo(@PathVariable int id) {
        return vehiculoService.getVehiculoById(id);
    }
}
