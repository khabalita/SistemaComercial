package com.khabalita.sistemacomercial.config;

import com.khabalita.sistemacomercial.Entities.AppUser;
import com.khabalita.sistemacomercial.Entities.UserRole;
import com.khabalita.sistemacomercial.Repositories.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityDataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.initial-admin-username:admin}")
    private String username;

    @Value("${app.security.initial-admin-password:}")
    private String password;

    @Override
    public void run(String... args) {
        if (appUserRepository.findByUsernameIgnoreCase(username).isPresent()) return;
        if (password == null || password.isBlank()) {
            log.warn("No se creo el usuario admin inicial: defina ADMIN_INITIAL_PASSWORD");
            return;
        }
        appUserRepository.save(AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build());
        log.warn("Usuario administrador inicial creado: {}. Cambie la contraseña antes de producción.", username);
    }
}
