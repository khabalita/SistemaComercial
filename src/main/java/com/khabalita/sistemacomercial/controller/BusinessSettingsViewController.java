package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.Service.BusinessLogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/ui/settings")
@RequiredArgsConstructor
public class BusinessSettingsViewController {

    private final BusinessLogoService businessLogoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String settings(Model model) {
        model.addAttribute("logoAvailable", businessLogoService.hasLogo());
        return "settings/business";
    }

    @PostMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public String uploadLogo(@RequestPart("logo") MultipartFile logo, RedirectAttributes redirectAttributes) {
        try {
            businessLogoService.replaceLogo(logo);
            redirectAttributes.addFlashAttribute("ok", "Logo del negocio actualizado.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/ui/settings";
    }

    @PostMapping("/logo/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String removeLogo(RedirectAttributes redirectAttributes) {
        try {
            businessLogoService.removeLogo();
            redirectAttributes.addFlashAttribute("ok", "Logo eliminado.");
        } catch (IOException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/ui/settings";
    }

    @GetMapping("/logo")
    public ResponseEntity<byte[]> logo() throws IOException {
        BusinessLogoService.LogoData logo = businessLogoService.loadLogo();
        if (logo == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(logo.contentType()))
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .header("X-Content-Type-Options", "nosniff")
                .body(logo.content());
    }
}
