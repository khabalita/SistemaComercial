package com.khabalita.sistemacomercial.config;

import com.khabalita.sistemacomercial.Entities.AppUser;
import com.khabalita.sistemacomercial.Repositories.AppUserRepository;
import com.khabalita.sistemacomercial.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableMethodSecurity
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserRepository appUserRepository;
    private final AuthenticationSuccessHandler loginAuditSuccessHandler;
    private final LoginAuditFailureHandler loginAuditFailureHandler;
    private final AuditService auditService;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            AppUser appUser = appUserRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            return User.withUsername(appUser.getUsername())
                    .password(appUser.getPasswordHash())
                    .roles(appUser.getRole().name())
                    .disabled(!appUser.isEnabled())
                    .build();
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/error/**").permitAll()
                        .requestMatchers("/ui/**").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginAuditSuccessHandler)
                        .failureHandler(loginAuditFailureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(exception -> exception.accessDeniedHandler(
                        (request, response, accessDeniedException) -> {
                            auditService.record("ACCESS_DENIED", request.getRequestURI(), null,
                                    accessDeniedException.getClass().getSimpleName(), "FAILURE");
                            response.sendRedirect("/error/403");
                        }))
                .csrf(Customizer.withDefaults());
        return http.build();
    }
}
