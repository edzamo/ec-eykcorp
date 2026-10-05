package com.eykcorp.clientes.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String usuario, @NotBlank String password) {

    /** Nunca incluye la contraseña. */
    @Override
    public String toString() {
        return "LoginRequest[usuario=" + usuario + "]";
    }
}
