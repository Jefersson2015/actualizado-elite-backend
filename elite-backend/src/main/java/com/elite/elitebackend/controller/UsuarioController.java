package com.elite.elitebackend.controller;

import com.elite.elitebackend.model.Usuario;
import com.elite.elitebackend.model.UsuarioResponse;
import com.elite.elitebackend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Instancia del encoder de BCrypt para manejar contraseñas seguras
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario) {
        //  Encriptamos la contraseña antes de persistirla
        String passwordPlano = usuario.getPassword();
        usuario.setPassword(passwordEncoder.encode(passwordPlano));

        return usuarioRepository.save(usuario);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario credenciales) {
        // Buscamos el usuario por su correo/username
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(credenciales.getUsername());

        // Si el usuario existe, validamos la contraseña con BCrypt
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // matches() compara el texto plano con el hash almacenado
            boolean esValida = passwordEncoder.matches(credenciales.getPassword(), usuario.getPassword());

            if (esValida) {
                // Contraseña correcta: retornamos respuesta segura (sin password)
                return ResponseEntity.ok(new UsuarioResponse(usuario));
            }
        }

        // Retornamos 401 con mensaje de error
        return ResponseEntity.status(401).body(Map.of("mensaje", "Error en la autenticación"));
    }

    @GetMapping
    public java.util.List<Usuario> listar() {
        return usuarioRepository.findAll();
    }
}