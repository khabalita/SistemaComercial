package com.khabalita.sistemacomercial.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record ProviderRequestDto(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Size(max = 20, message = "El CUIT no puede superar los 20 caracteres")
        String taxId,

        @Size(max = 200, message = "La direccion no puede superar los 200 caracteres")
        String address,

        @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
        String phone,

        @Email(message = "El email debe tener formato valido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        String email,

        @Size(max = 150, message = "El sitio web no puede superar los 150 caracteres")
        String website)
{ }