package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

interface ClienteR2dbcRepository extends ReactiveCrudRepository<ClienteEntity, Long> {

    Mono<Boolean> existsByCorreo(String correo);

    Mono<Boolean> existsByCorreoAndIdNot(String correo, Long id);
}
