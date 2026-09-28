package com.khabalita.sistemacomercial.config;

import com.khabalita.sistemacomercial.audit.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginAuditFailureHandler implements AuthenticationFailureHandler {

    private final AuditService auditService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        auditService.record("LOGIN_FAILURE", "AppUser", username,
                "Credenciales invalidas", "FAILURE");
        response.sendRedirect("/login?error");
    }
}
