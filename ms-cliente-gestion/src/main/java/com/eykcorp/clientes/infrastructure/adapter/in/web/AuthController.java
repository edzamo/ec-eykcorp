package com.eykcorp.clientes.infrastructure.adapter.in.web;

import com.eykcorp.clientes.infrastructure.security.AutenticadorAdministrador;
import com.eykcorp.clientes.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Login del administrador (INV-14: sin caso de uso de negocio; vive en infraestructura). */
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
                .map(valido -> jwtService.emitir(peticion.usuario()))
                .map(t -> new LoginResponse(t.token(), t.expiraEnSegundos()));
    }
}
