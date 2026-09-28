package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.Service.ICustomerService;
import com.khabalita.sistemacomercial.dto.request.CustomerRequestDto;
import com.khabalita.sistemacomercial.dto.response.CustomerResponseDto;
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
@RequestMapping("/ui/customers")
@RequiredArgsConstructor
public class CustomerViewController {

    private static final int PAGE_SIZE = 50;

    private final ICustomerService customerService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(defaultValue = "0") int page, Model model) {
        var customersPage = customerService.getCustomersPage(name,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                        Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id"))));
        model.addAttribute("customers", customersPage.getContent());
        model.addAttribute("page", customersPage);
        model.addAttribute("name", name);
        return "customers/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("customer", emptyRequest());
        model.addAttribute("isEdit", false);
        model.addAttribute("itemId", null);
        return "customers/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid CustomerRequestDto dto, BindingResult binding,
                         Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("customer", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            return "customers/form";
        }
        try {
            customerService.saveCustomer(dto);
            ra.addFlashAttribute("ok", "Cliente creado.");
            return "redirect:/ui/customers";
        } catch (Exception e) {
            model.addAttribute("customer", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", friendlyMessage(e));
            return "customers/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            CustomerResponseDto c = customerService.getCustomerById(id);
            model.addAttribute("customer", toRequestDto(c));
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            return "customers/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/customers";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, @Valid CustomerRequestDto dto,
                         BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("customer", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            return "customers/form";
        }
        try {
            customerService.updateCustomer(id, dto);
            ra.addFlashAttribute("ok", "Cliente actualizado.");
            return "redirect:/ui/customers";
        } catch (Exception e) {
            model.addAttribute("customer", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("itemId", id);
            model.addAttribute("errorMessage", friendlyMessage(e));
            return "customers/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            customerService.deleteCustomer(id);
            ra.addFlashAttribute("ok", "Cliente eliminado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", friendlyMessage(e));
        }
        return "redirect:/ui/customers";
    }

    private CustomerRequestDto emptyRequest() {
        return CustomerRequestDto.builder().active(true).build();
    }

    private CustomerRequestDto toRequestDto(CustomerResponseDto c) {
        return CustomerRequestDto.builder()
                .name(c.name())
                .taxId(c.taxId())
                .address(c.address())
                .city(c.city())
                .province(c.province())
                .postalCode(c.postalCode())
                .phone(c.phone())
                .email(c.email())
                .notes(c.notes())
                .active(c.active())
                .build();
    }

    private String friendlyMessage(Exception e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (msg.contains("foreign") || msg.contains("integridad referencial") || msg.contains("constraint")) {
            return "No se puede eliminar: hay ventas u otros registros asociados a este cliente.";
        }
        return e.getMessage();
    }
}
