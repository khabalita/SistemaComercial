package com.khabalita.sistemacomercial.controller;

import com.khabalita.sistemacomercial.dto.request.ProductRequestDto;
import com.khabalita.sistemacomercial.dto.response.ProductResponseDto;
import com.khabalita.sistemacomercial.Service.IBrandService;
import com.khabalita.sistemacomercial.Service.ICategoryService;
import com.khabalita.sistemacomercial.Service.ICoinService;
import com.khabalita.sistemacomercial.Service.IIvaTypeService;
import com.khabalita.sistemacomercial.Service.IProductService;
import com.khabalita.sistemacomercial.Service.IProviderService;
import com.khabalita.sistemacomercial.dto.response.ProductImportResult;
import com.khabalita.sistemacomercial.dto.request.PriceUpdateRequest;
import com.khabalita.sistemacomercial.Service.ProductImportService;
import com.khabalita.sistemacomercial.Service.ProductImageService;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.io.IOException;

@Controller
@RequestMapping("/ui/products")
@RequiredArgsConstructor
public class ProductViewController {

    private static final int PAGE_SIZE = 50;

    private final IProductService productService;
    private final ICategoryService categoryService;
    private final IBrandService brandService;
    private final IProviderService providerService;
    private final ICoinService coinService;
    private final IIvaTypeService ivaTypeService;
    private final ProductImportService productImportService;
    private final ProductImageService productImageService;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String brand,
                       @RequestParam(required = false) String provider,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id")));
        var productsPage = productService.getProductsPage(name, category, brand, provider, pageable);
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("page", productsPage);
        model.addAttribute("name", name);
        model.addAttribute("category", category);
        model.addAttribute("brand", brand);
        model.addAttribute("provider", provider);
        return "products/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("product", emptyProduct());
        model.addAttribute("isEdit", false);
        model.addAttribute("productId", null);
        addLookups(model);
        return "products/form";
    }

    @GetMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public String importForm() {
        return "products/import";
    }

    @GetMapping("/prices")
    @PreAuthorize("hasRole('ADMIN')")
    public String pricesForm(Model model) {
        addPriceLookups(model);
        return "products/prices";
    }

    @PostMapping("/prices/preview")
    @PreAuthorize("hasRole('ADMIN')")
    public String previewPrices(@RequestParam(required = false) Long brandId,
                                @RequestParam String adjustmentType,
                                @RequestParam BigDecimal value, Model model) {
        PriceUpdateRequest request = new PriceUpdateRequest(brandId, adjustmentType, value);
        try {
            model.addAttribute("preview", productService.previewPriceUpdate(request));
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("brandId", brandId);
        model.addAttribute("adjustmentType", adjustmentType);
        model.addAttribute("value", value);
        addPriceLookups(model);
        return "products/prices";
    }

    @PostMapping("/prices/apply")
    @PreAuthorize("hasRole('ADMIN')")
    public String applyPrices(@RequestParam(required = false) Long brandId,
                              @RequestParam String adjustmentType,
                              @RequestParam BigDecimal value, RedirectAttributes ra) {
        try {
            int affected = productService.applyPriceUpdate(new PriceUpdateRequest(brandId, adjustmentType, value));
            ra.addFlashAttribute("ok", "Precios actualizados: " + affected + " productos.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/products";
    }

    @PostMapping("/import/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public String validateImport(@RequestPart("file") MultipartFile file, Model model) {
        model.addAttribute("result", productImportService.validate(file));
        return "products/import";
    }

    @PostMapping("/import/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveImport(@RequestPart("file") MultipartFile file, Model model, RedirectAttributes ra) {
        ProductImportResult result = productImportService.importProducts(file);
        if (result.valid()) {
            ra.addFlashAttribute("ok", "Importacion completada: " + result.acceptedRows() + " productos creados.");
            return "redirect:/ui/products";
        }
        model.addAttribute("result", result);
        return "products/import";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid ProductRequestDto dto, BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("product", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            addLookups(model);
            return "products/form";
        }
        try {
            ProductResponseDto saved = productService.saveProduct(dto);
            ra.addFlashAttribute("ok", "Producto creado.");
            return "redirect:/ui/products/" + saved.id();
        } catch (Exception e) {
            model.addAttribute("product", dto);
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", e.getMessage());
            addLookups(model);
            return "products/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("product", productService.getProductById(id));
            return "products/detail";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/products";
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) throws IOException {
        ProductImageService.ImageData image = productImageService.loadPrimary(id);
        if (image == null) {
            byte[] placeholder = ("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"640\" height=\"480\" viewBox=\"0 0 640 480\"><rect width=\"640\" height=\"480\" fill=\"#e5e7eb\"/><path d=\"M180 350l95-110 70 80 55-65 110 95H180z\" fill=\"#9ca3af\"/><circle cx=\"400\" cy=\"165\" r=\"38\" fill=\"#9ca3af\"/></svg>").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.ok().contentType(MediaType.valueOf("image/svg+xml")).body(placeholder);
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.contentType())).body(image.content());
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN')")
    public String uploadImage(@PathVariable Long id, @RequestPart("image") MultipartFile image,
                              RedirectAttributes ra) {
        try {
            productImageService.replace(id, image);
            ra.addFlashAttribute("ok", "Imagen actualizada.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/products/" + id;
    }

    @PostMapping("/{id}/image/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteImage(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productImageService.delete(id);
            ra.addFlashAttribute("ok", "Imagen eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/products/" + id;
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            ProductResponseDto p = productService.getProductById(id);
            model.addAttribute("product", toRequestDto(p));
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", id);
            addLookups(model);
            return "products/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/ui/products";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, @Valid ProductRequestDto dto,
                         BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("product", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", id);
            model.addAttribute("errorMessage", "Revisa los datos ingresados.");
            model.addAttribute("fieldErrors", binding.getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
            addLookups(model);
            return "products/form";
        }
        try {
            productService.updateProduct(id, dto);
            ra.addFlashAttribute("ok", "Producto actualizado.");
            return "redirect:/ui/products/" + id;
        } catch (Exception e) {
            model.addAttribute("product", dto);
            model.addAttribute("isEdit", true);
            model.addAttribute("productId", id);
            model.addAttribute("errorMessage", e.getMessage());
            addLookups(model);
            return "products/form";
        }
    }

    @PostMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStock(@PathVariable Long id, @RequestParam Integer stock, RedirectAttributes ra) {
        if (stock == null || stock < 0) {
            ra.addFlashAttribute("error", "El stock debe ser mayor o igual a 0.");
            return "redirect:/ui/products/" + id;
        }
        try {
            productService.updateStock(id, stock);
            ra.addFlashAttribute("ok", "Stock actualizado.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ui/products/" + id;
    }

    @GetMapping("/template.xlsx")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> templateXlsx() throws IOException {
        return fileResponse(productImportService.exportXlsx(true), "productos-plantilla.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/export.xlsx")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportXlsx() throws IOException {
        return fileResponse(productImportService.exportXlsx(false), "productos.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/template.csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> templateCsv() throws IOException {
        return fileResponse(productImportService.exportCsv(true), "productos-plantilla.csv", "text/csv;charset=UTF-8");
    }

    @GetMapping("/export.csv")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> exportCsv() throws IOException {
        return fileResponse(productImportService.exportCsv(false), "productos.csv", "text/csv;charset=UTF-8");
    }

    private ResponseEntity<ByteArrayResource> fileResponse(byte[] content, String filename, String mediaType) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(mediaType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(new ByteArrayResource(content));
    }

    private ProductRequestDto emptyProduct() {
        return ProductRequestDto.builder()
                .stock(0)
                .margin(BigDecimal.ZERO)
                .cost(BigDecimal.ZERO)
                .salePrice(BigDecimal.ZERO)
                .build();
    }

    private ProductRequestDto toRequestDto(ProductResponseDto p) {
        return ProductRequestDto.builder()
                .name(p.name())
                .internalCode(p.internalCode())
                .barCode(p.barCode())
                .providerCode(p.providerCode())
                .coinId(p.coinId())
                .ivaTypeId(p.ivaTypeId())
                .categoryId(p.categoryId())
                .brandId(p.brandId())
                .providerId(p.providerId())
                .cost(p.cost())
                .margin(p.margin())
                .salePrice(p.salePrice())
                .stock(p.stock())
                .minStock(p.minStock())
                .build();
    }

    private void addLookups(Model model) {
        model.addAttribute("coins", coinService.getAllCoins());
        model.addAttribute("ivaTypes", ivaTypeService.getAllIvaTypes());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());
        model.addAttribute("providers", providerService.getAllProviders());
    }

    private void addPriceLookups(Model model) {
        model.addAttribute("brands", brandService.getAllBrands());
    }
}
