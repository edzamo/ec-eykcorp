package com.eykcorp.clientes.application.port.in;

import com.eykcorp.clientes.domain.cliente.Cliente;
import reactor.core.publisher.Mono;

public interface ObtenerClienteUseCase {

    Mono<Cliente> obtener(Long id);
}
