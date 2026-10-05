package com.eykcorp.clientes.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Los límites de longitud evitan ejecutar BCrypt (coste) con entradas desmesuradas (SEC-002). */
public record LoginRequest(@NotBlank @Size(max = 128) String usuario, @NotBlank @Size(max = 128) String password) {

    /** Nunca incluye la contraseña. */
    @Override
    public String toString() {
        return "LoginRequest[usuario=" + usuario + "]";
    }
}
