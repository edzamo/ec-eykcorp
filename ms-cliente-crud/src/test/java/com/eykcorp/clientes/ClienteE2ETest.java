package com.eykcorp.clientes;

import static org.assertj.core.api.Assertions.assertThat;

import com.eykcorp.clientes.infrastructure.adapter.out.persistence.PostgresTestContainer;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** E2E del backend: servidor real + PostgreSQL (Testcontainers), sin seguridad ni Mongo todavía. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteE2ETest {

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registrarPropiedades(registry);
    }

    @Autowired
    private WebTestClient web;

    private static Map<String, Object> cuerpo(String correo, String nombres) {
        return Map.of("nombres", nombres, "apellidos", "Pérez Loor", "correo", correo, "telefono", "0991234567");
    }

    private static String correoUnico() {
        return "e2e-" + UUID.randomUUID() + "@example.com";
    }

    private Map<?, ?> crear(String correo) {
        return web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo(correo, "Ana"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
    }

    @Test
    void flujo_completo_crear_listar_obtener_actualizar_eliminar_y_404() {
        String correo = correoUnico();
        Map<?, ?> creado = crear(correo);
        Integer id = (Integer) creado.get("id");
        assertThat(id).isNotNull();

        web.get().uri("/clientes").exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$[?(@.id==" + id + ")].correo").isEqualTo(correo);

        web.get().uri("/clientes/{id}", id).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombres").isEqualTo("Ana");

        web.put().uri("/clientes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo(correo, "Ana María"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombres").isEqualTo("Ana María")
                .jsonPath("$.fechaCreacion").isEqualTo(creado.get("fechaCreacion"));

        web.get().uri("/clientes/{id}", id).exchange()
                .expectBody().jsonPath("$.fechaCreacion").isEqualTo(creado.get("fechaCreacion"));

        web.delete().uri("/clientes/{id}", id).exchange().expectStatus().isNoContent();
        web.get().uri("/clientes/{id}", id).exchange().expectStatus().isNotFound();
        web.delete().uri("/clientes/{id}", id).exchange().expectStatus().isNotFound();
    }

    @Test
    void debe_responder_400_por_validacion() {
        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("nombres", "", "apellidos", "Pérez", "correo", "malo"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.errores.nombres").exists().jsonPath("$.errores.correo").exists();
    }

    @Test
    void debe_responder_409_por_correo_duplicado_al_crear_y_al_actualizar() {
        String correo = correoUnico();
        crear(correo);
        web.post().uri("/clientes").contentType(MediaType.APPLICATION_JSON).bodyValue(cuerpo(correo, "Otra"))
                .exchange().expectStatus().isEqualTo(409);

        Integer otroId = (Integer) crear(correoUnico()).get("id");
        web.put().uri("/clientes/{id}", otroId).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo(correo, "Otra"))
                .exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void debe_responder_404_al_actualizar_un_cliente_inexistente() {
        web.put().uri("/clientes/{id}", 987_654_321L).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cuerpo(correoUnico(), "Ana"))
                .exchange().expectStatus().isNotFound();
    }
}
