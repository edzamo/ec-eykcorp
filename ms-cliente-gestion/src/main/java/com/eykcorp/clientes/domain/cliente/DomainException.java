package com.eykcorp.clientes.domain.cliente;

/** Raíz de las excepciones de negocio; se traducen en un único manejador de entrada. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensaje) {
        super(mensaje);
    }
}
