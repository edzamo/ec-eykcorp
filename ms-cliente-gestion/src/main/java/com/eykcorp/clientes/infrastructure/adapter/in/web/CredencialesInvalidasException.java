package com.eykcorp.clientes.infrastructure.adapter.in.web;

/** Credenciales rechazadas: no distingue usuario de contraseña. */
class CredencialesInvalidasException extends RuntimeException {

    CredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}
