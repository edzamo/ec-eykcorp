package com.eykcorp.fixture.permitido.application.port.out;

import reactor.core.publisher.Mono;

public interface PuertoPermitido {
    Mono<Void> uno();

    Mono<Void> dos();
}
