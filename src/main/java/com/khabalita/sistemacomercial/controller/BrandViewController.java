package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.Service.IBrandService;
import com.khabalita.sistemacomercial.dto.request.BrandRequestDto;
import com.khabalita.sistemacomercial.dto.response.BrandResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/ui/brands")
@RequiredArgsConstructor
public class BrandViewController {

    private static final int PAGE_SIZE = 50;

    private final IBrandService brandService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        var brandsPage = brandService.getBrandsPage(name, PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id"))));
        model.addAttribute("brands", brandsPage.getContent());
        model.addAttribute("page", brandsPage);
        model.addAttribute("name", name);
        return "brands/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("brand", BrandRequestDto.builder().build());
        model.addAttribute("isEdit", false);
        model.addAttribute("itemId", null);
        return "brands/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid BrandRequestDto dto, BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("brand", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            return "brands/form";
        }
        try {
            brandService.saveBrand(dto);
            ra.addFlashAttribute("ok", "Marca creada.");
            return "redirect:/ui/brands";
        } catch (Exception e) {
            model.addAttribute("brand", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", friendlyMessage(e));
            return "brands/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            BrandResponseDto b = brandService.getBrandById(id);
            model.addAttribute("brand", BrandRequestDto.builder().name(b.name()).build());
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            return "brands/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/brands";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, @Valid BrandRequestDto dto,
                         BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("brand", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            return "brands/form";
        }
        try {
            brandService.updateBrand(id, dto);
            ra.addFlashAttribute("ok", "Marca actualizada.");
            return "redirect:/ui/brands";
        } catch (Exception e) {
            model.addAttribute("brand", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("errorMessage", friendlyMessage(e));
            return "brands/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            brandService.deleteBrand(id);
            ra.addFlashAttribute("ok", "Marca eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", friendlyMessage(e));
        }
        return "redirect:/ui/brands";
    }

    private String friendlyMessage(Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (msg.contains("unique") || msg.contains("unicidad") || msg.contains("uk")) {
            return "Ya existe una marca con ese nombre.";
        }
        if (msg.contains("foreign") || msg.contains("integridad referencial") || msg.contains("constraint")) {
            return "No se puede eliminar: hay productos asociados a esta marca.";
        }
        return e.getMessage();
    }
}
