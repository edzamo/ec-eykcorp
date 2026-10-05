package com.eykcorp.clientes.application.port.in;

import reactor.core.publisher.Mono;

public interface EliminarClienteUseCase {

    Mono<Void> eliminar(Long id);
}
