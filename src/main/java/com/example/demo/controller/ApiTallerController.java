// Autor: Antonio Miguel Morales Caldero
package com.example.demo.controller;

import com.example.demo.model.CitaModel;
import com.example.demo.model.ValoracionModel;
import com.example.demo.service.CitaService;
import com.example.demo.service.UsuarioService;
import com.example.demo.service.ValoracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taller")
public class ApiTallerController {

    private final CitaService citaService;
    private final UsuarioService usuarioService;
    private final ValoracionService valoracionService;

    @Autowired
    public ApiTallerController(CitaService citaService, UsuarioService usuarioService, ValoracionService valoracionService) {
        this.citaService = citaService;
        this.usuarioService = usuarioService;
        this.valoracionService = valoracionService;
    }

    @GetMapping("/citas")
    public ResponseEntity<List<CitaModel>> verCitas(@RequestHeader("Authorization") String token) {
        Integer userId = usuarioService.validateToken(token.substring(7));
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<CitaModel> citas = citaService.obtenerCitasPorUsuario(userId);
        return ResponseEntity.ok(citas);
    }

    @PostMapping("/citas/crear")
    public ResponseEntity<String> crearCita(
            @RequestHeader("Authorization") String token,
            @RequestBody CitaModel citaModel) {
        Integer userId = usuarioService.validateToken(token.substring(7));
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        citaModel.setUsuarioId(userId);
        citaService.crearCita(citaModel);

        return ResponseEntity.ok("Cita creada con éxito.");
    }

    @GetMapping("/citas/no-valoradas")
    public ResponseEntity<List<CitaModel>> verCitasNoValoradas(@RequestHeader("Authorization") String token) {
        Integer userId = usuarioService.validateToken(token.substring(7));
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<CitaModel> citasNoValoradas = citaService.obtenerCitasNoValoradasPorUsuario(userId);
        return ResponseEntity.ok(citasNoValoradas);
    }

    @PostMapping("/citas/{id}/valorar")
    public ResponseEntity<String> guardarValoracion(
            @RequestHeader("Authorization") String token,
            @PathVariable int id,
            @RequestBody ValoracionModel valoracionModel) {
        Integer userId = usuarioService.validateToken(token.substring(7));
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        CitaModel cita = citaService.obtenerCitaPorId(id);
        if (cita != null && cita.getEstado().equalsIgnoreCase("terminado") && !cita.isValorada() && cita.getUsuarioId() == userId) {
            valoracionModel.setCitaId(cita.getId());
            valoracionModel.setUsuarioId(userId);

            valoracionService.guardarValoracion(valoracionModel);

            cita.setValorada(true);
            citaService.actualizarCita(cita);

            return ResponseEntity.ok("Valoración guardada con éxito.");
        }

        return ResponseEntity.badRequest().body("La cita no es válida para valorar.");
    }
}
