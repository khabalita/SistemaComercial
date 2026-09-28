package com.khabalita.sistemacomercial.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CustomerRequestDto(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        @Size(max = 20, message = "El CUIT/DNI no puede superar los 20 caracteres")
        String taxId,

        @Size(max = 200, message = "La direccion no puede superar los 200 caracteres")
        String address,

        @Size(max = 100, message = "La localidad no puede superar los 100 caracteres")
        String city,

        @Size(max = 100, message = "La provincia no puede superar los 100 caracteres")
        String province,

        @Size(max = 20, message = "El codigo postal no puede superar los 20 caracteres")
        String postalCode,

        @Size(max = 20, message = "El telefono no puede superar los 20 caracteres")
        String phone,

        @Email(message = "El email debe tener formato valido")
        @Size(max = 100, message = "El email no puede superar los 100 caracteres")
        String email,

        String notes,

        Boolean active)
{ }
