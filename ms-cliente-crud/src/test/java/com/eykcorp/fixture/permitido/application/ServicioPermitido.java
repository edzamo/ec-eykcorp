package com.eykcorp.fixture.permitido.application;

import com.eykcorp.fixture.permitido.domain.ModeloPermitido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class ServicioPermitido {
    Mono<String> resultado = Mono.just("ok");

    /** Canario CR-006: Mono.zip usa reactor.util.function.Tuple2. */
    Mono<String> combinar() {
        return Mono.zip(Mono.just("a"), Mono.just("b")).map(t -> t.getT1() + t.getT2());
    }

    /** Canario: @Transactional está permitido en application. */
    @Transactional
    Mono<ModeloPermitido> escribir() {
        return Mono.just(new ModeloPermitido());
    }
}
