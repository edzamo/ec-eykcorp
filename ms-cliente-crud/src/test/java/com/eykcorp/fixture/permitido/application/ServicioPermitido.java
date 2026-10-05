package com.eykcorp.fixture.permitido.application;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ServicioPermitido {
    Mono<String> resultado = Mono.just("ok");
}
