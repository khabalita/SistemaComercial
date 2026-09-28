package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.Entities.AppUser;
import com.khabalita.sistemacomercial.Entities.UserRole;
import com.khabalita.sistemacomercial.Repositories.AppUserRepository;
import com.khabalita.sistemacomercial.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserViewController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", appUserRepository.findAll());
        model.addAttribute("roles", UserRole.values());
        return "users/list";
    }

    @PostMapping
    public String create(@RequestParam String username, @RequestParam String password,
                         @RequestParam UserRole role, Model model, RedirectAttributes ra) {
        if (username.isBlank() || password.length() < 8) {
            model.addAttribute("errorMessage", "El usuario es obligatorio y la contraseña debe tener al menos 8 caracteres.");
            return list(model);
        }
        if (appUserRepository.findByUsernameIgnoreCase(username.trim()).isPresent()) {
            model.addAttribute("errorMessage", "El usuario ya existe.");
            return list(model);
        }
        AppUser user = appUserRepository.save(AppUser.builder()
                .username(username.trim())
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .enabled(true)
                .build());
        auditService.record("USER_CREATE", "AppUser", user.getId().toString(), "Usuario creado");
        ra.addFlashAttribute("ok", "Usuario creado.");
        return "redirect:/ui/users";
    }
}
