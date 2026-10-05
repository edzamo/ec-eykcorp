package com.eykcorp.clientes.infrastructure.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.test.StepVerifier;

class AutenticadorAdministradorTest {

    private final BCryptPasswordEncoder codificador = spy(new BCryptPasswordEncoder(4));
    private final AutenticadorAdministrador autenticador = new AutenticadorAdministrador(
            new AdminProperties(TokensDePrueba.USUARIO, TokensDePrueba.HASH), codificador);

    @Test
    void debe_autenticar_con_usuario_y_password_correctos() {
        StepVerifier.create(autenticador.autenticar(TokensDePrueba.USUARIO, TokensDePrueba.PASSWORD))
                .expectNext(true).verifyComplete();
    }

    @Test
    void debe_rechazar_password_incorrecta() {
        StepVerifier.create(autenticador.autenticar(TokensDePrueba.USUARIO, "otra"))
                .expectNext(false).verifyComplete();
    }

    @Test
    void debe_rechazar_usuario_incorrecto_aunque_la_password_sea_correcta() {
        StepVerifier.create(autenticador.autenticar("intruso", TokensDePrueba.PASSWORD))
                .expectNext(false).verifyComplete();
    }

    @Test
    void debe_rechazar_usuario_y_password_incorrectos() {
        StepVerifier.create(autenticador.autenticar("intruso", "otra"))
                .expectNext(false).verifyComplete();
    }

    @Test
    void siempre_debe_ejecutar_matches_aunque_el_usuario_no_coincida() {
        StepVerifier.create(autenticador.autenticar("intruso", "otra")).expectNext(false).verifyComplete();

        verify(codificador, atLeastOnce()).matches(anyString(), anyString());
    }

    @Test
    void debe_tratar_credenciales_nulas_como_incorrectas() {
        StepVerifier.create(autenticador.autenticar(null, null)).expectNext(false).verifyComplete();
    }

    @Test
    void no_debe_bloquear_el_hilo_que_suscribe() {
        StepVerifier.create(autenticador.autenticar(TokensDePrueba.USUARIO, TokensDePrueba.PASSWORD)
                        .map(ok -> Thread.currentThread().getName()))
                .expectNextMatches(hilo -> hilo.startsWith("boundedElastic"))
                .verifyComplete();
    }
}
