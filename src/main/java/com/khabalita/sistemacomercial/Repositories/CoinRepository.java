package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.Coin;

public interface CoinRepository extends BaseRepository<Coin, Long> {

    Coin findByCodeIgnoreCase(String code);
}
