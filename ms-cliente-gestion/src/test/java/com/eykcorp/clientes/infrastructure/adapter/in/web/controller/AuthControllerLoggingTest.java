package com.eykcorp.clientes.infrastructure.adapter.in.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.eykcorp.clientes.CapturaLogs;
import com.eykcorp.clientes.infrastructure.adapter.in.web.dto.request.LoginRequest;
import com.eykcorp.clientes.infrastructure.adapter.in.web.exception.CredencialesInvalidasException;
import com.eykcorp.clientes.infrastructure.security.AutenticadorAdministrador;
import com.eykcorp.clientes.infrastructure.security.JwtService;
import com.eykcorp.clientes.infrastructure.security.TokenEmitido;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthControllerLoggingTest {

    private final AutenticadorAdministrador autenticador = mock(AutenticadorAdministrador.class);
    private final JwtService jwt = mock(JwtService.class);
    private final AuthController controller = new AuthController(autenticador, jwt);

    @Test
    void login_exitoso_debe_loguear_info_sin_usuario_ni_password_ni_token() {
        when(autenticador.autenticar("admin-secreto", "clave-secreta")).thenReturn(Mono.just(true));
        when(jwt.emitir("admin-secreto")).thenReturn(new TokenEmitido("token-abc", 900L));

        try (CapturaLogs logs = CapturaLogs.de(AuthController.class)) {
            StepVerifier.create(controller.login(new LoginRequest("admin-secreto", "clave-secreta")))
                    .expectNextCount(1).verifyComplete();

            assertThat(logs.mensajes()).containsExactly("INFO Login exitoso");
        }
    }

    @Test
    void login_fallido_debe_loguear_warn_sin_usuario_ni_password() {
        when(autenticador.autenticar("admin-secreto", "mala")).thenReturn(Mono.just(false));

        try (CapturaLogs logs = CapturaLogs.de(AuthController.class)) {
            StepVerifier.create(controller.login(new LoginRequest("admin-secreto", "mala")))
                    .expectError(CredencialesInvalidasException.class).verify();

            assertThat(logs.mensajes()).containsExactly("WARN Login fallido");
        }
    }
}
