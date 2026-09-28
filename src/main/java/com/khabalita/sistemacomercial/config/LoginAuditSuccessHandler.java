package com.khabalita.sistemacomercial.config;

import com.khabalita.sistemacomercial.audit.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginAuditSuccessHandler implements AuthenticationSuccessHandler {

    private final AuditService auditService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        auditService.record("LOGIN_SUCCESS", "AppUser", authentication.getName(), "Inicio de sesion");
        response.sendRedirect("/ui/products");
    }
}
