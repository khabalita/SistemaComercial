package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.CustomerAccountMovement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerAccountMovementRepository extends BaseRepository<CustomerAccountMovement, Long> {

    List<CustomerAccountMovement> findByCustomerIdOrderByMovementDateDescIdDesc(Long customerId);

    @Query("select coalesce(sum(case when m.type = com.khabalita.sistemacomercial.Entities.CustomerAccountMovementType.DEBIT then m.amount else 0 end), 0) "
            + "- coalesce(sum(case when m.type = com.khabalita.sistemacomercial.Entities.CustomerAccountMovementType.CREDIT then m.amount else 0 end), 0) "
            + "from CustomerAccountMovement m where m.customer.id = :customerId")
    BigDecimal balanceByCustomerId(@Param("customerId") Long customerId);
}
