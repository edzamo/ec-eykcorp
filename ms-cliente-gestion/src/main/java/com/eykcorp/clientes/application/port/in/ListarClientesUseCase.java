package com.eykcorp.clientes.application.port.in;

import com.eykcorp.clientes.domain.cliente.Cliente;
import reactor.core.publisher.Flux;

public interface ListarClientesUseCase {

    Flux<Cliente> listar();
}
