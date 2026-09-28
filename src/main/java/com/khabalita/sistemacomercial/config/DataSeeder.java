package com.khabalita.sistemacomercial.config;

import com.khabalita.sistemacomercial.Entities.Brand;
import com.khabalita.sistemacomercial.Entities.Category;
import com.khabalita.sistemacomercial.Entities.Coin;
import com.khabalita.sistemacomercial.Entities.Customer;
import com.khabalita.sistemacomercial.Entities.IvaType;
import com.khabalita.sistemacomercial.Entities.SaleSequence;
import com.khabalita.sistemacomercial.Repositories.BrandRepository;
import com.khabalita.sistemacomercial.Repositories.CategoryRepository;
import com.khabalita.sistemacomercial.Repositories.CoinRepository;
import com.khabalita.sistemacomercial.Repositories.CustomerRepository;
import com.khabalita.sistemacomercial.Repositories.IvaTypeRepository;
import com.khabalita.sistemacomercial.Repositories.SaleSequenceRepository;
import com.khabalita.sistemacomercial.Repositories.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CoinRepository coinRepository;
    private final IvaTypeRepository ivaTypeRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final CustomerRepository customerRepository;
    private final SaleSequenceRepository saleSequenceRepository;
    private final SaleRepository saleRepository;

    @Override
    public void run(String... args) {
        seedCoins();
        seedIvaTypes();
        seedCategories();
        seedBrands();
        seedCustomers();
        seedSaleSequence();
    }

    private void seedCoins() {
        List<Coin> defaults = List.of(
                Coin.builder().code("ARS").name("Peso Argentino").symbol("$").presentValue(BigDecimal.ONE).build(),
                Coin.builder().code("USD").name("Dolar Estadounidense").symbol("US$").presentValue(BigDecimal.valueOf(1000)).build()
        );
        defaults.forEach(c -> {
            if (coinRepository.findByCodeIgnoreCase(c.getCode()) == null) {
                coinRepository.save(c);
                log.info("Seed: moneda {} creada", c.getCode());
            }
        });
    }

    private void seedIvaTypes() {
        List<IvaType> defaults = List.of(
                IvaType.builder().description("IVA 21%").percentage(new BigDecimal("21.00")).build(),
                IvaType.builder().description("IVA 10.5%").percentage(new BigDecimal("10.50")).build(),
                IvaType.builder().description("IVA 27%").percentage(new BigDecimal("27.00")).build(),
                IvaType.builder().description("Exento").percentage(BigDecimal.ZERO).build()
        );
        defaults.forEach(i -> {
            if (ivaTypeRepository.findByDescriptionIgnoreCase(i.getDescription()) == null) {
                ivaTypeRepository.save(i);
                log.info("Seed: tipo de IVA {} creado", i.getDescription());
            }
        });
    }

    private void seedCategories() {
        List<String> defaults = List.of("General");
        defaults.forEach(name -> {
            if (categoryRepository.findByNameIgnoreCase(name) == null) {
                categoryRepository.save(Category.builder().name(name).build());
                log.info("Seed: categoria {} creada", name);
            }
        });
    }

    private void seedBrands() {
        List<String> defaults = List.of("Sin marca");
        defaults.forEach(name -> {
            if (brandRepository.findByNameIgnoreCase(name) == null) {
                brandRepository.save(Brand.builder().name(name).build());
                log.info("Seed: marca {} creada", name);
            }
        });
    }

    private void seedCustomers() {
        if (customerRepository.findByTaxId("00-00000000-0").isEmpty()) {
            customerRepository.save(Customer.builder()
                    .name("Consumidor Final")
                    .taxId("00-00000000-0")
                    .active(true)
                    .build());
            log.info("Seed: cliente Consumidor Final creado");
        }
    }

    private void seedSaleSequence() {
        if (saleSequenceRepository.findById(1L).isEmpty()) {
            long nextNumber = saleRepository.findFirstByOrderByIdDesc()
                    .map(sale -> parseSaleNumber(sale.getNumber()) + 1)
                    .orElse(1L);
            SaleSequence sequence = SaleSequence.builder().nextNumber(nextNumber).build();
            saleSequenceRepository.save(sequence);
            log.info("Seed: secuencia de ventas creada desde el numero {}", nextNumber);
        }
    }

    private long parseSaleNumber(String number) {
        try {
            return Long.parseLong(number.replace("V-", ""));
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }
}
