package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.SaleSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SaleSequenceRepository extends BaseRepository<SaleSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SaleSequence s where s.id = :id")
    Optional<SaleSequence> findByIdForUpdate(@Param("id") Long id);
}
