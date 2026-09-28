package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.IvaType;

public interface IvaTypeRepository extends BaseRepository<IvaType, Long> {

    IvaType findByDescriptionIgnoreCase(String description);
}
