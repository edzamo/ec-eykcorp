package com.eykcorp.clientes;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.infrastructure.adapter.out.persistence.PostgresTestContainer;
import com.eykcorp.clientes.infrastructure.security.PropiedadesDeSeguridadDePrueba;
import com.eykcorp.clientes.infrastructure.security.TokensDePrueba;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** EXC-2: con MongoDB inalcanzable la operación del cliente no falla y la salud no depende de Mongo. */
@PropiedadesDeSeguridadDePrueba
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteMongoCaidoE2ETest {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registrarPropiedades(registry);
        registry.add("spring.data.mongodb.uri",
                () -> "mongodb://localhost:1/auditoria?serverSelectionTimeoutMS=500&connectTimeoutMS=500");
    }

    @Autowired
    private WebTestClient web;

    @Test
    void debe_responder_201_dentro_del_timeout_aunque_mongo_no_responda() {
        long inicio = System.nanoTime();

        web.mutate().responseTimeout(Duration.ofSeconds(10)).build()
                .post().uri("/clientes").headers(h -> h.setBearerAuth(TokensDePrueba.tokenVigente()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("nombres", "Ana", "apellidos", "Pérez",
                        "correo", "mongo-caido-" + UUID.randomUUID() + "@example.com"))
                .exchange()
                .expectStatus().isCreated();

        assertThat(Duration.ofNanos(System.nanoTime() - inicio)).isLessThan(Duration.ofSeconds(10));
    }

    @Test
    void la_salud_debe_seguir_UP_aunque_mongo_este_caido() {
        web.get().uri("/actuator/health").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("UP");
    }
}
