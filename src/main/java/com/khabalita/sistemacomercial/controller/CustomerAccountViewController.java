package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.Service.ICustomerAccountService;
import com.khabalita.sistemacomercial.dto.request.CustomerAccountPaymentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ui/customers/{customerId}/account")
@RequiredArgsConstructor
public class CustomerAccountViewController {

    private final ICustomerAccountService accountService;

    @GetMapping
    public String account(@PathVariable Long customerId, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("account", accountService.getAccount(customerId));
            model.addAttribute("payment", new CustomerAccountPaymentRequest(null, null));
            return "customers/account";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/customers";
        }
    }

    @PostMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public String payment(@PathVariable Long customerId,
                          @Valid CustomerAccountPaymentRequest payment,
                          BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("account", accountService.getAccount(customerId));
            model.addAttribute("payment", payment);
            model.addAttribute("errorMessage", "Revisa el importe y el concepto del pago.");
            return "customers/account";
        }
        try {
            accountService.registerPayment(customerId, payment);
            ra.addFlashAttribute("ok", "Pago registrado en la cuenta corriente.");
            return "redirect:/ui/customers/" + customerId + "/account";
        } catch (Exception e) {
            model.addAttribute("account", accountService.getAccount(customerId));
            model.addAttribute("payment", payment);
            model.addAttribute("errorMessage", e.getMessage());
            return "customers/account";
        }
    }
}
