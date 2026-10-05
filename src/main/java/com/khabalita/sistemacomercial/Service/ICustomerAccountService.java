package com.khabalita.sistemacomercial.Service;

import com.khabalita.sistemacomercial.Entities.Sale;
import com.khabalita.sistemacomercial.dto.request.CustomerAccountPaymentRequest;
import com.khabalita.sistemacomercial.dto.response.CustomerAccountResponse;

public interface ICustomerAccountService {

    CustomerAccountResponse getAccount(Long customerId);

    void registerSaleDebit(Sale sale);

    void registerPayment(Long customerId, CustomerAccountPaymentRequest request);
}
