package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.Service.IProviderService;
import com.khabalita.sistemacomercial.dto.request.ProviderRequestDto;
import com.khabalita.sistemacomercial.dto.response.ProviderResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/ui/providers")
@RequiredArgsConstructor
public class ProviderViewController {

    private static final int PAGE_SIZE = 50;

    private final IProviderService providerService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        var providersPage = providerService.getProvidersPage(name, PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id"))));
        model.addAttribute("providers", providersPage.getContent());
        model.addAttribute("page", providersPage);
        model.addAttribute("name", name);
        return "providers/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("provider", ProviderRequestDto.builder().build());
        model.addAttribute("isEdit", false);
        model.addAttribute("itemId", null);
        return "providers/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid ProviderRequestDto dto, BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("provider", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            return "providers/form";
        }
        try {
            providerService.saveProvider(dto);
            ra.addFlashAttribute("ok", "Proveedor creado.");
            return "redirect:/ui/providers";
        } catch (Exception e) {
            model.addAttribute("provider", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", friendlyMessage(e));
            return "providers/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            ProviderResponseDto p = providerService.getProviderById(id);
            model.addAttribute("provider", toRequestDto(p));
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            return "providers/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/providers";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, @Valid ProviderRequestDto dto,
                         BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("provider", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            return "providers/form";
        }
        try {
            providerService.updateProvider(id, dto);
            ra.addFlashAttribute("ok", "Proveedor actualizado.");
            return "redirect:/ui/providers";
        } catch (Exception e) {
            model.addAttribute("provider", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("errorMessage", friendlyMessage(e));
            return "providers/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            providerService.deleteProvider(id);
            ra.addFlashAttribute("ok", "Proveedor eliminado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", friendlyMessage(e));
        }
        return "redirect:/ui/providers";
    }

    private ProviderRequestDto toRequestDto(ProviderResponseDto p) {
        return ProviderRequestDto.builder()
                .name(p.name())
                .taxId(p.taxId())
                .email(p.email())
                .phone(p.phone())
                .website(p.website())
                .address(p.address())
                .build();
    }

    private String friendlyMessage(Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (msg.contains("foreign") || msg.contains("integridad referencial") || msg.contains("constraint")) {
            return "No se puede eliminar: hay productos asociados a este proveedor.";
        }
        return e.getMessage();
    }
}
