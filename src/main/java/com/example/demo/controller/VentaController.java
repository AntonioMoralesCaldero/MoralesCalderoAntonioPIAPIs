package com.example.demo.controller;

import com.example.demo.entity.Usuario;
import com.example.demo.model.OfertaModel;
import com.example.demo.model.VehiculoModel;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.OfertaService;
import com.example.demo.service.UsuarioService;
import com.example.demo.service.VehiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/venta")
public class VentaController {

    private final VehiculoService vehiculoService;
    private final UsuarioService usuarioService;
    private final OfertaService ofertaService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public VentaController(VehiculoService vehiculoService, UsuarioService usuarioService,
                              OfertaService ofertaService, CloudinaryService cloudinaryService) {
        this.vehiculoService = vehiculoService;
        this.usuarioService = usuarioService;
        this.ofertaService = ofertaService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/mis-coches")
    public ResponseEntity<?> obtenerCochesDelUsuario(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado.");
        }

        Usuario usuario = usuarioService.findByUsername(principal.getName());
        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        List<VehiculoModel> cochesComprados = vehiculoService.getCochesByUsuarioId(usuario.getId());
        return ResponseEntity.ok(cochesComprados);
    }

    @PostMapping("/vender-tu-coche/agregar")
    public ResponseEntity<?> agregarVehiculo(@RequestPart("oferta") OfertaModel ofertaModel,
                                             @RequestPart("imagenFile") MultipartFile imagenFile,
                                             Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado.");
        }

        Usuario usuario = usuarioService.findByUsername(principal.getName());
        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado.");
        }

        try {
            String imageUrl = cloudinaryService.uploadImage(imagenFile);
            ofertaModel.setImagen(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al subir la imagen.");
        }

        ofertaModel.setEstado("PENDIENTE");
        ofertaService.guardarOferta(ofertaModel);

        return ResponseEntity.ok("Oferta creada exitosamente.");
    }

    @GetMapping("/venta")
    public ResponseEntity<?> redirigirAMisCoches() {
        return ResponseEntity.ok("Redirigir a /mis-coches.");
    }
}
