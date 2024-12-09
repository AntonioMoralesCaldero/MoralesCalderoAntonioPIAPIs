// Autor: Antonio Miguel Morales Caldero
package com.example.demo.controller;

import com.example.demo.entity.Oferta;
import com.example.demo.model.CitaModel;
import com.example.demo.model.ValoracionModel;
import com.example.demo.model.VehiculoModel;
import com.example.demo.service.CitaService;
import com.example.demo.service.OfertaService;
import com.example.demo.service.ValoracionService;
import com.example.demo.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OfertaService ofertaService;
    private final VehiculoService vehiculoService;
    private final CitaService citaService;
    private final ValoracionService valoracionService;

    @Autowired
    public AdminController(OfertaService ofertaService, VehiculoService vehiculoService,
                           CitaService citaService, ValoracionService valoracionService) {
        this.ofertaService = ofertaService;
        this.vehiculoService = vehiculoService;
        this.citaService = citaService;
        this.valoracionService = valoracionService;
    }

    @GetMapping("/ofertas")
    public ResponseEntity<List<Oferta>> verOfertas() {
        List<Oferta> ofertasPendientes = ofertaService.obtenerOfertasPendientes();
        return ResponseEntity.ok(ofertasPendientes);
    }

    @PostMapping("/ofertas/{id}/aceptar")
    public ResponseEntity<String> aceptarOferta(@PathVariable int id) {
        Oferta oferta = ofertaService.obtenerOfertaPorId(id);
        if (oferta != null) {
            oferta.setEstado("aceptada");
            ofertaService.actualizarOferta(oferta);
            vehiculoService.agregarVehiculoDesdeOferta(oferta);
            return ResponseEntity.ok("Oferta aceptada exitosamente.");
        }
        return ResponseEntity.badRequest().body("Oferta no encontrada.");
    }

    @PostMapping("/ofertas/{id}/rechazar")
    public ResponseEntity<String> rechazarOferta(@PathVariable int id) {
        Oferta oferta = ofertaService.obtenerOfertaPorId(id);
        if (oferta != null) {
            oferta.setEstado("rechazada");
            ofertaService.actualizarOferta(oferta);
            return ResponseEntity.ok("Oferta rechazada exitosamente.");
        }
        return ResponseEntity.badRequest().body("Oferta no encontrada.");
    }

    @GetMapping("/citas")
    public ResponseEntity<List<CitaModel>> verCitas() {
        List<CitaModel> citas = citaService.obtenerTodasCitas();
        return ResponseEntity.ok(citas);
    }

    @PostMapping("/citas/{id}/actualizar")
    public ResponseEntity<String> actualizarCita(@PathVariable int id,
                                                 @RequestBody CitaModel citaActualizada) {
        CitaModel cita = citaService.obtenerCitaPorId(id);
        if (cita != null) {
            cita.setDiagnostico(citaActualizada.getDiagnostico());
            cita.setEstado(citaActualizada.getEstado());
            cita.setFechaCita(citaActualizada.getFechaCita());
            cita.setFechaReparacionFinalizada(citaActualizada.getFechaReparacionFinalizada());
            cita.setVehiculoOcasionId(citaActualizada.getVehiculoOcasionId());
            citaService.actualizarCita(cita);
            return ResponseEntity.ok("Cita actualizada exitosamente.");
        }
        return ResponseEntity.badRequest().body("Cita no encontrada.");
    }

    @PostMapping("/citas/{id}/cancelar")
    public ResponseEntity<String> cancelarCita(@PathVariable int id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.ok("Cita cancelada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al cancelar la cita.");
        }
    }

    @GetMapping("/valoraciones")
    public ResponseEntity<List<ValoracionModel>> listarValoraciones() {
        List<ValoracionModel> valoraciones = valoracionService.obtenerTodasValoraciones();
        return ResponseEntity.ok(valoraciones);
    }

    @PostMapping("/valoraciones/{id}/editar")
    public ResponseEntity<String> editarComentarioValoracion(@PathVariable int id,
                                                             @RequestBody ValoracionModel valoracionActualizada) {
        ValoracionModel valoracion = valoracionService.obtenerValoracionPorId(id);
        if (valoracion != null) {
            valoracion.setComentario(valoracionActualizada.getComentario());
            valoracionService.actualizarValoracion(valoracion);
            return ResponseEntity.ok("Comentario de valoración actualizado exitosamente.");
        }
        return ResponseEntity.badRequest().body("Valoración no encontrada.");
    }
}
