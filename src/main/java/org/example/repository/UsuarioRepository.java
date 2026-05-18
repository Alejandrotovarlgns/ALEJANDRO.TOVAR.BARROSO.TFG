package org.example.repository;

import org.example.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Añadimos este para que el Service pueda buscar al usuario antes de validar la clave
    Optional<Usuario> findByUsuario(String usuario);
}