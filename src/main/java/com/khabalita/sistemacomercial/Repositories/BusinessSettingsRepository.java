package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.BusinessSettings;

import java.util.Optional;

public interface BusinessSettingsRepository extends BaseRepository<BusinessSettings, Long> {

    Optional<BusinessSettings> findFirstByOrderByIdAsc();
}
