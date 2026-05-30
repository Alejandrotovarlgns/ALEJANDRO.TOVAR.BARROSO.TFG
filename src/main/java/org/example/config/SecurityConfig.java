package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                        .disable()
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // ACCESO PÚBLICO LIMPIO: Solo permitimos el Login y ver el detalle de la zapatilla sin cuenta
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/producto/detalle/**").permitAll()

                        // ACCESO COMPRA Y CARRITO: Rutas exclusivas para que el cliente gestione su cesta y compre
                        .requestMatchers("/productos/comprar", "/carrito/**").hasRole("CLIENTE")

                        // ACCESO AL CATÁLOGO: El inventario general ahora lo comparten los tres roles
                        .requestMatchers("/inventario").hasAnyRole("ADMIN", "USER", "CLIENTE")

                        // ACCESO GESTIÓN: Rutas restringidas únicamente al rol Administrador (añadido las de borrado modal)
                        .requestMatchers("/nuevo", "/editar/**", "/eliminar/**", "/guardar",
                                "/productos/eliminar-talla", "/productos/eliminar-completo/**",
                                "/productos/exportar/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .formLogin(form -> form
                        .loginPage("/login") // Indicamos a Spring que use nuestra vista personalizada
                        .defaultSuccessUrl("/inventario", true) // Al loguearse con éxito redirige al panel
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}