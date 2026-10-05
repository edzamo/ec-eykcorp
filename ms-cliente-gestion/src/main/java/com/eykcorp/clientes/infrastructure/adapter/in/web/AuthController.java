package com.eykcorp.clientes.infrastructure.adapter.in.web;

import com.eykcorp.clientes.infrastructure.security.AutenticadorAdministrador;
import com.eykcorp.clientes.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Login del administrador (INV-14: sin caso de uso de negocio; vive en infraestructura). */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AutenticadorAdministrador autenticador;
    private final JwtService jwtService;

    public AuthController(AutenticadorAdministrador autenticador, JwtService jwtService) {
        this.autenticador = autenticador;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest peticion) {
        return autenticador.autenticar(peticion.usuario(), peticion.password())
                .flatMap(valido -> valido
                        ? Mono.fromSupplier(() -> jwtService.emitir(peticion.usuario()))
                                .map(t -> new LoginResponse(t.token(), t.expiraEnSegundos()))
                        : Mono.error(new CredencialesInvalidasException()));
    }
}
