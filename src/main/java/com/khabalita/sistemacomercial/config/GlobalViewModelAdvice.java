package com.khabalita.sistemacomercial.config;

import com.khabalita.sistemacomercial.Service.BusinessLogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalViewModelAdvice {

    private final BusinessLogoService businessLogoService;

    @ModelAttribute
    public void addAuthenticatedUser(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("currentUsername", null);
            model.addAttribute("currentRole", null);
            model.addAttribute("isAdmin", false);
            model.addAttribute("businessLogoAvailable", false);
            return;
        }

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("USUARIO");
        model.addAttribute("currentUsername", authentication.getName());
        model.addAttribute("currentRole", role);
        model.addAttribute("isAdmin", "ADMIN".equals(role));
        model.addAttribute("businessLogoAvailable", businessLogoService.hasLogo());
    }
}
