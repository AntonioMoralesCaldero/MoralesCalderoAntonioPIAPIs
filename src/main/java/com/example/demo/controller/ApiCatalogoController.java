// Autor: Antonio Miguel Morales Caldero
package com.example.demo.controller;

import com.example.demo.model.VehiculoModel;
import com.example.demo.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class ApiCatalogoController {

    private final VehiculoService vehiculoService;

    @Autowired
    public ApiCatalogoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public ResponseEntity<List<VehiculoModel>> getAllVehiculos() {
        List<VehiculoModel> vehiculos = vehiculoService.getAllVehiculos();
        return ResponseEntity.ok(vehiculos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoModel> getVehiculoById(@PathVariable int id) {
        VehiculoModel vehiculo = vehiculoService.getVehiculoById(id);
        if (vehiculo != null) {
            return ResponseEntity.ok(vehiculo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
