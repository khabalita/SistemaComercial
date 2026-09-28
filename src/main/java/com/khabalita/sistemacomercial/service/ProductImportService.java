package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Brand;
import com.khabalita.sistemacomercial.Entities.Category;
import com.khabalita.sistemacomercial.Entities.Coin;
import com.khabalita.sistemacomercial.Entities.Product;
import com.khabalita.sistemacomercial.Entities.Provider;
import com.khabalita.sistemacomercial.Repositories.BrandRepository;
import com.khabalita.sistemacomercial.Repositories.CategoryRepository;
import com.khabalita.sistemacomercial.Repositories.CoinRepository;
import com.khabalita.sistemacomercial.Repositories.IvaTypeRepository;
import com.khabalita.sistemacomercial.Repositories.ProductRepository;
import com.khabalita.sistemacomercial.Repositories.ProviderRepository;
import com.khabalita.sistemacomercial.audit.Audited;
import com.khabalita.sistemacomercial.dto.response.ProductImportResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImportService {

    public static final List<String> HEADERS = List.of(
            "nombre", "cod_interno", "cod_barras", "cod_proveedor", "costo", "precio_venta",
            "stock", "stock_min", "categoría", "marca", "proveedor");
    private static final BigDecimal DEFAULT_MARGIN = BigDecimal.ZERO;
    private static final String DEFAULT_IVA = "Exento";
    private static final int MAX_ROWS = 10_000;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final ProductRepository productRepository;
    private final CoinRepository coinRepository;
    private final IvaTypeRepository ivaTypeRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProviderRepository providerRepository;

    public ProductImportResult validate(MultipartFile file) {
        try {
            List<ProductImportRow> rows = readRows(file);
            List<String> errors = validateRows(rows);
            return result(rows.size(), errors);
        } catch (IllegalArgumentException | IOException e) {
            return new ProductImportResult(false, 0, 0, List.of(e.getMessage()));
        }
    }

    @Transactional
    @Audited("PRODUCT_IMPORT")
    public ProductImportResult importProducts(MultipartFile file) {
        try {
            List<ProductImportRow> rows = readRows(file);
            List<String> errors = validateRows(rows);
            if (!errors.isEmpty()) return result(rows.size(), errors);

            List<Product> products = rows.stream().map(this::toEntity).toList();
            productRepository.saveAll(products);
            return new ProductImportResult(true, rows.size(), products.size(), List.of());
        } catch (IllegalArgumentException | IOException e) {
            return new ProductImportResult(false, 0, 0, List.of(e.getMessage()));
        }
    }

    @Transactional(readOnly = true)
    @Audited("PRODUCT_EXPORT_XLSX")
    public byte[] exportXlsx(boolean template) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("products");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) header.createCell(i).setCellValue(HEADERS.get(i));
            if (!template) {
                int rowNumber = 1;
                for (Product product : productRepository.findAll()) {
                    Row row = sheet.createRow(rowNumber++);
                    List<String> values = productValues(product);
                    for (int i = 0; i < values.size(); i++) row.createCell(i).setCellValue(values.get(i));
                }
            }
            for (int i = 0; i < HEADERS.size(); i++) sheet.autoSizeColumn(i);
            workbook.write(output);
            return output.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    @Audited("PRODUCT_EXPORT_CSV")
    public byte[] exportCsv(boolean template) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord(HEADERS);
            if (!template) for (Product product : productRepository.findAll()) printer.printRecord(productValues(product));
        }
        return output.toByteArray();
    }

    private List<String> productValues(Product product) {
        return List.of(safe(product.getName()), safe(product.getInternalCode()), safe(product.getBarCode()),
                safe(product.getProviderCode()), product.getCost().toPlainString(),
                product.getSalePrice() == null ? product.getCost().toPlainString() : product.getSalePrice().toPlainString(),
                String.valueOf(product.getStock()),
                product.getMinStock() == null ? "" : String.valueOf(product.getMinStock()),
                product.getCategory() == null ? "" : safe(product.getCategory().getName()),
                product.getBrand() == null ? "" : safe(product.getBrand().getName()),
                product.getProvider() == null ? "" : safe(product.getProvider().getName()));
    }

    private String safe(String value) {
        if (value == null) return "";
        String normalized = value.trim();
        return normalized.startsWith("=") || normalized.startsWith("+") || normalized.startsWith("-") || normalized.startsWith("@")
                ? "'" + normalized : normalized;
    }

    private ProductImportResult result(int totalRows, List<String> errors) {
        return new ProductImportResult(errors.isEmpty(), totalRows,
                errors.isEmpty() ? totalRows : 0, List.copyOf(errors));
    }

    private List<ProductImportRow> readRows(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("El archivo esta vacio");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("El archivo no puede superar 10 MB");
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        if (filename.endsWith(".xlsx")) return readXlsx(file);
        if (filename.endsWith(".csv")) return readCsv(file);
        throw new IllegalArgumentException("Formato no soportado. Use .xlsx o .csv");
    }

    private List<ProductImportRow> readCsv(MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String firstLine = content.lines().findFirst().orElse("");
        char delimiter = firstLine.contains(";") && !firstLine.contains(",") ? ';' : ',';
        try (Reader reader = new InputStreamReader(
                new java.io.ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.builder()
                     .setDelimiter(delimiter).setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).build())) {
            validateHeaders(parser.getHeaderNames());
            List<ProductImportRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (rows.size() >= MAX_ROWS) throw new IllegalArgumentException("El archivo no puede superar 10.000 filas");
                rows.add(row(record.getRecordNumber() + 1, HEADERS.stream()
                        .collect(Collectors.toMap(Function.identity(), h -> value(record.get(h))))));
            }
            return rows;
        }
    }

    private List<ProductImportRow> readXlsx(MultipartFile file) throws IOException {
        try (InputStream input = file.getInputStream(); Workbook workbook = new XSSFWorkbook(input)) {
            if (workbook.getNumberOfSheets() == 0) throw new IllegalArgumentException("El Excel no tiene hojas");
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null) throw new IllegalArgumentException("Falta la fila de encabezados");
            DataFormatter formatter = new DataFormatter();
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < HEADERS.size(); i++) headers.add(cellValue(header.getCell(i), formatter));
            validateHeaders(headers);

            List<ProductImportRow> rows = new ArrayList<>();
            for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                Row current = sheet.getRow(index);
                if (current == null || isBlank(current, formatter)) continue;
                if (rows.size() >= MAX_ROWS) throw new IllegalArgumentException("El archivo no puede superar 10.000 filas");
                Map<String, String> values = HEADERS.stream().collect(Collectors.toMap(
                        Function.identity(), h -> cellValue(current.getCell(HEADERS.indexOf(h)), formatter)));
                rows.add(row(index + 1, values));
            }
            return rows;
        }
    }

    private ProductImportRow row(long rowNumber, Map<String, String> values) {
        return new ProductImportRow((int) rowNumber, values.get("nombre"), values.get("cod_interno"),
                values.get("cod_barras"), values.get("cod_proveedor"),
                decimal(values.get("costo"), (int) rowNumber, "costo"),
                decimal(values.get("precio_venta"), (int) rowNumber, "precio_venta"),
                integer(values.get("stock"), (int) rowNumber, "stock"), integer(values.get("stock_min"), (int) rowNumber, "stock_min"),
                values.get("categoría"), values.get("marca"), values.get("proveedor"));
    }

    private List<String> validateRows(List<ProductImportRow> rows) {
        List<String> errors = new ArrayList<>();
        Set<String> internalCodes = new HashSet<>();
        for (ProductImportRow row : rows) {
            String prefix = "Fila " + row.rowNumber() + ": ";
            required(row.name(), prefix, "nombre", errors);
            required(row.internalCode(), prefix, "cod_interno", errors);
            if (row.cost() == null || row.cost().signum() <= 0) errors.add(prefix + "costo debe ser mayor a 0");
            if (row.salePrice() != null && row.salePrice().signum() <= 0) errors.add(prefix + "precio_venta debe ser mayor a 0");
            if (row.stock() == null || row.stock() < 0) errors.add(prefix + "stock debe ser mayor o igual a 0");
            if (row.minStock() != null && row.minStock() < 0) errors.add(prefix + "stock_min no puede ser negativo");
            if (row.internalCode() != null && !internalCodes.add(row.internalCode().toLowerCase(Locale.ROOT)))
                errors.add(prefix + "cod_interno repetido dentro del archivo");
            if (row.internalCode() != null && productRepository.findByInternalCodeIgnoreCase(row.internalCode()).isPresent())
                errors.add(prefix + "cod_interno ya existe: " + row.internalCode());
            resolveRelations(row, prefix, errors);
        }
        return errors;
    }

    private void resolveRelations(ProductImportRow row, String prefix, List<String> errors) {
        if (coinRepository.findByCodeIgnoreCase("ARS") == null)
            errors.add(prefix + "la moneda ARS no esta configurada");
        if (ivaTypeRepository.findByDescriptionIgnoreCase(DEFAULT_IVA) == null)
            errors.add(prefix + "iva por defecto no existe: " + DEFAULT_IVA);
        if (!blank(row.categoryName()) && categoryRepository.findByNameIgnoreCase(row.categoryName()) == null)
            errors.add(prefix + "categoria no existe: " + row.categoryName());
        if (!blank(row.brandName()) && brandRepository.findByNameIgnoreCase(row.brandName()) == null)
            errors.add(prefix + "marca no existe: " + row.brandName());
        if (!blank(row.providerName()) && provider(row) == null)
            errors.add(prefix + "proveedor no existe: " + row.providerName());
    }

    private Product toEntity(ProductImportRow row) {
        Product product = Product.builder().name(row.name()).internalCode(row.internalCode())
                .barCode(row.barCode()).providerCode(row.providerCode()).cost(row.cost()).margin(DEFAULT_MARGIN)
                .salePrice(row.salePrice() == null ? row.cost() : row.salePrice())
                .stock(row.stock()).minStock(row.minStock())
                .coin(coinRepository.findByCodeIgnoreCase("ARS"))
                .ivaType(ivaTypeRepository.findByDescriptionIgnoreCase(DEFAULT_IVA))
                .category(blank(row.categoryName()) ? null : categoryRepository.findByNameIgnoreCase(row.categoryName()))
                .brand(blank(row.brandName()) ? null : brandRepository.findByNameIgnoreCase(row.brandName()))
                .provider(provider(row)).build();
        return product;
    }

    private Provider provider(ProductImportRow row) {
        return blank(row.providerName()) ? null : providerRepository.findByNameIgnoreCase(row.providerName());
    }

    private void validateHeaders(List<String> headers) {
        if (headers.size() != HEADERS.size())
            throw new IllegalArgumentException("La cantidad de columnas no coincide con la plantilla");
        List<String> normalized = headers.stream().map(this::normalize).toList();
        List<String> expected = HEADERS.stream().map(this::normalize).toList();
        if (!normalized.equals(expected)) throw new IllegalArgumentException("Los encabezados deben ser: " + String.join(",", HEADERS));
    }

    private String cellValue(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.FORMULA) throw new IllegalArgumentException("No se permiten formulas en el Excel");
        return normalize(formatter.formatCellValue(cell));
    }

    private boolean isBlank(Row row, DataFormatter formatter) {
        for (int i = 0; i < HEADERS.size(); i++) if (!cellValue(row.getCell(i), formatter).isBlank()) return false;
        return true;
    }

    private String value(String value) {
        String normalized = normalize(value);
        if (normalized.startsWith("=") || normalized.startsWith("@"))
            throw new IllegalArgumentException("No se permiten formulas en el CSV");
        return normalized;
    }

    private BigDecimal decimal(String value, int row, String field) {
        if (blank(value)) return null;
        try { return new BigDecimal(value.replace(",", ".")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Fila " + row + ": " + field + " no es numerico"); }
    }

    private Integer integer(String value, int row, String field) {
        if (blank(value)) return null;
        try { return new BigDecimal(value.replace(",", ".")).intValueExact(); }
        catch (ArithmeticException | NumberFormatException e) { throw new IllegalArgumentException("Fila " + row + ": " + field + " debe ser entero"); }
    }

    private void required(String value, String prefix, String field, List<String> errors) {
        if (blank(value)) errors.add(prefix + field + " es obligatorio");
    }

    private void required(String first, String second, String prefix, String field, List<String> errors) {
        if (blank(first) && blank(second)) errors.add(prefix + field + " es obligatorio");
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    private String normalize(String value) {
        return value == null ? "" : value.replace("\uFEFF", "").trim();
    }
}
