package com.khabalita.sistemacomercial.Repositories;

import com.khabalita.sistemacomercial.Entities.AppUser;

import java.util.Optional;

public interface AppUserRepository extends BaseRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);
}
