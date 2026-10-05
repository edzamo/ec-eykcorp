package com.eykcorp.clientes;

import com.eykcorp.clientes.infrastructure.adapter.out.persistence.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientesApplicationTests {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registrarPropiedades(registry);
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
    void debe_responder_404_cuando_se_consulta_actuator_env_porque_no_esta_expuesto() {
        webTestClient.get().uri("/actuator/env")
                .exchange()
                .expectStatus().isNotFound();
    }
}
