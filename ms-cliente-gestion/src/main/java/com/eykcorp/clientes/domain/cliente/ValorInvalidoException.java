package com.eykcorp.clientes.domain.cliente;

/** Un valor no cumple una invariante del dominio (se traduce en 400). */
public class ValorInvalidoException extends DomainException {

    public ValorInvalidoException(String mensaje) {
        super(mensaje);
    }
}
