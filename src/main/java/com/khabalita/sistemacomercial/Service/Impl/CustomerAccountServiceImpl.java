package com.khabalita.sistemacomercial.Service.Impl;

import com.khabalita.sistemacomercial.Entities.Customer;
import com.khabalita.sistemacomercial.Entities.CustomerAccountMovement;
import com.khabalita.sistemacomercial.Entities.CustomerAccountMovementType;
import com.khabalita.sistemacomercial.Entities.Sale;
import com.khabalita.sistemacomercial.Repositories.CustomerAccountMovementRepository;
import com.khabalita.sistemacomercial.Repositories.CustomerRepository;
import com.khabalita.sistemacomercial.Service.ICustomerAccountService;
import com.khabalita.sistemacomercial.audit.Audited;
import com.khabalita.sistemacomercial.dto.request.CustomerAccountPaymentRequest;
import com.khabalita.sistemacomercial.dto.response.CustomerAccountMovementResponse;
import com.khabalita.sistemacomercial.dto.response.CustomerAccountResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerAccountServiceImpl implements ICustomerAccountService {

    private final CustomerAccountMovementRepository movementRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerAccountResponse getAccount(Long customerId) {
        Customer customer = findCustomer(customerId);
        BigDecimal balance = normalizedBalance(movementRepository.balanceByCustomerId(customerId));
        var movements = movementRepository.findByCustomerIdOrderByMovementDateDescIdDesc(customerId).stream()
                .map(movement -> new CustomerAccountMovementResponse(
                        movement.getId(), movement.getMovementDate(), movement.getType(), movement.getAmount(),
                        movement.getConcept(), movement.getSale() == null ? null : movement.getSale().getId(),
                        movement.getSale() == null ? null : movement.getSale().getNumber(), movement.getCreatedBy()))
                .toList();
        return new CustomerAccountResponse(customer.getId(), customer.getName(), balance, movements);
    }

    @Override
    @Transactional
    public void registerSaleDebit(Sale sale) {
        if (sale.getCustomer() == null) {
            throw new IllegalArgumentException("La venta a cuenta corriente requiere un cliente");
        }
        movementRepository.save(CustomerAccountMovement.builder()
                .customer(sale.getCustomer())
                .sale(sale)
                .movementDate(sale.getDate())
                .type(CustomerAccountMovementType.DEBIT)
                .amount(sale.getTotal().setScale(2, RoundingMode.HALF_UP))
                .concept("Venta " + sale.getNumber())
                .createdBy(currentUsername())
                .build());
    }

    @Override
    @Transactional
    @Audited("CUSTOMER_ACCOUNT_PAYMENT")
    public void registerPayment(Long customerId, CustomerAccountPaymentRequest request) {
        Customer customer = findCustomer(customerId);
        BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal balance = normalizedBalance(movementRepository.balanceByCustomerId(customerId));
        if (amount.compareTo(balance) > 0) {
            throw new IllegalArgumentException("El pago no puede superar el saldo pendiente de " + balance);
        }
        movementRepository.save(CustomerAccountMovement.builder()
                .customer(customer)
                .movementDate(LocalDateTime.now())
                .type(CustomerAccountMovementType.CREDIT)
                .amount(amount)
                .concept(request.concept().trim())
                .createdBy(currentUsername())
                .build());
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + customerId));
    }

    private BigDecimal normalizedBalance(BigDecimal balance) {
        return (balance == null ? BigDecimal.ZERO : balance).setScale(2, RoundingMode.HALF_UP);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "sistema" : authentication.getName();
    }
}
