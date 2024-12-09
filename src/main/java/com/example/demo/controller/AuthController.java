package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.UsuarioModel;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        UsuarioModel usuario = usuarioService.login(username, password);
        if (usuario != null) {
            String token = jwtUtil.generateToken(usuario.getUsername());
            return ResponseEntity.ok(Map.of("message", "Inicio de sesión exitoso", "token", token));
        } else {
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UsuarioModel usuarioModel) {
        try {
            usuarioService.registrar(usuarioModel);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado con éxito");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al registrar el usuario: " + e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token no presente o inválido.");
        }

        return ResponseEntity.ok("Sesión cerrada con éxito. Por favor, elimina el token del cliente.");
    }
}
