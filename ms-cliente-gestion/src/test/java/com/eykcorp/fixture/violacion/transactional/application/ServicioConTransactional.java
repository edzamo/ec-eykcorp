package com.eykcorp.fixture.violacion.transactional.application;

import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/** Canario CR-005: EXC-2 prohíbe @Transactional en application. */
public class ServicioConTransactional {

    @Transactional
    Mono<String> escribir() {
        return Mono.just("ok");
    }
}
