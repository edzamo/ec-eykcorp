package com.eykcorp.clientes;

import com.eykcorp.clientes.infrastructure.adapter.out.audit.MongoTestContainer;
import com.eykcorp.clientes.infrastructure.adapter.out.persistence.PostgresTestContainer;
import com.eykcorp.clientes.infrastructure.security.PropiedadesDeSeguridadDePrueba;
import com.eykcorp.clientes.infrastructure.security.TokensDePrueba;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@PropiedadesDeSeguridadDePrueba
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientesApplicationTests {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registrarPropiedades(registry);
        MongoTestContainer.registrarPropiedades(registry);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void debe_responder_200_con_status_UP_cuando_se_consulta_actuator_health() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void debe_responder_401_cuando_se_consulta_actuator_env_sin_token() {
        webTestClient.get().uri("/actuator/env")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void no_debe_exponer_actuator_env_ni_con_token_valido() {
        webTestClient.get().uri("/actuator/env")
                .headers(h -> h.setBearerAuth(TokensDePrueba.tokenVigente()))
                .exchange()
                .expectStatus().value(status -> org.assertj.core.api.Assertions.assertThat(status)
                        .isIn(403, 404));
    }

    @Test
    void debe_arrancar_el_contexto_completo_con_la_cadena_de_seguridad() {
        webTestClient.get().uri("/clientes").exchange().expectStatus().isUnauthorized();
    }
}
