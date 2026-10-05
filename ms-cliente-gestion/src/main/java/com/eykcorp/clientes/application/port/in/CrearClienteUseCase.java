package com.eykcorp.clientes.application.port.in;

import com.eykcorp.clientes.domain.cliente.Cliente;
import reactor.core.publisher.Mono;

public interface CrearClienteUseCase {

    Mono<Cliente> crear(DatosCliente datos);
}
