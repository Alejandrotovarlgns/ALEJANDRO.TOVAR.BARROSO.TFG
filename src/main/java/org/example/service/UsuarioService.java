package org.example.service;

import org.example.entity.Usuario;
import org.example.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // Importamos el codificador
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Añadimos el codificador final

    // Los inyectamos juntos de forma limpia en el constructor original
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tu método original que busca por tu columna exacta de MySQL
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return usuario;
    }

    // --- NUEVO MÉTODO DE REGISTRO PARA EL ROL CLIENTE ---
    public void registrarNuevoCliente(Usuario usuario) {
        // 1. Haseamos la contraseña usando tu BCrypt inyectado
        String contraseniaHaseada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(contraseniaHaseada);

        // 2. Le asignamos fijado el nuevo rol de cliente
        // (Quitamos el setEnabled que te daba error de compilación)
        usuario.setRol("ROLE_CLIENTE");

        // 3. Guardamos en el repositorio
        usuarioRepository.save(usuario);
    }
}