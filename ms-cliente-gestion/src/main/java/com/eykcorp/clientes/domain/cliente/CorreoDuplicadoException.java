package com.eykcorp.clientes.domain.cliente;

/** El mensaje no incluye el correo: es dato personal (INV-17). */
public class CorreoDuplicadoException extends DomainException {

    public CorreoDuplicadoException() {
        super("Ya existe un cliente con el correo indicado");
    }
}
