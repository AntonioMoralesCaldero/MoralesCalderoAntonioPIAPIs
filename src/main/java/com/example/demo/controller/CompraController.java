// Autor: Antonio Miguel Morales Caldero
package com.example.demo.controller;

import com.example.demo.entity.Compra;
import com.example.demo.entity.Usuario;
import com.example.demo.entity.Vehiculo;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.VehiculoRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compra")
public class CompraController {

	private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompraService compraService;
    private final JwtUtil jwtUtil;

    @Autowired
    public CompraController(VehiculoRepository vehiculoRepository, UsuarioRepository usuarioRepository, CompraService compraService, JwtUtil jwtUtil) {
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.compraService = compraService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/{vehiculoId}")
    public ResponseEntity<?> comprarVehiculo(@PathVariable int vehiculoId,
                                             @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token no presente o inválido.");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body("Token inválido o expirado.");
        }

        String username = jwtUtil.extractUsername(token);
        Usuario usuario = usuarioRepository.findByUsername(username);

        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado.");
        }

        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                                              .orElseThrow(() -> new RuntimeException("Vehículo no encontrado"));

        if (vehiculo.isVendido()) {
            return ResponseEntity.badRequest().body("Este coche ya fue vendido.");
        }

        vehiculo.setVendido(true);
        vehiculoRepository.save(vehiculo);

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setVehiculo(vehiculo);
        compraService.registrarCompra(compra);

        return ResponseEntity.ok("¡Compra realizada con éxito!");
    }

}
