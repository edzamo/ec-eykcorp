package com.eykcorp.clientes.infrastructure.adapter.in.web.exception;

/** Credenciales rechazadas: no distingue usuario de contraseña. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Credenciales inválidas");
    }
}
