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

                        // Permitimos explIcitamente el acceso a la URL de nuestro nuevo login
                        .requestMatchers("/login").permitAll()

                        // Acceso pUblico para escaneo de cOdigos QR
                        .requestMatchers("/producto/detalle/**").permitAll()

                        // Rutas restringidas Unicamente al rol Administrador
                        .requestMatchers("/nuevo", "/editar/**", "/eliminar/**", "/guardar").hasRole("ADMIN")

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