package com.eykcorp.fixture.violacion.bloqueo.application;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ServicioQueBloquea {
    String monoBlock(Mono<String> m) {
        return m.block();
    }

    Object monoBlockOptional(Mono<String> m) {
        return m.blockOptional();
    }

    String fluxBlockFirst(Flux<String> f) {
        return f.blockFirst();
    }

    String fluxBlockLast(Flux<String> f) {
        return f.blockLast();
    }

    Object fluxToIterable(Flux<String> f) {
        return f.toIterable();
    }

    Object fluxToStream(Flux<String> f) {
        return f.toStream();
    }
}
