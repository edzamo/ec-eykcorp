package com.eykcorp.clientes.infrastructure.adapter.in.web.controller;

import com.eykcorp.clientes.infrastructure.adapter.in.web.dto.request.LoginRequest;
import com.eykcorp.clientes.infrastructure.adapter.in.web.dto.response.LoginResponse;
import com.eykcorp.clientes.infrastructure.adapter.in.web.exception.CredencialesInvalidasException;

import com.eykcorp.clientes.infrastructure.security.AutenticadorAdministrador;
import com.eykcorp.clientes.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Login del administrador (INV-14: sin caso de uso de negocio; vive en infraestructura). */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AutenticadorAdministrador autenticador;
    private final JwtService jwtService;

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest peticion) {
        return autenticador.autenticar(peticion.usuario(), peticion.password())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(CredencialesInvalidasException::new))
                .doOnError(CredencialesInvalidasException.class, e -> log.warn("Login fallido"))
                .map(valido -> jwtService.emitir(peticion.usuario()))
                .doOnNext(t -> log.info("Login exitoso"))
                .map(t -> new LoginResponse(t.token(), t.expiraEnSegundos()));
    }
}
