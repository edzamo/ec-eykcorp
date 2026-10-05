package com.eykcorp.clientes.infrastructure.adapter.in.web.dto.request;

import com.eykcorp.clientes.domain.cliente.Telefono;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Email @Size(max = 254) String correo,
        @Pattern(regexp = Telefono.PATRON, message = "debe tener entre 7 y 15 dígitos, con + inicial opcional")
                String telefono) {

    /** INV-17: sin datos personales. */
    @Override
    public String toString() {
        return "ClienteRequest[***]";
    }
}
