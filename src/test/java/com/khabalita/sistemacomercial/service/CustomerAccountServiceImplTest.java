package com.khabalita.sistemacomercial.service;

import com.khabalita.sistemacomercial.Entities.Customer;
import com.khabalita.sistemacomercial.Entities.CustomerAccountMovement;
import com.khabalita.sistemacomercial.Entities.CustomerAccountMovementType;
import com.khabalita.sistemacomercial.Entities.Sale;
import com.khabalita.sistemacomercial.Repositories.CustomerAccountMovementRepository;
import com.khabalita.sistemacomercial.Repositories.CustomerRepository;
import com.khabalita.sistemacomercial.Service.Impl.CustomerAccountServiceImpl;
import com.khabalita.sistemacomercial.dto.request.CustomerAccountPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerAccountServiceImplTest {

    @Mock CustomerAccountMovementRepository movementRepository;
    @Mock CustomerRepository customerRepository;

    @InjectMocks CustomerAccountServiceImpl service;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().name("Cliente test").build();
        customer.setId(1L);
        lenient().when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    }

    @Test
    void paymentGreaterThanBalanceIsRejected() {
        when(movementRepository.balanceByCustomerId(1L)).thenReturn(new BigDecimal("3500.00"));

        assertThrows(IllegalArgumentException.class, () -> service.registerPayment(1L,
                new CustomerAccountPaymentRequest(new BigDecimal("3500.01"), "Pago excedente")));
        verify(movementRepository, never()).save(any(CustomerAccountMovement.class));
    }

    @Test
    void partialPaymentCreatesCreditMovement() {
        when(movementRepository.balanceByCustomerId(1L)).thenReturn(new BigDecimal("10000.00"));

        service.registerPayment(1L,
                new CustomerAccountPaymentRequest(new BigDecimal("3500"), "Pago de deuda"));

        ArgumentCaptor<CustomerAccountMovement> captor = ArgumentCaptor.forClass(CustomerAccountMovement.class);
        verify(movementRepository).save(captor.capture());
        assertEquals(CustomerAccountMovementType.CREDIT, captor.getValue().getType());
        assertEquals(new BigDecimal("3500.00"), captor.getValue().getAmount());
        assertEquals("Pago de deuda", captor.getValue().getConcept());
    }

    @Test
    void saleCreatesDebitMovement() {
        Sale sale = Sale.builder()
                .customer(customer)
                .number("V-00001")
                .date(LocalDateTime.now())
                .total(new BigDecimal("1200.00"))
                .build();

        service.registerSaleDebit(sale);

        ArgumentCaptor<CustomerAccountMovement> captor = ArgumentCaptor.forClass(CustomerAccountMovement.class);
        verify(movementRepository).save(captor.capture());
        assertEquals(CustomerAccountMovementType.DEBIT, captor.getValue().getType());
        assertEquals(new BigDecimal("1200.00"), captor.getValue().getAmount());
        assertEquals("Venta V-00001", captor.getValue().getConcept());
    }
}
