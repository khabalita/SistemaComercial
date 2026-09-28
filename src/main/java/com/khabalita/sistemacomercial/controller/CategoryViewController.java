package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.Service.ICategoryService;
import com.khabalita.sistemacomercial.dto.request.CategoryRequestDto;
import com.khabalita.sistemacomercial.dto.response.CategoryResponseDto;
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
@RequestMapping("/ui/categories")
@RequiredArgsConstructor
public class CategoryViewController {

    private static final int PAGE_SIZE = 50;

    private final ICategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        var categoriesPage = categoryService.getCategoriesPage(name, PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id"))));
        model.addAttribute("categories", categoriesPage.getContent());
        model.addAttribute("page", categoriesPage);
        model.addAttribute("name", name);
        return "categories/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("category", CategoryRequestDto.builder().build());
        model.addAttribute("isEdit", false);
        model.addAttribute("itemId", null);
        return "categories/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid CategoryRequestDto dto, BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("category", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            return "categories/form";
        }
        try {
            categoryService.saveCategory(dto);
            ra.addFlashAttribute("ok", "Categoría creada.");
            return "redirect:/ui/categories";
        } catch (Exception e) {
            model.addAttribute("category", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", friendlyMessage(e));
            return "categories/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            CategoryResponseDto c = categoryService.getCategoryById(id);
            model.addAttribute("category", CategoryRequestDto.builder().name(c.name()).build());
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            return "categories/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/categories";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, @Valid CategoryRequestDto dto,
                         BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("category", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            return "categories/form";
        }
        try {
            categoryService.updateCategory(id, dto);
            ra.addFlashAttribute("ok", "Categoría actualizada.");
            return "redirect:/ui/categories";
        } catch (Exception e) {
            model.addAttribute("category", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("errorMessage", friendlyMessage(e));
            return "categories/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.deleteCategory(id);
            ra.addFlashAttribute("ok", "Categoría eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", friendlyMessage(e));
        }
        return "redirect:/ui/categories";
    }

    private String friendlyMessage(Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (msg.contains("unique") || msg.contains("unicidad") || msg.contains("uk")) {
            return "Ya existe una categoría con ese nombre.";
        }
        if (msg.contains("foreign") || msg.contains("integridad referencial") || msg.contains("constraint")) {
            return "No se puede eliminar: hay productos asociados a esta categoría.";
        }
        return e.getMessage();
    }
}
