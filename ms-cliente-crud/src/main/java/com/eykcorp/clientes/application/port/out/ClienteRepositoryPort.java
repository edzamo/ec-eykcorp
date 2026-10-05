package com.eykcorp.clientes.application.port.out;

import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.domain.cliente.Correo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClienteRepositoryPort {

    /** Inserta si el cliente no tiene id; actualiza en caso contrario. Devuelve el cliente con id. */
    Mono<Cliente> guardar(Cliente cliente);

    Mono<Cliente> buscarPorId(Long id);

    Flux<Cliente> buscarTodos();

    Mono<Void> eliminarPorId(Long id);

    Mono<Boolean> existePorCorreo(Correo correo);

    Mono<Boolean> existePorCorreoDeOtro(Correo correo, Long id);
}
