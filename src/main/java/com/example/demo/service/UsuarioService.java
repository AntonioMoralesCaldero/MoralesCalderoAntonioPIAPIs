// Autor: Antonio Miguel Morales Caldero
package com.example.demo.service;

import com.example.demo.entity.Usuario;
import com.example.demo.model.UsuarioModel;

import java.util.List;

public interface UsuarioService {

    UsuarioModel registrar(UsuarioModel usuarioModel);
    boolean authenticate(String username, String password);
    List<UsuarioModel> findAll();
    UsuarioModel findById(int id);
    Usuario findByUsername(String username);
    Usuario findUsuarioById(int id);
    UsuarioModel login(String username, String password);
    String generateToken(int userId);
    Integer validateToken(String token);
    void logout(String token);
}
