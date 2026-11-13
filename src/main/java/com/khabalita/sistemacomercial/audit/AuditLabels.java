package com.khabalita.sistemacomercial.audit;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component("auditLabels")
public class AuditLabels {

    private static final Map<String, String> ACTIONS = Map.ofEntries(
            Map.entry("PRODUCT_CREATE", "Creó un producto"),
            Map.entry("PRODUCT_UPDATE", "Actualizó un producto"),
            Map.entry("PRODUCT_DELETE", "Eliminó un producto"),
            Map.entry("PRODUCT_IMPORT", "Importó productos"),
            Map.entry("PRODUCT_PRICE_BULK_UPDATE", "Actualizó precios"),
            Map.entry("STOCK_UPDATE", "Actualizó stock"),
            Map.entry("PRODUCT_EXPORT_XLSX", "Exportó productos a Excel"),
            Map.entry("PRODUCT_EXPORT_CSV", "Exportó productos a CSV"),
            Map.entry("SALE_CREATE", "Registró una venta"),
            Map.entry("SALE_CANCEL", "Anuló una venta"),
            Map.entry("USER_CREATE", "Creó un usuario"),
            Map.entry("USER_DELETE", "Eliminó un usuario"),
            Map.entry("ACCESS_DENIED", "Intentó acceder sin permisos"),
            Map.entry("LOGIN_SUCCESS", "Inició sesión"),
            Map.entry("LOGIN_FAILURE", "Falló el inicio de sesión"),
            Map.entry("USD_RATE_UPDATE", "Actualizó la cotización del dólar"),
            Map.entry("BUSINESS_LOGO_UPDATE", "Actualizó el logo del negocio")
    );

    private static final Map<String, String> ENTITIES = Map.ofEntries(
            Map.entry("ProductServiceImpl", "producto"),
            Map.entry("ProductImportService", "productos"),
            Map.entry("SaleServiceImpl", "venta"),
            Map.entry("CoinServiceImpl", "moneda"),
            Map.entry("AppUser", "usuario"),
            Map.entry("UserViewController", "usuario"),
            Map.entry("ProductExport", "productos")
    );

    public String action(String value) {
        return ACTIONS.getOrDefault(value, value == null ? "Realizó una acción" : value);
    }

    public String entity(String value, String id) {
        String label = ENTITIES.getOrDefault(value, value == null ? "registro" : value);
        return id == null || id.isBlank() ? label : label + " #" + id;
    }

    public String outcome(String value) {
        return "FAILURE".equalsIgnoreCase(value) ? "No se pudo completar" : "Completado";
    }
}
