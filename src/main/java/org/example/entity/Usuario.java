package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String usuario;
    private String password;
    private String nombre;
    private String rol; // En BD guardamos "ADMIN" o "USER"

    // --- METODOS DE USERDETAILS (COMO EN TU EJEMPLO) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Al añadir "ROLE_", Spring Security reconocerA hasRole('ADMIN') perfectamente
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.rol));
    }

    @Override
    public String getUsername() { return this.usuario; }

    @Override
    public String getPassword() { return this.password; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}