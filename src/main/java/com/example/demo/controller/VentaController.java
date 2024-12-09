package com.example.demo.controller;

import com.example.demo.entity.Usuario;
import com.example.demo.model.OfertaModel;
import com.example.demo.model.VehiculoModel;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.OfertaService;
import com.example.demo.service.UsuarioService;
import com.example.demo.service.VehiculoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/venta")
public class VentaController {

    private final VehiculoService vehiculoService;
    private final UsuarioService usuarioService;
    private final OfertaService ofertaService;
    private final CloudinaryService cloudinaryService;
    private final JwtUtil jwtUtil;

    @Autowired
    public VentaController(VehiculoService vehiculoService, UsuarioService usuarioService,
                            OfertaService ofertaService, CloudinaryService cloudinaryService,
                            JwtUtil jwtUtil) {
        this.vehiculoService = vehiculoService;
        this.usuarioService = usuarioService;
        this.ofertaService = ofertaService;
        this.cloudinaryService = cloudinaryService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/mis-coches")
    public ResponseEntity<?> obtenerCochesDelUsuario(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Token no presente o inválido.");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body("Token inválido o expirado.");
        }

        String username = jwtUtil.extractUsername(token);
        Usuario usuario = usuarioService.findByUsername(username);

        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        List<VehiculoModel> cochesComprados = vehiculoService.getCochesByUsuarioId(usuario.getId());
        return ResponseEntity.ok(cochesComprados);
    }

    @PostMapping(value = "/vender-tu-coche/agregar", consumes = "multipart/form-data")
    public ResponseEntity<?> agregarVehiculo(
            @RequestHeader("Authorization") String token,
            @RequestPart("datosVehiculo") String datosVehiculo,
            @RequestPart("imagenFile") MultipartFile imagenFile) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token no proporcionado o inválido");
        }
        String username = jwtUtil.extractUsername(token.substring(7));
        if (username == null || !jwtUtil.isTokenValid(token.substring(7))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        OfertaModel ofertaModel;
        try {
            ofertaModel = objectMapper.readValue(datosVehiculo, OfertaModel.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al procesar los datos del vehículo");
        }

        try {
            String imageUrl = cloudinaryService.uploadImage(imagenFile);
            ofertaModel.setImagen(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir la imagen");
        }

        ofertaModel.setEstado("PENDIENTE");
        ofertaService.guardarOferta(ofertaModel);

        return ResponseEntity.ok("Vehículo agregado con éxito");
    }

    @GetMapping("/venta")
    public ResponseEntity<?> redirigirAMisCoches() {
        return ResponseEntity.ok("Redirigir a /mis-coches.");
    }
}
