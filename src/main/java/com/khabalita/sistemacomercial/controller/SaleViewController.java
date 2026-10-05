package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.Entities.SalePaymentMethod;
import com.khabalita.sistemacomercial.Service.ICustomerService;
import com.khabalita.sistemacomercial.Service.IProductService;
import com.khabalita.sistemacomercial.Service.ISaleService;
import com.khabalita.sistemacomercial.dto.request.ItemRequestDto;
import com.khabalita.sistemacomercial.dto.request.SaleRequestDto;
import com.khabalita.sistemacomercial.dto.response.SaleResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ui/sales")
@RequiredArgsConstructor
public class SaleViewController {

    private static final int PAGE_SIZE = 50;

    private final ISaleService saleService;
    private final ICustomerService customerService;
    private final IProductService productService;

    @GetMapping
    public String list(@RequestParam(required = false) String customer,
                       @RequestParam(required = false) String product,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        var salesPage = saleService.getSalesPage(customer, product,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                        Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"))));
        model.addAttribute("sales", salesPage.getContent());
        model.addAttribute("page", salesPage);
        model.addAttribute("customer", customer);
        model.addAttribute("product", product);
        return "sales/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("lineCount", 3);
        model.addAttribute("paymentMethod", SalePaymentMethod.CASH);
        return "sales/form";
    }

    @PostMapping("/new")
    public String create(@RequestParam(required = false) Long customerId,
                         @RequestParam(required = false) String notes,
                          @RequestParam("productId") List<Long> productIds,
                           @RequestParam("quantity") List<Integer> quantities,
                           @RequestParam(value = "lineDiscount", required = false) List<BigDecimal> discounts,
                           @RequestParam(value = "generalDiscountPercent", required = false) BigDecimal generalDiscountPercent,
                           @RequestParam(defaultValue = "CASH") SalePaymentMethod paymentMethod,
                           RedirectAttributes ra, Model model) {
        try {
            SaleRequestDto dto = buildRequest(customerId, notes, productIds, quantities, discounts,
                    generalDiscountPercent, paymentMethod);
            SaleResponseDto created = saleService.createSale(dto);
            ra.addFlashAttribute("ok", "Venta " + created.number() + " creada.");
            return "redirect:/ui/sales/" + created.id();
        } catch (Exception e) {
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("lineCount", productIds.size());
            model.addAttribute("customerId", customerId);
            model.addAttribute("notes", notes);
            model.addAttribute("selectedProductIds", productIds);
            model.addAttribute("quantities", quantities);
            model.addAttribute("lineDiscounts", discounts == null ? new ArrayList<>() : discounts);
            model.addAttribute("generalDiscountPercent", generalDiscountPercent);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("errorMessage", friendlyMessage(e));
            return "sales/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("sale", saleService.getSaleById(id));
            return "sales/detail";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/sales";
        }
    }

    @GetMapping("/{id}/print")
    public String print(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("sale", saleService.getSaleById(id));
            return "sales/print";
        } catch (Exception e) {
            ra.addFlashAttribute("error", friendlyMessage(e));
            return "redirect:/ui/sales";
        }
    }

    private SaleRequestDto buildRequest(Long customerId, String notes,
                                          List<Long> productIds, List<Integer> quantities,
                                          List<BigDecimal> discounts,
                                          BigDecimal generalDiscountPercent,
                                          SalePaymentMethod paymentMethod) {
        List<ItemRequestDto> items = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            Long pid = productIds.get(i);
            Integer qty = quantities.get(i);
            if (pid == null || qty == null || qty <= 0) continue;
            BigDecimal disc = (discounts != null && i < discounts.size() && discounts.get(i) != null)
                    ? discounts.get(i) : BigDecimal.ZERO;
            items.add(ItemRequestDto.builder()
                    .productId(pid)
                    .quantity(qty)
                    .lineDiscount(disc)
                    .build());
        }
        return SaleRequestDto.builder()
                .customerId(customerId)
                .paymentMethod(paymentMethod)
                .notes(notes)
                .generalDiscountPercent(generalDiscountPercent)
                .items(items)
                .build();
    }

    private String friendlyMessage(Exception e) {
        return e.getMessage() == null ? "Error desconocido" : e.getMessage();
    }
}
