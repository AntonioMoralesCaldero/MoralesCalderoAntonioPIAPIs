// Autor: Antonio Miguel Morales Caldero
package com.example.demo.controller;

import com.example.demo.entity.Usuario;
import com.example.demo.model.CitaModel;
import com.example.demo.model.UsuarioModel;
import com.example.demo.model.ValoracionModel;
import com.example.demo.model.VehiculoModel;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.CitaService;
import com.example.demo.service.ValoracionService;
import com.example.demo.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taller")
public class TallerController {

    private final CitaService citaService;
    private final UsuarioRepository usuarioRepository;
    private final ValoracionService valoracionService;
    private final VehiculoService vehiculoService;
    private final JwtUtil jwtUtil;

    @Autowired
    public TallerController(CitaService citaService,
                            UsuarioRepository usuarioRepository,
                            ValoracionService valoracionService,
                            VehiculoService vehiculoService,
                            JwtUtil jwtUtil) {
        this.citaService = citaService;
        this.usuarioRepository = usuarioRepository;
        this.valoracionService = valoracionService;
        this.vehiculoService = vehiculoService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/citas")
    public ResponseEntity<?> verCitas(@RequestHeader("Authorization") String authHeader) {
        String username = validarToken(authHeader);
        Usuario usuario = usuarioRepository.findByUsername(username);

        List<CitaModel> citas = citaService.obtenerCitasPorUsuario(usuario.getId());
        return ResponseEntity.ok(citas);
    }

    @PostMapping("/citas/crear")
    public ResponseEntity<?> crearCita(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody CitaModel citaModel) {
        String username = validarToken(authHeader);
        Usuario usuario = usuarioRepository.findByUsername(username);

        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado.");
        }

        if (citaModel.getVehiculoOcasionId() == null) {
            return ResponseEntity.badRequest().body("Debe seleccionar un vehículo.");
        }

        UsuarioModel usuarioModel = new UsuarioModel();
        usuarioModel.setId(usuario.getId());
        usuarioModel.setNombre(usuario.getNombre());
        usuarioModel.setApellidos(usuario.getApellidos());
        usuarioModel.setUsername(usuario.getUsername());

        citaModel.setUsuario(usuarioModel);

        citaService.crearCita(citaModel);

        return ResponseEntity.ok("¡Cita creada con éxito!");
    }


    @PostMapping("/citas/{id}/valorar")
    public ResponseEntity<?> guardarValoracion(@RequestHeader("Authorization") String authHeader,
                                               @PathVariable int id,
                                               @RequestBody ValoracionModel valoracionModel) {
        String username = validarToken(authHeader);
        Usuario usuario = usuarioRepository.findByUsername(username);

        CitaModel cita = citaService.obtenerCitaPorId(id);

        if (cita == null || !cita.getEstado().equalsIgnoreCase("terminado") || cita.isValorada()) {
            return ResponseEntity.badRequest().body("La cita no puede ser valorada.");
        }

        valoracionModel.setCitaId(cita.getId());
        valoracionModel.setUsuarioId(usuario.getId());

        valoracionService.guardarValoracion(valoracionModel);

        cita.setValorada(true);
        citaService.actualizarCita(cita);

        return ResponseEntity.ok("¡Valoración guardada con éxito!");
    }

    @GetMapping("/citas/no-valoradas")
    public ResponseEntity<?> verCitasNoValoradas(@RequestHeader("Authorization") String authHeader) {
        String username = validarToken(authHeader);
        Usuario usuario = usuarioRepository.findByUsername(username);

        List<CitaModel> citasNoValoradas = citaService.obtenerCitasNoValoradasPorUsuario(usuario.getId());
        return ResponseEntity.ok(citasNoValoradas);
    }

    @GetMapping("/citas/{id}/ver-valoracion")
    public ResponseEntity<?> verValoracion(@RequestHeader("Authorization") String authHeader, @PathVariable int id) {
        String username = validarToken(authHeader);
        Usuario usuario = usuarioRepository.findByUsername(username);

        CitaModel cita = citaService.obtenerCitaPorId(id);

        if (cita == null || !cita.getEstado().equalsIgnoreCase("terminado") || !cita.isValorada()) {
            return ResponseEntity.badRequest().body("La cita no tiene valoraciones.");
        }

        ValoracionModel valoracion = valoracionService.obtenerValoracionPorCitaId(cita.getId());
        if (valoracion == null) {
            return ResponseEntity.badRequest().body("Valoración no encontrada.");
        }

        return ResponseEntity.ok(valoracion);
    }

    @PostMapping("/citas/{id}/guardar-valoracion")
    public ResponseEntity<?> actualizarValoracion(@RequestHeader("Authorization") String authHeader,
                                                  @PathVariable int id,
                                                  @RequestBody ValoracionModel valoracionModel) {
        String username = validarToken(authHeader);
        Usuario usuario = usuarioRepository.findByUsername(username);

        CitaModel cita = citaService.obtenerCitaPorId(id);

        if (cita == null || !cita.isValorada()) {
            return ResponseEntity.badRequest().body("No se puede actualizar la valoración.");
        }

        valoracionModel.setCitaId(cita.getId());
        valoracionModel.setUsuarioId(usuario.getId());
        valoracionService.actualizarValoracion(valoracionModel);

        return ResponseEntity.ok("¡Valoración actualizada con éxito!");
    }

    private String validarToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token no presente o inválido.");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Token inválido o expirado.");
        }

        return jwtUtil.extractUsername(token);
    }
   
}
