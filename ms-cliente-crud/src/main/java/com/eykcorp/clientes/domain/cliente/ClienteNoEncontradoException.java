package com.eykcorp.clientes.domain.cliente;

public class ClienteNoEncontradoException extends DomainException {

    public ClienteNoEncontradoException(Long id) {
        super("No existe un cliente con id " + id);
    }
}
