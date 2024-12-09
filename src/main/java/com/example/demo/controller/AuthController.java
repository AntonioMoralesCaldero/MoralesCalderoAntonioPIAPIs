package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.UsuarioModel;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;

    @Autowired
    public AuthController(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil, UsuarioService usuarioService) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        try {
            // Intenta cargar al usuario o administrador
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            // Verifica la contraseña manualmente (puedes usar bcrypt si está configurado)
            if (userDetails.getPassword().equals(password)) {
                // Genera un token JWT para cualquier usuario válido
                String token = jwtUtil.generateToken(userDetails.getUsername());
                return ResponseEntity.ok(Map.of("message", "Inicio de sesión exitoso", "token", token));
            } else {
                return ResponseEntity.status(401).body("Usuario o contraseña incorrectos.");
            }
        } catch (Exception e) {
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
